package project.meetu.controller.consult;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import project.meetu.model.dto.College;
import project.meetu.model.dto.Consult;
import project.meetu.model.dto.ConsultableTime;
import project.meetu.model.dto.Department;
import project.meetu.model.dto.Office;
import project.meetu.model.dto.Professor;
import project.meetu.model.dto.Reservation;
import project.meetu.model.dto.ServiceUser;
import project.meetu.model.service.AlertManager;
import project.meetu.model.service.ConsultManager;
import project.meetu.model.service.UserManager;

@Controller
public class ReservationController {

	private final ConsultManager consultService;
	private final UserManager userService;
	private final AlertManager alertService;

	@Autowired
	public ReservationController(ConsultManager consultService, UserManager userService,
			AlertManager alertService) {
		this.consultService = consultService;
		this.userService = userService;
		this.alertService = alertService;
	}

	@GetMapping("/consult/viewReservation")
	public String goReservationView(String consultId, Model model) {

		Consult reservation = consultService.getReservationInfo(consultId);
		model.addAttribute("reservation", reservation);

		return "consult/reservationView";

	}

	@GetMapping("/consult/changeStatus")
	public String changeStatus(Consult reservation, HttpServletRequest req, RedirectAttributes rttr) {
		
		HttpSession session = req.getSession();
		String name = (String) session.getAttribute("name");
		int role = (int) session.getAttribute("role");

		boolean success = consultService.changeReservationStatus(reservation);
		if (!success) {
			rttr.addFlashAttribute("changeFailed", true);
		}
		
		// ?????? ?????? ????????? ?????? ?????? ??????
		success = alertService.addAlertByReservationStatus(name, role, reservation);
		if (!success) {
			rttr.addFlashAttribute("addAlertFailed", true);
		}

		return "redirect:/user/my";

	}

	@RequestMapping(value = "/consult/reservationForm", method = RequestMethod.GET)
	public ModelAndView goReservationForm(HttpServletRequest req, ModelAndView mav) {
		
		HttpSession session = req.getSession();
		String userId = (String) session.getAttribute("id");
		String profNo = (String) req.getParameter("profNo");
		String isUpdate = (String) req.getParameter("isUpdate"); // ???????????? ????????? ??????

		// ????????? ?????? ID
		ServiceUser profUser = userService.getUserByMemberNo(profNo);
		String profId = profUser.getUserId();
		if (profId != null) {
			mav.addObject("profId", profId);
		}
		
		// ????????? ??????
		Office officeInfo = userService.getOfficeByProfId(profUser.getMemberInfo().getMemberNo());
		if (officeInfo != null) {
			mav.addObject("coordinates", officeInfo);
		}

		// ?????? ??????????????? ?????? ?????? ??????
		Professor professorInfo = userService.getProfessorByMemberNo(profNo);
		if (professorInfo != null) {
			mav.addObject("professorInfo", professorInfo);
		}

		// ???????????? ????????? ????????????
		if(isUpdate == null) {
			// ?????? ???????????? ?????? ???????????? ????????? ??????
			boolean isReservated = consultService.checkReservated(userId, profId);
			if (isReservated) {
				mav.addObject("isReservated", 1);
	
				List<College> collegeList = userService.getColleges();
				if (collegeList != null) {
					mav.addObject("colleges", collegeList);
				}
	
				List<Department> departmentList = userService.getDepartments();
				if (departmentList != null) {
					mav.addObject("departments", departmentList);
				}
	
				mav.setViewName("consult/professorSearchPage");
				return mav; // ?????? ?????? ???????????? ?????? ?????? ?????? ?????? ???????????? ??????
			}
		}

		// ?????? ????????? ?????? ?????? ??????
		List<ConsultableTime> consultableTimeList = consultService.getConsultableTimes(profId);
		if (consultableTimeList != null && consultableTimeList.size() != 0) {
			mav.addObject("consultableTimes", consultableTimeList);
		} else {
			mav.addObject("hasConsultableTime", 0);

			List<College> collegeList = userService.getColleges();
			if (collegeList != null) {
				mav.addObject("colleges", collegeList);
			}

			List<Department> departmentList = userService.getDepartments();
			if (departmentList != null) {
				mav.addObject("departments", departmentList);
			}

			mav.setViewName("consult/professorSearchPage");
			return mav; // ?????? ?????? ???????????? ?????? ?????? ?????? ?????? ???????????? ??????
		}

		// ?????? ????????? ?????? ????????? ?????? (?????? ????????? ??????)
		List<Consult> reservationList = consultService.getUndoneReservation(profId);
		if (reservationList != null) {
			mav.addObject("reservations", reservationList);
		}

		mav.setViewName("consult/reservationForm");
		
		// ?????? ?????? ??????
		int newAlertCount = alertService.getNewAlertCount(userId);
		session.setAttribute("newAlerts", newAlertCount);
		
		return mav;
	}

