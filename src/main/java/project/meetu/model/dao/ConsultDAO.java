package project.meetu.model.dao;

import java.util.List;

import project.meetu.model.dto.College;
import project.meetu.model.dto.Consult;
import project.meetu.model.dto.Department;
import project.meetu.model.dto.Professor;

public interface ConsultDAO {

	// 회원의 전체 상담목록 조회
	List<Consult> findConsultList(String userId);
	
	// 전체 단대 조회
	List<College> findCollegeList();
	
	// 전체 학과 조회
	List<Department> findDepartmentList();

	// 학과별 교수 조회
	List<Professor> findDeptProfessorList(String deptNo);
}