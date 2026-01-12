package com.duck.petcareproject.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.Pet;
import com.duck.petcareproject.domain.Schedule;
import com.duck.petcareproject.service.MemberService;
import com.duck.petcareproject.service.PetService;
import com.duck.petcareproject.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ScheduleController {
	
	private final ScheduleService scheduleService;
	private final PetService petService;
	private final MemberService memberService;
	
	// 일정 등록
	@PostMapping("/insertSchedule")
	public String insertSchedule(Authentication authentication,
								@RequestParam("petSeq") int petSeq,
								@RequestParam("title") String title,
								@RequestParam("scheduleDate") String scheduleDate, // hidden (yyyy-MM-dd)
								@RequestParam(value = "memo", required = false) String memo,
								@RequestParam(value = "alarm", required = false, defaultValue = "N") String alarm
					) throws Exception {
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}
		
		String userId = authentication.getName();
		Member loginMember = memberService.getMember(userId);
		Integer userSeq = (loginMember != null ? loginMember.getUserSeq() : null);
		if (userSeq == null) return "redirect:/loginForm";
		
		//  petSeq가 내 펫인지 검증
		Pet pet = petService.getPetBySeq(petSeq, userSeq);
		if (pet == null) {
			// 남의 펫으로 일정 등록 시도 or 잘못된 petSeq
			return "redirect:/";
		}
		
		// schedule_time 만들기 (날짜만 받으니 00:00:00)
		LocalDate d = LocalDate.parse(scheduleDate);
		LocalDateTime scheduleTime = d.atStartOfDay();
		
		// 스케줄 객체 생성
		Schedule sch = new Schedule();
		sch.setPetSeq(petSeq);
		sch.setTitle(title.trim());
		sch.setScheduleTime(scheduleTime);
		sch.setMemo(memo != null ? memo.trim() : null);
		// alarm 은 DB 컬럼 추가 예정
		// sch.setAlarm(alarm);
		
		// 저장
		scheduleService.insertSchedule(sch);
		
		// 저장 후 대시보드 이동
		return "redirect:/";
	}
	
	// 일정 등록 폼
	@GetMapping("/addScheduleForm")
	public String addScheduleForm(@RequestParam("petSeq") int petSeq, Model model, Authentication authentication) {
		
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}
	
		String userId = authentication.getName();
		Member member = memberService.getMember(userId);
		if (member == null) return "redirect:/loginForm";
	
		int userSeq = member.getUserSeq();
		
		// 사용자의 펫 조회
		 Pet pet = petService.getPetBySeq(petSeq, userSeq);
		 
		 // 예외 처리
		 if (pet == null) {
			 return "redirect:/";
		 }
		 
		// 화면에 표시할 이름
		model.addAttribute("petName", pet.getPetName());
		// 등록 시 필요한 petSeq
		model.addAttribute("petSeq", petSeq);
		
		return "schedule/addScheduleForm";
	}

}
