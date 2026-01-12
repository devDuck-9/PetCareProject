package com.duck.petcareproject.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.Pet;
import com.duck.petcareproject.service.MemberService;
import com.duck.petcareproject.service.PetService;
import com.duck.petcareproject.service.storage.FileStorageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PetController {
	
	private final PetService petService;
	private final MemberService memberService;
	private final FileStorageService fileStorageService;
	
	
	// 펫 등록
	@PostMapping("/insertPet")
	public String insertPet(Model model, Authentication authentication,
							@RequestParam("petName") String petName,
							@RequestParam("petType") String petType,
							@RequestParam("gender") String gender,
							@RequestParam("birthDate")String birthDate,
							@RequestParam(value="memo", required = false) String memo,
							@RequestParam(value="photo", required = false) MultipartFile photo
				) throws Exception {
		
		// 로그인 체크
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
				return "redirect:/loginForm";
		}

		// 로그인 ID (Spring Security username)
		String userId = authentication.getName();

		// userId → userSeq 조회
		Member loginMember = memberService.getMember(userId);
		Integer userSeq = (Integer)loginMember.getUserSeq();
		if (userSeq == null) return "redirect:/loginForm";
		
		// 이미지 저장
		String petImagePath = null;
		if (photo != null && !photo.isEmpty()) {
			petImagePath = fileStorageService.savePetImage(photo);
		}

		// 나이 계산 (birthDate -> petAge)
		Integer petAge = null;
		if (birthDate != null && !birthDate.isBlank()) {
				LocalDate birth = LocalDate.parse(birthDate);
				petAge = Period.between(birth, LocalDate.now()).getYears();
		}
		
		// Pet 객체 만들기
		Pet pet = new Pet();
		pet.setUserSeq(userSeq);
		pet.setPetName(petName.trim());
		pet.setPetType(petType.trim());
		pet.setPetAge(petAge);
		pet.setPetImage(petImagePath);
		pet.setCreatedAt(LocalDateTime.now());
		pet.setUpdatedAt(LocalDateTime.now());

		// 저장
		petService.insertPet(pet);
		
		// 대쉬보드로 이동
		return "redirect:/";
	}
	
}
