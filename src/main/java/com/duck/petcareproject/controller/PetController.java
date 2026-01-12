package com.duck.petcareproject.controller;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PetController {
	
	@Value("${app.upload.dir}")
	private String uploadDir;
	
	private final PetService petService;
	private final MemberService memberService;
	
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
			petImagePath = savePetImage(photo);
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
	
	// 파일(이미지)
	private String savePetImage(MultipartFile file) throws Exception {
		// 저장 폴더
		File dir = new File(uploadDir, "pet");
		if (!dir.exists()) dir.mkdirs();
		
		// 확장자
		String original = file.getOriginalFilename();
		String ext = getExtLower(original);
		
		// 파일명
		String savedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
		
		// 저장
		File target = new File(dir, savedName);
		file.transferTo(target);
		
		// DB에 저장할 웹 접근 경로
		return "/resources/files/pet/" + savedName;
		
	}
	private String getExtLower(String filename) {
		if (filename == null) return "jpg";
		int idx = filename.lastIndexOf('.');
		if (idx < 0) return "jpg";
		return filename.substring(idx + 1).toLowerCase();
	}
	
	

}
