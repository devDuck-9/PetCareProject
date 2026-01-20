package com.duck.petcareproject.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duck.petcareproject.domain.CommunityComment;
import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.Pet;
import com.duck.petcareproject.service.CommunityCommentService;
import com.duck.petcareproject.service.KakaoLocalService;
import com.duck.petcareproject.service.MemberService;
import com.duck.petcareproject.service.PetService;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ApiController {

	private final MemberService memberService;
	private final PetService petService;
	private final CommunityCommentService communityCommentService;
	private final JavaMailSender mailSender;
	private final KakaoLocalService kakaoLocalService;
	
	@Value("${app.sms.mock:false}")
	private boolean smsMock;
	
	@PostConstruct
	public void check() {
		System.out.println("SMS MOCK MODE = " + smsMock);
	}
	
	// 병원정보
	@GetMapping("/api/hospitals")
	public ResponseEntity<String> list(@RequestParam("lat") double lat,
										@RequestParam("lng") double lng,
										@RequestParam(value = "q", required = false) String q,
										@RequestParam(value = "page", defaultValue = "1") int page,
										@RequestParam(value = "size", defaultValue = "5") int size) {
		try {
			if (page < 1) page = 1;
			if (size < 1) size = 5;
			if (size > 15) size = 15;

			String json = kakaoLocalService.searchHospitals(lat, lng, q, page, size);
			return ResponseEntity.ok(json);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("{\"error\":\"kakao api failed\"}");
		}
	}
	
	// 아이디 중복 확인
	@GetMapping("/api/member/check-id")
	public ResponseEntity<Map<String, Object>> checkUserId(@RequestParam("userId") String userId) {

		Map<String, Object> res = new HashMap<>();

		String trimmed = (userId == null) ? "" : userId.trim();
		res.put("userId", trimmed);

		// 빈값이면 400
		if (trimmed.isEmpty()) {
			res.put("exists", false);
			res.put("message", "아이디를 입력해주세요.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}

		boolean exists = memberService.existsByUserId(trimmed);
		res.put("exists", exists);
		res.put("message", exists ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.");

		return ResponseEntity.ok(res);
	}
	
	// 이름(닉네임) 중복확인
	@GetMapping("/api/member/check-name")
	public ResponseEntity<Map<String, Object>> checkUserName(@RequestParam("userName") String userName) {

		Map<String, Object> res = new HashMap<>();

		String trimmed = (userName == null) ? "" : userName.trim();
		res.put("userName", trimmed);

		if (trimmed.isEmpty()) {
			res.put("exists", false);
			res.put("message", "이름(닉네임)을 입력해주세요.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}

		boolean exists = memberService.existsByUserName(trimmed);
		res.put("exists", exists);
		res.put("message", exists ? "이미 사용 중인 이름(닉네임)입니다." : "사용 가능한 이름(닉네임)입니다.");

		return ResponseEntity.ok(res);
	}
	
	// 내 펫 목록
	@GetMapping("/api/pet/mine")
	public ResponseEntity<?> myPets(Authentication authentication) {

		// 로그인 안 됐으면 401
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
		}

		String userId = authentication.getName();

		Member m = memberService.getMember(userId);

		// 로그인은 됐는데 DB에 멤버가 없으면 404
		if (m == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "회원 정보를 찾을 수 없습니다."));
		}

		List<Pet> pets = petService.selectPetsByUser(m.getUserSeq());
		return ResponseEntity.ok(pets);
	}
	
	// 댓글 비동기 수정
	@PostMapping("/api/comment/update")
	public ResponseEntity<Map<String, Object>> updateCommentAjax( Authentication auth,
					@RequestParam("commentSeq") int commentSeq,
					@RequestParam("content") String content) {
					
		Map<String, Object> res = new HashMap<>();

		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			res.put("ok", false);
			res.put("msg", "로그인이 필요합니다.");
			return ResponseEntity.status(401).body(res);
		}

		Member member = memberService.getMember(auth.getName());
		if (member == null) {
			res.put("ok", false);
			res.put("msg", "로그인이 필요합니다.");
			return ResponseEntity.status(401).body(res);
		}

		String text = (content == null ? "" : content.trim());
		if (text.isBlank()) {
			res.put("ok", false);
			res.put("msg", "댓글 내용을 입력해주세요.");
			return ResponseEntity.badRequest().body(res);
		}

		// 내 댓글인지 확인
		CommunityComment origin = communityCommentService.getCommentByIdAndUser(commentSeq, member.getUserSeq());
		if (origin == null) {
			res.put("ok", false);
			res.put("msg", "수정 권한이 없습니다.");
			return ResponseEntity.status(403).body(res);
		}

		CommunityComment up = new CommunityComment();
		up.setCommentSeq(commentSeq);
		up.setUserSeq(member.getUserSeq());
		up.setContent(text);

		boolean ok = communityCommentService.updateComment(up);
		if (!ok) {
			res.put("ok", false);
			res.put("msg", "수정에 실패했습니다.");
			return ResponseEntity.internalServerError().body(res);
		}

		res.put("ok", true);
		res.put("msg", "수정되었습니다.");
		res.put("content", text); // 프론트에서 업데이트할 최신 내용
		return ResponseEntity.ok(res);
	}
	
	// 핸드폰 인증번호
	@PostMapping("/api/sms/send")
	public ResponseEntity<Map<String, Object>> send(@RequestParam("mobile") String mobile, HttpSession session) {
		Map<String, Object> res = new HashMap<>();
		String m = mobile == null ? "" : mobile.trim();

		if (!m.matches("^01[0-9]-\\d{3,4}-\\d{4}$")) {
			res.put("message", "휴대폰 번호 형식이 올바르지 않습니다.");
			return ResponseEntity.badRequest().body(res);
		}

		// 재발송 제한(60초)
		Long lastSent = (Long) session.getAttribute("SMS_LAST_SENT");
		long now = System.currentTimeMillis();
		if (lastSent != null && (now - lastSent) < 60_000) {
			res.put("message", "재발송은 60초 후 가능합니다. 잠시 후 다시 시도해주세요.");
			return ResponseEntity.status(429).body(res);
		}

		String code = String.format("%06d", (int)(Math.random() * 1_000_000));
		long expiresAt = now + (3 * 60 * 1000); // 3분

		session.setAttribute("SMS_MOBILE", m);
		session.setAttribute("SMS_CODE", code);
		session.setAttribute("SMS_EXPIRES", expiresAt);
		session.setAttribute("SMS_TRIES", 0);
		session.setAttribute("SMS_VERIFIED", false);
		session.setAttribute("SMS_LAST_SENT", now);

		// 실제 발송 대신 개발용 로그 출력
		System.out.println("# ========================================================================= #");
		System.out.println("[DEV SMS] mobile=" + m + ", code=" + code + ", expiresAt=" + expiresAt);
		System.out.println("# ========================================================================= #");

		res.put("message", "인증번호를 발송했어요. ");
		
		// 개발모드(mock)일 때만 프론트로 코드 내려주기
		if (smsMock) {
			res.put("devCode", code);
		}
		
		return ResponseEntity.ok(res);
	}

	@PostMapping("/api/sms/verify")
	public ResponseEntity<Map<String, Object>> verify(@RequestParam("mobile") String mobile, @RequestParam("code") String code, HttpSession session) {
		Map<String, Object> res = new HashMap<>();
		String m = mobile == null ? "" : mobile.trim();
		String c = code == null ? "" : code.trim();

		String savedMobile = (String) session.getAttribute("SMS_MOBILE");
		String savedCode = (String) session.getAttribute("SMS_CODE");
		Long exp = (Long) session.getAttribute("SMS_EXPIRES");
		Integer tries = (Integer) session.getAttribute("SMS_TRIES");
		
		if (!m.matches("^01[0-9]-\\d{3,4}-\\d{4}$")) {
			res.put("message", "휴대폰 번호 형식이 올바르지 않습니다.");
			return ResponseEntity.badRequest().body(res);
		}

		if (!c.matches("^\\d{6}$")) {
			res.put("message", "인증번호 6자리를 입력해주세요.");
			return ResponseEntity.badRequest().body(res);
		}
		
		if (savedMobile == null || savedCode == null || exp == null) {
			res.put("message", "인증번호 발송부터 진행해주세요.");
			return ResponseEntity.badRequest().body(res);
		}

		if (System.currentTimeMillis() > exp) {
			res.put("message", "인증번호가 만료되었습니다. 다시 발송해주세요.");
			return ResponseEntity.badRequest().body(res);
		}

		int t = (tries == null) ? 0 : tries;
		if (t >= 5) {
			res.put("message", "인증 시도 횟수를 초과했어요. 인증번호를 다시 발송해주세요.");
			return ResponseEntity.status(429).body(res);
		}

		session.setAttribute("SMS_TRIES", t + 1);

		if (!m.equals(savedMobile) || !c.equals(savedCode)) {
			res.put("message", "인증번호가 올바르지 않습니다.");
			return ResponseEntity.badRequest().body(res);
		}

		session.setAttribute("SMS_VERIFIED", true);
		session.removeAttribute("SMS_CODE");
		session.removeAttribute("SMS_EXPIRES");
		session.removeAttribute("SMS_TRIES");
		
		res.put("message", "휴대폰 인증이 완료되었습니다 ✅");
		
		return ResponseEntity.ok(res);
	}
	
	// ==== 이메일 인증 ====
	@PostMapping("/api/email/send")
	public ResponseEntity<Map<String, Object>> sendEmailCode(@RequestParam("email") String email, HttpSession session) {
		Map<String, Object> res = new HashMap<>();
		String e = email == null ? "" : email.trim();
		
		// 간단 형식 체크
		if (!e.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
			res.put("message", "이메일 형식이 올바르지 않습니다.");
			return ResponseEntity.badRequest().body(res);
		}

		// 재발송 제한(60초)
		Long lastSent = (Long) session.getAttribute("EMAIL_LAST_SENT");
		long now = System.currentTimeMillis();
		if (lastSent != null && (now - lastSent) < 60_000) {
			res.put("message", "재발송은 60초 후 가능합니다. 잠시 후 다시 시도해주세요.");
			return ResponseEntity.status(429).body(res);
		}
		
		String code = String.format("%06d", (int)(Math.random() * 1_000_000));
		long expiresAt = now + (3 * 60 * 1000); // 3분

		session.setAttribute("EMAIL_ADDR", e);
		session.setAttribute("EMAIL_CODE", code);
		session.setAttribute("EMAIL_EXPIRES", expiresAt);
		session.setAttribute("EMAIL_TRIES", 0);
		session.setAttribute("EMAIL_VERIFIED", false);
		session.setAttribute("EMAIL_LAST_SENT", now);

		// 메일 발송
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom("memmem9948@gmail.com");
		msg.setTo(e);
		msg.setSubject("[PetCare] 이메일 인증번호");
		msg.setText("인증번호는 🔐 " + code + " 입니다. \n\n ✅ 3분 내 입력해주세요.");
		try {
			
			System.out.println("[EMAIL] send try to=" + e);
			mailSender.send(msg);
			System.out.println("[EMAIL] send success to=" + e);
			
			System.out.println("# ============================================== #");
			System.out.println("[EMAIL] send code=" + code);
			System.out.println("# ============================================== #");
			
		} catch (Exception ex) {
			
			System.out.println("[EMAIL] send FAILED: " + ex.getClass().getName() + " / " + ex.getMessage());
			ex.printStackTrace();
			System.out.println("[DEV EMAIL] email=" + e + ", code=" + code);
			
			res.put("message", "메일 발송에 실패했어요. (개발모드: 콘솔 인증번호 확인)");
			return ResponseEntity.status(500).body(res);
		}

		res.put("message", "인증번호를 이메일로 발송했어요.");
		
		System.out.println("verify sessionId=" + session.getId());
		session.setAttribute("EMAIL_VERIFIED", true);
		session.setAttribute("EMAIL_ADDR", email);
		System.out.println("set EMAIL_VERIFIED/EMAIL_ADDR=" + email);
		
		return ResponseEntity.ok(res);
	}

	@PostMapping("/api/email/verify")
	public ResponseEntity<Map<String, Object>> verifyEmailCode(HttpSession session,
												@RequestParam("email") String email,
												@RequestParam("code") String code) {
		
		Map<String, Object> res = new HashMap<>();
		String e = email == null ? "" : email.trim();
		String c = code == null ? "" : code.trim();

		String savedEmail = (String) session.getAttribute("EMAIL_ADDR");
		String savedCode = (String) session.getAttribute("EMAIL_CODE");
		Long exp = (Long) session.getAttribute("EMAIL_EXPIRES");
		Integer tries = (Integer) session.getAttribute("EMAIL_TRIES");

		if (savedEmail == null || savedCode == null || exp == null) {
			res.put("message", "인증번호 발송부터 진행해주세요.");
			return ResponseEntity.badRequest().body(res);
		}

		if (System.currentTimeMillis() > exp) {
			res.put("message", "인증번호가 만료되었습니다. 다시 발송해주세요.");
			return ResponseEntity.badRequest().body(res);
		}

		int t = tries == null ? 0 : tries;
		if (t >= 5) {
			res.put("message", "인증 시도 횟수를 초과했어요. 인증번호를 다시 발송해주세요.");
			return ResponseEntity.status(429).body(res);
		}
		session.setAttribute("EMAIL_TRIES", t + 1);

		if (!e.equals(savedEmail) || !c.equals(savedCode)) {
			res.put("message", "인증번호가 올바르지 않습니다.");
			return ResponseEntity.badRequest().body(res);
		}

		session.setAttribute("EMAIL_VERIFIED", true);
		session.setAttribute("EMAIL_ADDR", savedEmail);

		// 재사용 방지
		session.removeAttribute("EMAIL_CODE");
		session.removeAttribute("EMAIL_EXPIRES");
		session.removeAttribute("EMAIL_TRIES");

		res.put("message", "이메일 인증이 완료되었습니다 ✅");
		return ResponseEntity.ok(res);
	}
	
	
}
