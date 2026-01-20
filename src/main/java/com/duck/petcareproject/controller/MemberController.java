package com.duck.petcareproject.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.duck.petcareproject.domain.Gender;
import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.MemberPasswordForm;
import com.duck.petcareproject.domain.MemberUpdateForm;
import com.duck.petcareproject.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberController {
	
	private final MemberService memberService;
	
	// 비밀번호 변경 폼
	@GetMapping("/mypage/password")
	public String passwordPage(Authentication auth, Model model, @RequestParam(value = "msg", required = false) String msg) {

		if (auth == null) return "redirect:/loginForm";

		Member entity = memberService.findByUserId(auth.getName());
		if (entity == null) return "redirect:/loginForm";

		model.addAttribute("member", new MemberPasswordForm());
		model.addAttribute("profileMember", entity);	
		model.addAttribute("msg", msg);
		model.addAttribute("activeMenu", "mypage");
		model.addAttribute("activeProfile", "password");
		
		return "member/password";
	}

	// 기존 비밀번호 확인 API (버튼용)
	@PostMapping(value = "/api/member/check-password", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> checkPassword(@RequestBody Map<String, String> body, Authentication auth) {
		if (auth == null) return Map.of("ok", false, "message", "로그인이 필요합니다.");

		String pw = body.getOrDefault("password", "").trim();
		if (pw.isEmpty()) return Map.of("ok", false, "message", "비밀번호를 입력해주세요.");

		boolean ok = memberService.isPasswordMatched(auth.getName(), pw);
		
		return ok ? Map.of("ok", true, "message", "확인 완료! ✅") : Map.of("ok", false, "message", "비밀번호를 다시 입력해주세요.");
	}

	// 비밀번호 변경 처리
	@PostMapping("/mypage/password")
	public String passwordUpdate(@Valid @ModelAttribute("member") MemberPasswordForm form, BindingResult bindingResult, Authentication auth, Model model) {

		if (auth == null) return "redirect:/loginForm";

		// auth로 강제 고정 (사용자가 조작 못 하게)
		form.setUserId(auth.getName());

		// 1) 기본 검증
		if (!form.getNewPassword().equals(form.getConfirmPassword())) {
			bindingResult.rejectValue("confirmPassword", "pw.mismatch", "새 비밀번호가 일치하지 않습니다.");
		}

		if (bindingResult.hasErrors()) {
			Member entity = memberService.findByUserId(auth.getName());
			model.addAttribute("profileMember", entity);
			return "member/password";
		}

		// 2) 서비스에서 기존 비번 체크 + 변경
		try {
			memberService.changePassword(form.getUserId(), form.getCurrentPassword(), form.getNewPassword());
			// 한글 쿼리스트링 금지: 코드로 보내기
			return "redirect:/mypage/password?msg=pw_changed";
		} catch (IllegalArgumentException e) {
			
			 // 새 비밀번호는 기존 비밀번호와 달라야 합니다. 전용 처리
			if (e.getMessage().contains("기존 비밀번호")) {
				return "redirect:/mypage/password?msg=pw_same";
			}
		
			bindingResult.rejectValue("currentPassword", "pw.invalid", e.getMessage());
			Member entity = memberService.findByUserId(auth.getName());
			model.addAttribute("profileMember", entity);
			return "member/password";
		}
	}
	
	// 회원 이미지 저장
	@PostMapping("/member/profile/photo")
	@ResponseBody
	public Map<String, Object> uploadProfile(@RequestParam("profile") MultipartFile file, Authentication authentication, HttpServletRequest request) {
			
		if (authentication == null) return Map.of("ok", false, "message", "로그인이 필요합니다.");

		try {
			String userId = authentication.getName();
			String url = memberService.saveProfilePhoto(userId, file);
			return Map.of("ok", true, "url", url);
		} catch (IllegalArgumentException e) {
			return Map.of("ok", false, "message", e.getMessage());
		} catch (Exception e) {
			return Map.of("ok", false, "message", "업로드에 실패했습니다.");
		}
	}
	
	// 회원탈퇴 폼
	@GetMapping("/member/withdraw")
	public String withdrawPage(Authentication authentication, Model model) {
		String userId = (authentication != null) ? authentication.getName() : null;

		Member member = null;
		if (userId != null) {
			member = memberService.findByUserId(userId);
		}

		model.addAttribute("member", member);
		return "member/withdraw";
	}
	
	// 회원탈퇴
	@PostMapping(value = "/member/withdraw", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> withdraw(@RequestBody Map<String, String> body, Authentication authentication, HttpServletRequest request) {

		if (authentication == null) {
			return Map.of("ok", false, "message", "로그인이 필요합니다.");
		}

		String password = body.getOrDefault("password", "").trim();
		if (password.isEmpty()) {
			return Map.of("ok", false, "message", "비밀번호를 입력해주세요.");
		}

		String userId = authentication.getName();
		try {
			memberService.withdrawByUserId(userId, password);
		} catch (IllegalArgumentException e) {
			// 서비스에서 구분해준 에러 메시지
			return Map.of("ok", false, "message", e.getMessage());
		} catch (Exception e) {
			return Map.of("ok", false, "message", "요청 처리 중 오류가 발생했습니다.");
		}

		// 탈퇴 성공 -> 인증/세션 종료
		SecurityContextHolder.clearContext();
		HttpSession session = request.getSession(false);
		if (session != null) session.invalidate();

		return Map.of("ok", true);
	}
	
	// 내정보 수정
	@PostMapping("/mypage/update")
	public String myPageUpdate(@Valid @ModelAttribute("member") MemberUpdateForm member, Authentication auth, BindingResult bindingResult, Model model) {
		
		if (auth == null) return "redirect:/loginForm";
		
		member.setUserId(auth.getName());
		
		// 이메일 합치기
		if (member.getEmailId() != null && member.getEmailDomain() != null) {
			member.setEmail(member.getEmailId() + "@" + member.getEmailDomain());
		}
		
		// 성별 빈값 방지
		if (member.getGender() == null) member.setGender(Gender.U);
		
		// 검증 실패 시 그대로 반환
		if (bindingResult.hasErrors()) {
			Member entity = memberService.findByUserId(auth.getName());
			model.addAttribute("profileMember", entity);
			return "member/mypage";
		}
		
		// 수정
		try {
			memberService.editMember(member);
			return "redirect:/mypage?saved=1";
		} catch(RuntimeException e) {
			Member entity = memberService.findByUserId(auth.getName());
			model.addAttribute("profileMember", entity);
			model.addAttribute("member", member);
			model.addAttribute("errorMessage", e.getMessage());
			return "member/mypage";
		}
		
	}
	
	// 내정보
	@GetMapping("/mypage")
	public String myPage(Authentication auth, Model model,
						@RequestParam(value = "saved", required = false) String saved,
						@RequestParam(value = "msg", required = false) String msg) {
		
		if (auth == null) return "redirect:/loginForm";

		// 실제 DB 엔티티
		Member entity = memberService.findByUserId(auth.getName());
		if (entity == null) return "redirect:/loginForm";
		
		// 폼 전용 DTO
		MemberUpdateForm form = new MemberUpdateForm();
		form.setUserId(entity.getUserId());
		form.setUserName(entity.getUserName());
		form.setGender(entity.getGender());
		form.setMobile(entity.getMobile());
		form.setZipcode(entity.getZipcode());
		form.setAddress1(entity.getAddress1());
		form.setAddress2(entity.getAddress2());
		
		// 이메일 분리
		if (entity.getEmail() != null && entity.getEmail().contains("@")) {
			String[] parts = entity.getEmail().split("@", 2);
			form.setEmailId(parts[0]);
			form.setEmailDomain(parts[1]);
		}
		
		
		// th:object 용
		model.addAttribute("member", form);
		// 왼쪽 프로필 표시용
		model.addAttribute("profileMember", entity);
		model.addAttribute("activeMenu", "mypage");
		model.addAttribute("activeProfile", "info");

		model.addAttribute("saved", saved);
		if ("1".equals(saved)) {
		    model.addAttribute("msg", "저장되었습니다");
		}
		
		return "member/mypage";
	}
	
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
		try {
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
		} catch (RuntimeException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "member/memberJoinForm";
		}
		
		
	}

}
