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
	
//	// 내정보
//	@GetMapping("/mypage")
//	public String myPage(Authentication auth, Model model) {
//		
//	}
	
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
	public String joinResult(@Valid @ModelAttribute("member") Member member, BindingResult bindingResult, Model model, HttpSession session) {

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
		
		// 휴대폰
		Boolean verified = (Boolean) session.getAttribute("SMS_VERIFIED");
		String verifiedMobile = (String) session.getAttribute("SMS_MOBILE");

		if (verified == null || !verified || verifiedMobile == null || !verifiedMobile.equals(member.getMobile())) {
			bindingResult.rejectValue("mobile", "mobile.verify", "휴대폰 인증을 완료해주세요.");
			return "member/memberJoinForm";
		}
		
		System.out.println("joinResult sessionId=" + session.getId());
		System.out.println("EMAIL_VERIFIED=" + session.getAttribute("EMAIL_VERIFIED"));
		System.out.println("EMAIL_ADDR=" + session.getAttribute("EMAIL_ADDR"));
		
		// 이메일
		Boolean emailVerified = (Boolean) session.getAttribute("EMAIL_VERIFIED");
		String verifiedEmail = (String) session.getAttribute("EMAIL_ADDR");

		if (emailVerified == null || !emailVerified || verifiedEmail == null || !verifiedEmail.equals(member.getEmail())) {
			bindingResult.rejectValue("emailId", "email.verify", "이메일 인증을 완료해주세요.");
			return "member/memberJoinForm";
		}
		
		// 아이디 중복
		if (memberService.existsByUserId(member.getUserId())) {
			bindingResult.rejectValue("userId", "userId.dup", "이미 사용 중인 아이디입니다.");
			return "member/memberJoinForm";
		}
		
		// 이름(닉네임)
		if (memberService.existsByUserName(member.getUserName())) {
			bindingResult.rejectValue("userName", "userName.dup", "이미 사용 중인 이름(닉네임)입니다.");
			return "member/memberJoinForm";
		}
		
		
		// 저장
		memberService.addMember(member);
		
		
		// 재사용 방지
		session.removeAttribute("SMS_VERIFIED");
		session.removeAttribute("SMS_MOBILE");
		session.removeAttribute("SMS_LAST_SENT");
		session.removeAttribute("SMS_CODE");
		session.removeAttribute("SMS_EXPIRES");
		session.removeAttribute("SMS_TRIES");
		
		session.removeAttribute("EMAIL_VERIFIED");
		session.removeAttribute("EMAIL_ADDR");
		session.removeAttribute("EMAIL_LAST_SENT");
		
		return "redirect:/loginForm";
	}

}
