package project.meetu.controller.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import project.meetu.model.dto.Member;
import project.meetu.model.dto.ServiceUser;
import project.meetu.model.service.UserManager;
import project.meetu.model.service.exception.LoginException;

@Controller
public class LoginController {
	
	private final UserManager userService;
	
	@Autowired // 컨테이너에 있는 서비스와 연결됨
	public LoginController(UserManager userService) {
		this.userService = userService;
	}

	@GetMapping("/")
	public String index() {
		return "user/loginForm";
	}
	
	@PostMapping("user/login") // POST방식으로 데이터 전달 시 사용
	public String login(@RequestParam("role") String role, ServiceUser user, 
			HttpServletRequest req, RedirectAttributes rttr) {
		user.setMemberInfo(new Member());
		user.getMemberInfo().setRole(Integer.parseInt(role));	
		
		try {
			user = userService.login(user);
			
			HttpSession session = req.getSession();
			session.setAttribute("id", user.getUserId());
			session.setAttribute("name", user.getMemberInfo().getName());
			session.setAttribute("role", user.getMemberInfo().getRole());
			
			return "user/loginSuccess";
			
		} catch (LoginException e) {
			rttr.addFlashAttribute("loginFailed", true); // redirect시 값 전달
			rttr.addFlashAttribute("exception", e.getMessage());
			return "redirect:/";
		}
	
	}
}
