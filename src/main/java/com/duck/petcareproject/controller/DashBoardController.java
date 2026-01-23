package com.duck.petcareproject.controller;

import java.util.Collections;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.duck.petcareproject.domain.DashboardDTO;
import com.duck.petcareproject.service.DashboardService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashBoardController {
	
	private final DashboardService dashboardService;
		
	@GetMapping("/")
	public String dashboard(Model model, Authentication authentication, HttpServletRequest request,
						@RequestParam(value = "petPage", required = false) Integer petPage,
						@RequestParam(value = "schPage", required = false) Integer schPage,
						@RequestParam(value = "postPage", required = false) Integer postPage) {
		
		// 헤더 메뉴 활성화
		model.addAttribute("activeMenu", "dashboard");
		
		// 로그인 전
		if (isAnonymous(authentication)) {
				return "views/dashboard/dashboard";
		}
		
		// 로그인 후
		boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		if (isAdmin) {
			return "redirect:/admin/dashboard";
		}
		
		// 로그인 ID (Spring Security 의 userName)
		String userId = authentication.getName();
		
		 // 대시보드 데이터 한 번에 조립해서 받기
		DashboardDTO dto = dashboardService.getDashboard(userId, petPage, schPage, postPage);
		
		// 로그인은 되어있는데 DB 사용자 없을 경우 -> 강제 로그아웃
		if (dto == null || dto.getLoginMember() == null) {
			SecurityContextHolder.clearContext();
			HttpSession session = request.getSession(false);
			if (session != null) session.invalidate();
			return "redirect:/loginForm?error=account";
		}

		// 사용자
		model.addAttribute("loginMember", dto.getLoginMember());
		
		// 반려동물
		model.addAttribute("pets", dto.getPets());
		model.addAttribute("petPage", dto.getPetPage());
		model.addAttribute("petTotalPage", dto.getPetTotalPage());
		
		// 일정
		model.addAttribute("schedules", dto.getSchedules());
		model.addAttribute("schPage", dto.getSchPage());
		model.addAttribute("schTotalPage", dto.getSchTotalPage());
		
		// 오늘의 게시글
		model.addAttribute("todayPosts", dto.getTodayPosts());
	    model.addAttribute("postPage", dto.getPostPage());
	    model.addAttribute("postTotalPage", dto.getPostTotalPage());
	    
		return "views/dashboard/dashboard";
	}
	
	// ================================================
	//	비동기: 반려동물 패널 fragment
	// ================================================
	@GetMapping("/dashboard/pets")
	public String petsFragment(Model model, Authentication authentication,
								@RequestParam(value = "petPage", required = false) Integer petPage) {

		if (isAnonymous(authentication)) {
			// 비동기 요청인데 전체 화면 반환하면 깨질 수 있으니 fragment 만 빈 값으로 반환
			model.addAttribute("pets", Collections.emptyList());
			model.addAttribute("petPage", 1);
			model.addAttribute("petTotalPage", 1);
			return "views/dashboard/fragments :: petPanel";
		}

		String userId = authentication.getName();
		DashboardDTO dto = dashboardService.getDashboard(userId, petPage, null, null);

		model.addAttribute("pets", dto.getPets());
		model.addAttribute("petPage", dto.getPetPage());
		model.addAttribute("petTotalPage", dto.getPetTotalPage());

		return "views/dashboard/fragments :: petPanel";
	}

	// ================================================
	//	비동기: 일정 패널 fragment
	// ================================================
	@GetMapping("/dashboard/schedules")
	public String schedulesFragment(Model model, Authentication authentication,
									@RequestParam(value = "schPage", required = false) Integer schPage) {

		if (isAnonymous(authentication)) {
			model.addAttribute("schedules", Collections.emptyList());
			model.addAttribute("schPage", 1);
			model.addAttribute("schTotalPage", 1);
			return "views/dashboard/fragments :: schedulePanel";
		}

		String userId = authentication.getName();
		DashboardDTO dto = dashboardService.getDashboard(userId, null, schPage, null);

		model.addAttribute("schedules", dto.getSchedules());
		model.addAttribute("schPage", dto.getSchPage());
		model.addAttribute("schTotalPage", dto.getSchTotalPage());

		return "views/dashboard/fragments :: schedulePanel";
	}

	// ================================================
	//	비동기: 오늘의 게시글 패널 fragment
	// ================================================
	@GetMapping("/dashboard/today-posts")
	public String todayPostsFragment(Model model, Authentication authentication,
									@RequestParam(value = "postPage", required = false) Integer postPage) {

		if (isAnonymous(authentication)) {
			model.addAttribute("todayPosts", Collections.emptyList());
			model.addAttribute("postPage", 1);
			model.addAttribute("postTotalPage", 1);
			return "views/dashboard/fragments :: postPanel";
		}

		String userId = authentication.getName();
		DashboardDTO dto = dashboardService.getDashboard(userId, null, null, postPage);

		model.addAttribute("todayPosts", dto.getTodayPosts());
		model.addAttribute("postPage", dto.getPostPage());
		model.addAttribute("postTotalPage", dto.getPostTotalPage());

		return "views/dashboard/fragments :: postPanel";
	}

	// ================================================
	//	공통: 로그인 여부 체크
	// ================================================
	private boolean isAnonymous(Authentication authentication) {
		return (authentication == null) || (authentication instanceof AnonymousAuthenticationToken);
	}
	
	
}
