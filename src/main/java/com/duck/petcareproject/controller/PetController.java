package com.duck.petcareproject.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.duck.petcareproject.domain.Gender;
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
	
	// 펫 삭제
	@PostMapping("/deletePet/{id}")
	public String deletePet(@PathVariable("id") Integer petSeq, Authentication authentication, RedirectAttributes ra) {

		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}

		String userId = authentication.getName();
		Member loginMember = memberService.getMember(userId);
		if (loginMember == null) return "redirect:/loginForm";
		int userSeq = loginMember.getUserSeq();

		// 내 펫인지 체크 & 삭제
		boolean ok = petService.deletePet(petSeq, userSeq);
		if (!ok) {
			ra.addFlashAttribute("errorMsg", "삭제할 수 없습니다.");
			return "redirect:/";
		}

		ra.addFlashAttribute("successMsg", "반려동물이 삭제되었습니다.");
		return "redirect:/";
	}
	
	// 펫 수정
	@PostMapping("/updatePet/{id}")
	public String updatePet(@PathVariable("id") Integer petSeq,
									Authentication authentication,
									RedirectAttributes ra,
									@RequestParam("petName") String petName,
									@RequestParam("petType") String petType,
									@RequestParam("gender") String gender,
									@RequestParam("birthDate") String birthDate,
									@RequestParam(value="memo", required=false) String memo,
									@RequestParam(value="photo", required=false) MultipartFile photo ) throws Exception {

		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}

		String userId = authentication.getName();
		Member loginMember = memberService.getMember(userId);
		if (loginMember == null) return "redirect:/loginForm";
		int userSeq = loginMember.getUserSeq();

		// 내 펫인지 먼저 조회
		Pet origin = petService.getPetBySeq(petSeq, userSeq);
		if (origin == null) {
			ra.addFlashAttribute("errorMsg", "잘못된 접근입니다.");
			return "redirect:/";
		}

		// 이미지 처리 : 새 사진이 없으면 기존 유지
		String petImagePath = origin.getPetImage();
		if (photo != null && !photo.isEmpty()) {
			petImagePath = fileStorageService.savePetImage(photo);
		}

		// 나이 계산
		Integer petAge = null;
		LocalDate parsedBirth = null;
		if (birthDate != null && !birthDate.isBlank()) {
			parsedBirth = LocalDate.parse(birthDate);
			petAge = Period.between(parsedBirth, LocalDate.now()).getYears();
		}

		// 업데이트 객체 구성
		Pet pet = new Pet();
		pet.setPetSeq(petSeq);
		pet.setUserSeq(userSeq);
		pet.setPetName(petName.trim());
		pet.setPetType(petType.trim());
		pet.setGender(gender != null ? Gender.valueOf(gender) : Gender.U);
		pet.setBirthDate(parsedBirth);
		pet.setPetAge(petAge);
		pet.setMemo(memo);
		pet.setPetImage(petImagePath);

		petService.updatePet(pet);

		ra.addFlashAttribute("successMsg", "반려동물 정보가 수정되었습니다.");
		return "redirect:/detailPet/" + petSeq;
	}
	
	// 펫 수정 폼
	@GetMapping("/updatePet/{id}")
	public String updatePetForm(@PathVariable("id") Integer petSeq, Authentication authentication, Model model, RedirectAttributes ra) {

		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}

		String userId = authentication.getName();
		Member loginMember = memberService.getMember(userId);
		if (loginMember == null) return "redirect:/loginForm";

		Pet pet = petService.getPetBySeq(petSeq, loginMember.getUserSeq());
		if (pet == null) {
			ra.addFlashAttribute("errorMsg", "잘못된 정보입니다.");
			return "redirect:/";
		}

		// 수정폼에서 기존 값 바인딩
		model.addAttribute("pet", pet);

		return "pets/petEditForm";
	}
	
	// 펫 상세
	@GetMapping("/detailPet/{id}")
	public String petDetail(@PathVariable("id") Integer petSeq, Authentication authentication, Model model, RedirectAttributes ra) {

		// 로그인 체크
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}

		// 로그인 유저 조회
		String userId = authentication.getName();
		Member loginMember = memberService.getMember(userId);
		if (loginMember == null) return "redirect:/loginForm";
		
		// 펫 조회
		Pet pet = petService.getPetBySeq(petSeq, loginMember.getUserSeq());
		if (pet == null) {
			// 없는 펫일 경우
			ra.addFlashAttribute("errorMsg", "잘못된 정보입니다.");
			return "redirect:/";
		}

		// 내 펫인지 검증
		if (loginMember.getUserSeq() != pet.getUserSeq()) {
			// 남의 펫 접근 방지
			ra.addFlashAttribute("errorMsg", "잘못된 접근입니다.");
			return "redirect:/";
		}
		
		model.addAttribute("pet", pet);
		
		return "pets/petDetail";
	}
	
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
		pet.setGender(gender != null ? Gender.valueOf(gender) : Gender.U);
		pet.setBirthDate(birthDate != null && !birthDate.isBlank() ? LocalDate.parse(birthDate) : null);
		pet.setMemo(memo);

		// 저장
		petService.insertPet(pet);
		
		// 대쉬보드로 이동
		return "redirect:/";
	}
	
}
