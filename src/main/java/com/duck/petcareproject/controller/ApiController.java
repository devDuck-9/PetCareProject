package com.duck.petcareproject.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.duck.petcareproject.service.MemberService;
import com.duck.petcareproject.service.PetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ApiController {

	private final MemberService memberService;
	private final PetService petService;
	private final CommunityCommentService communityCommentService;

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
	
}
