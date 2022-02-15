package project.meetu.controller.consult;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import project.meetu.model.dto.College;
import project.meetu.model.dto.Consult;
import project.meetu.model.dto.ConsultableTime;
import project.meetu.model.dto.Department;
import project.meetu.model.dto.Professor;
import project.meetu.model.dto.ServiceUser;
import project.meetu.model.service.ConsultManager;
import project.meetu.model.service.UserManager;

@Controller
public class ReservationController {
	
	private final ConsultManager consultService;
	private final UserManager userService;
	
	@Autowired
	public ReservationController(ConsultManager consultService, UserManager userService) {
		this.consultService = consultService;
		this.userService = userService;
	}
	
	@GetMapping("/consult/viewReservation")
	public String goReservationView(@RequestParam("consultId") String consultId, Model model) {
		
		Consult reservation = consultService.getReservationInfo(consultId);
		model.addAttribute("reservation", reservation);
		
		return "consult/reservationView";
		
	}
	
	@GetMapping("/consult/changeStatus")
	public String changeStatus(Consult reservation, RedirectAttributes rttr) {
		
		boolean success = consultService.changeReservationStatus(reservation);
		if (!success) {
			rttr.addFlashAttribute("changeFailed", true);
		}
		
		return "redirect:/user/my";
		
	}
	
	@RequestMapping(value = "/consult/reservationForm", method=RequestMethod.GET) 
	public ModelAndView goReservationForm(HttpServletRequest req, ModelAndView mav) {
		HttpSession session = req.getSession();
		String userId = (String) session.getAttribute("id");
		String profNo = (String) req.getParameter("profNo");
		
		// 교수의 회원 ID
		ServiceUser profUser = userService.getUserByMemberNo(profNo);
		String profId = profUser.getUserId();
		
		// 예약 페이지에서 보일 교수 정보
		Professor professorInfo = userService.getProfessorByMemberNo(profNo);
		if (professorInfo != null) {
			mav.addObject("professorInfo", professorInfo);
		}
		
		// 같은 교수에게 예약 레코드가 있는지 여부
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
			return mav; // 상담 가능 시간대가 없는 경우 교수 선택 페이지로 리턴
		}
		
		// 해당 교수의 상담 가능 시간
		List<ConsultableTime> consultableTimeList = consultService.getConsultableTimes(profId);
		if (consultableTimeList != null) {
			mav.addObject("consultableTimes", consultableTimeList);
		}
		else {
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
			return mav; // 상담 가능 시간대가 없는 경우 교수 선택 페이지로 리턴
		}
		
		// 해당 교수의 상담 불가능 시간 (이미 예약된 시간)
		List<Consult> reservationList = consultService.getUndoneReservation(profId);
		if (reservationList != null) {
			mav.addObject("reservations", reservationList);
		}
		
		mav.setViewName("consult/reservationForm");
		return mav;
	}

}
