package com.duck.petcareproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.duck.petcareproject.domain.Gender;
import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.service.MemberService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

	// 회원가입 폼 (서버 검증 메시지를 뿌리려면 모델 객체가 필요)
	@GetMapping("/joinForm")
	public String joinForm(Model model) {
		// 모델에 member 가 없을 때만 실행
		if (!model.containsAttribute("member")) {
			Member member = new Member();
			member.setGender(Gender.U);
			model.addAttribute("member", member);
		}
		return "member/memberJoinForm";
	}

	// 회원 가입
	@PostMapping("/joinResult")
	public String joinResult(@Valid @ModelAttribute("member") Member member,
							 BindingResult bindingResult,
							 Model model) {

		// 서버 검증 실패 시 다시 회원가입 폼으로
		if (bindingResult.hasErrors()) {
			return "member/memberJoinForm";
		}

		// 성별 빈값 방지
		if (member.getGender() == null) member.setGender(Gender.U);

		// 이메일 합치기 (emailId + emailDomain)
		if (member.getEmailId() != null && member.getEmailDomain() != null) {
			String email = member.getEmailId().trim() + "@" + member.getEmailDomain().trim();
			member.setEmail(email);
		}

		// 저장
		memberService.addMember(member);

		return "redirect:/loginForm";
	}

}