	@PostMapping("/consult/reservate")
	public String reservate(HttpServletRequest req, RedirectAttributes rttr, Model model) {
		System.out.println("reserve");
		HttpSession session = req.getSession();
		String name = (String) session.getAttribute("name"); 
		
		String choiceDate = req.getParameter("choiceDate");
		String startTime = req.getParameter("startTime");
		String consultTime = req.getParameter("consultTime");
		String typeBtn = req.getParameter("type");
		String radio = req.getParameter("radio");
		String profId = req.getParameter("profId");
		String stuId = (String) session.getAttribute("id");
		
		int result = consultService.makeReservation(choiceDate, startTime, consultTime, typeBtn, radio, profId, stuId);

		if (result == 1) {
			// ????????? ????????? ?????? ?????? ??????
			boolean success = alertService.addAlertByMakeReservation(name, profId);
			if (!success) {
				rttr.addFlashAttribute("addAlertFailed", true);
			}
			return "redirect:/user/my";
		}
		else if (result == 2) {
			model.addAttribute("date_check", false);
			
			List<College> collegeList = userService.getColleges();
			if (collegeList != null) {
				model.addAttribute("colleges", collegeList);
			}

			List<Department> departmentList = userService.getDepartments();
			if (departmentList != null) {
				model.addAttribute("departments", departmentList);
			}
			return "/consult/professorSearchPage";
		}
		else {
			model.addAttribute("is_added", false);
			
			List<College> collegeList = userService.getColleges();
			if (collegeList != null) {
				model.addAttribute("colleges", collegeList);
			}

			List<Department> departmentList = userService.getDepartments();
			if (departmentList != null) {
				model.addAttribute("departments", departmentList);
			}
			return "/consult/professorSearchPage";
		}
	}
	
	@RequestMapping("/consult/updateReservationForm")
	public String goUpdateReservationForm(String consultId, String time, String date, RedirectAttributes rttr) {
		Consult reservation = consultService.getReservationInfo(consultId);
		
		System.out.println("??????: " + time);
		System.out.println("??????: " + date);
		
		// 2022-03-18 13:00:00
		if (time != null) {
			String oldStartDate = reservation.getStartDate();
			reservation.setStartDate(oldStartDate.substring(0, 11) + time + oldStartDate.substring(13));
		}
		
		if (date != null) {
			String oldStartDate = reservation.getStartDate();
			reservation.setStartDate(oldStartDate.substring(0, 8) + date + oldStartDate.substring(10));
			String oldEndDate = reservation.getEndDate();
			reservation.setEndDate(oldEndDate.substring(0, 8) + date + oldEndDate.substring(10));
		}
		
		rttr.addFlashAttribute("reservation", reservation);
		// ??????????????? ??????????????? ??????????????? ??? ???????????? ????????? ???????????? ??????(???????????? ?????? ???????????? ?????????)
		String profNo = reservation.getProfUser().getUserId().substring(2);
		String params = "profNo=" + profNo + "&isUpdate=" + "yes";
		
		return "redirect:/consult/reservationForm?" + params;
	}
	
	@PostMapping("/consult/updateReservation")
	public String updateReservation(Reservation reservation, String consultId, HttpServletRequest req, 
			RedirectAttributes rttr) {

		HttpSession session = req.getSession();
		String name = (String) session.getAttribute("name");
		int role = (int) session.getAttribute("role"); 
		
		boolean success = consultService.changeReservationInfo(reservation, consultId);
		if (!success) {
			rttr.addFlashAttribute("updateFailed", true);
		}
		
		// ????????? ????????? ?????? ?????? ??????
		success = alertService.addAlertByUpdateReservation(name, role, consultId);
		if (!success) {
			rttr.addFlashAttribute("addAlertFailed", true);
		}
		
		return "redirect:/user/my";
		
	}
	
}
