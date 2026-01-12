package com.duck.petcareproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.service.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberController {
	
	private final MemberService memberService;
	
	// 로그인 폼
	@GetMapping("/loginForm")
	public String loginForm(Model model, HttpSession session) {
		Object msg = session.getAttribute("LOGIN_ERROR_MSG");
		if (msg != null) {
			model.addAttribute("loginErrorMsg", msg.toString());
			session.removeAttribute("LOGIN_ERROR_MSG");
		}
		return "member/loginForm";
	}
			
	// 회원 가입
	@PostMapping("/joinResult")
	public String joinResult(@ModelAttribute Member member, Model model) {
		
		// 이메일 합치기 (emailId + emailDomain)
		if (member.getEmailId() != null && member.getEmailDomain() != null) {
			String email = member.getEmailId().trim() + "@" + member.getEmailDomain().trim();
			member.setEmail(email);
		}
		
		// 저장
		memberService.addMember(member);
		
		// 로그인 폼 페이지로 이동
		return "redirect:/loginForm";
	}

}
