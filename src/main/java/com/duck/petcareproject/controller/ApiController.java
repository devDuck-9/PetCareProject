package com.duck.petcareproject.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.Pet;
import com.duck.petcareproject.service.MemberService;
import com.duck.petcareproject.service.PetService;

@RestController
public class ApiController {
	
	@Autowired
	private MemberService memberService;
	@Autowired
	private PetService petService;
	
	// 아이디 중복 확인
	@GetMapping("/api/member/check-id")
	public Map<String, Object> checkUserId(@RequestParam("userId") String userId) {
			
			Map<String, Object> res = new HashMap<>();
			
			String trimmed = userId == null ? "" : userId.trim();
			res.put("userId", trimmed);
			
			// 빈값 방어
			if (trimmed.isEmpty()) {
					res.put("exists", false);
					res.put("message", "아이디를 입력해주세요.");
					return res;
			}

			boolean exists = memberService.existsByUserId(trimmed);
			res.put("exists", exists);
			res.put("message", exists ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.");

			return res;
	}
	
	// 펫 목록
	@GetMapping("/api/pets/mine")
	public List<Pet> myPets(Authentication authentication) {
		String userId = authentication.getName();
		Member m = memberService.getMember(userId);
		return petService.selectPetsByUser(m.getUserSeq());
	}
}
