package project.meetu.model.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import project.meetu.model.dto.College;
import project.meetu.model.dto.Department;
import project.meetu.model.dto.Professor;
import project.meetu.model.dto.ServiceUser;

@Mapper
public interface UserMapper {

	public ServiceUser selectServiceUser(String userId);
	
	public List<College> selectCollege();
	
	public List<Department> selectDepartment();

	public List<Professor> selectProfessorByDept(String deptNo);
	
	public ServiceUser selectServiceUserByMemberNo(String memberNo);
}
