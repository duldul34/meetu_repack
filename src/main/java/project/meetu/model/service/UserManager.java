package project.meetu.model.service;
/* 메소드명이 좀 더 비즈니스에 가까움 */

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.meetu.model.dto.College;
import project.meetu.model.dto.ConsultableTime;
import project.meetu.model.dto.Course;
import project.meetu.model.dto.Department;
import project.meetu.model.dto.Member;
import project.meetu.model.dto.Office;
import project.meetu.model.dto.Professor;
import project.meetu.model.dto.ServiceUser;
import project.meetu.model.service.exception.ChangePwdException;
import project.meetu.model.service.exception.LoginException;
import project.meetu.model.service.exception.ResignException;
import project.meetu.model.dao.UserDAO;

@Service
public class UserManager {

	private final UserDAO userDao;

	// 외부에서 가져와 값을 넣어주는 경우 - dependency injection(의존성 주입)
	@Autowired
	public UserManager(UserDAO userDao) {
		this.userDao = userDao;
	}

	/* 로그인 */
	public ServiceUser login(ServiceUser user) throws LoginException {

		ServiceUser findUser = userDao.findUser(user.getUserId());

		if (findUser == null) {
			throw new LoginException("존재하지 않는 회원입니다.");
		} else if (user.getMemberInfo().getRole() != findUser.getMemberInfo().getRole()) {
			throw new LoginException("소속이 일치하지 않습니다.");
		} else if (!user.getPassword().equals(findUser.getPassword())) {
			throw new LoginException("비밀번호가 일치하지 않습니다.");
		}

		return findUser;
	}
	
	public ServiceUser getUserByUserId(String userId) {
		return userDao.findUser(userId);
	}

	/* 전체 단대 조회 */
	public List<College> getColleges() {
		return userDao.findCollegeList();
	}

	/* 전체 학과 조회 */
	public List<Department> getDepartments() {
		return userDao.findDepartmentList();
	}
	
	/* 자신의 학과에 해당하는 과목 목록 조회 */
	public List<Course> getCoursesByDept(String userId) {
		return userDao.findCourseListByDept(userId);
	}

	/* 학과별 교수 목록 조회 */
	public List<Professor> getDeptProfessors(String deptNo) {
		List<Professor> professorList = userDao.findDeptProfessorList(deptNo);

		if (professorList != null) {
			for (Professor p : professorList) {
				ServiceUser findUser = userDao.findUserByMemberNo(p.getMemberNo());
				if (findUser != null) {
					p.setIsUser(true);
				} else {
					p.setIsUser(false);
				}
			}
		}
		return professorList;

	}

	/* 교수 검색 */
	public List<Professor> getProfessorsByKeyword(String keyword) {
		keyword = "%" + keyword + "%";
		List<Professor> professorList = userDao.findProfessorListByKeyword(keyword);

		if (professorList != null) {
			for (Professor p : professorList) {
				ServiceUser findUser = userDao.findUserByMemberNo(p.getMemberNo());
				if (findUser != null) {
					p.setIsUser(true);
				} else {
					p.setIsUser(false);
				}
			}
		}
		return professorList;
	}

	/* MemberNo에 해당하는 교수 정보 조회 */
	public Professor getProfessorByMemberNo(String memberNo) {
		Professor professorInfo = userDao.findProfessorByMemberNo(memberNo);

		ServiceUser findUser = userDao.findUserByMemberNo(memberNo);
		if (findUser != null) {
			professorInfo.setIsUser(true);
		} else {
			professorInfo.setIsUser(false);
		}

		return professorInfo;
	}
	
	/* memberNo에 해당하는 회원 조회 */
	public ServiceUser getUserByMemberNo(String memberNo) {
		ServiceUser serviceUser = userDao.findUserByMemberNo(memberNo);
		return serviceUser;
	}
	
	/* 현재 사용자의 구성원 정보 조회 */
	public Member getMemberInfo(String userId, int role) {
		if (role == 1) {
			return userDao.findStudentMember(userId);
		}
		else if (role == 0) {
			return userDao.findProfessorMember(userId);
		}
		else {
			return null;
		}
	}
	
	/* item에 해당하는 컬럼 값 변경 */
	public boolean changeinfoByItem(String item, String value, String userId) {
		if (item.equals("email")) {
			return userDao.changeEmail(value, userId);
		}
		else if (item.equals("major")) {
			return userDao.changeMajor(value, userId);
		}
		else {
			return false;
		}
	}
	
	/* 과목 추가 또는 삭제 */
	public boolean addOrRemoveClass(String type, String courseNo, String userId) {
		if (type.equals("add")) {
			return userDao.createClass(courseNo, userId);
		}
		else if (type.equals("remove")) {
			return userDao.deleteClass(courseNo, userId);
		}
		else {
			return false;
		}
	}
	
	/* 상담가능시간 추가 또는 삭제 */
	public boolean addOrRemoveConsultableTime(String type, ConsultableTime consultableTime) {
		if (type.equals("add")) {
			return userDao.createConsultableTime(consultableTime);
		}
		else if (type.equals("remove")) {
			return userDao.deleteConsultableTime(consultableTime);
		}
		else {
			return false;
		}
	}
	
	/* 교수 연구실 정보 */
	public Office getOfficeByProfId(String memberNo) {
		return userDao.findOfficeByProfId(memberNo);
	}
	
	/* MemberNo에 해당하는 Member 조회 */
	public Member getMemberByMemberNo(String memberNo) {
		Member member = userDao.findMemberByMemberNo(memberNo);
		return member;
	}
	
	/* 회원가입 확인 */
	public Member registerCheck(ServiceUser serviceUser) {
		String memberNo = serviceUser.getMemberInfo().getMemberNo();
		
		Member member = userDao.findMemberByMemberNo(memberNo);
		if (member != null) { // 학사 DB에 존재하는 사람 중
			if (userDao.findUserByMemberNo(memberNo) == null) { // 회원가입이 되어있지 않은 사람
				return member;
			}
		}
		
		return null;
	}
	
	/* 회원가입 */
	public boolean register(ServiceUser serviceUser) {
		String userId = "ex" + serviceUser.getMemberInfo().getMemberNo();
		serviceUser.setUserId(userId);
		return userDao.createServiceUser(serviceUser);
	}
	
	/* 비밀번호 변경 */
	public boolean changePwd(String userId, String oldPwd, String newPwd, String newPwdCk) 
			throws ChangePwdException {
		ServiceUser user = userDao.findUser(userId);
		if (user != null) {
			if (!user.getPassword().equals(oldPwd)) {
				System.out.println(user.getPassword() + " " + oldPwd);
				throw new ChangePwdException("기존 비밀번호가 일치하지 않습니다.");
			}
			else if (!newPwd.equals(newPwdCk)) {
				throw new ChangePwdException("새 비밀번호 확인 값이 일치하지 않습니다.");
			}
			else {
				return userDao.changePassword(newPwd, userId);
			}
		}
		return false;
	}
	
	/* 회원탈퇴 */
	public boolean resign(String userId, String oldPwd) throws ResignException {
		ServiceUser user = userDao.findUser(userId);
		if (user != null) {
			if (!user.getPassword().equals(oldPwd)) {
				throw new ResignException("비밀번호가 일치하지 않습니다.");
			}
			else {
				return userDao.deleteUser(oldPwd, userId);
			}
		}
		return false;
	}
}
