package com.duck.petcareproject.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.NotifyYn;
import com.duck.petcareproject.domain.Pet;
import com.duck.petcareproject.domain.Schedule;
import com.duck.petcareproject.domain.ScheduleStatus;
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
	
	// 일정 수정
	@RequestMapping("/updateSchedule/{id}")
	public String updateSchedule(@PathVariable("id") int scheduleSeq, Authentication authentication, RedirectAttributes ra,
								 @RequestParam("title") String title,
								 @RequestParam("scheduleDate") String scheduleDate,
								 @RequestParam(value = "memo", required = false) String memo,
								 @RequestParam(value = "notifyYn", required = false, defaultValue = "N") NotifyYn notifyYn,
								 @RequestParam(value = "status", required = false) ScheduleStatus status) {
		
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}

		String userId = authentication.getName();
		Member loginMember = memberService.getMember(userId);
		
		if (loginMember == null) return "redirect:/loginForm";
		int userSeq = loginMember.getUserSeq();
		
		// 내 일정인지 확인
		Schedule origin = scheduleService.selectScheduleByIdAndUser(scheduleSeq, userSeq);
		if (origin == null) {
			ra.addFlashAttribute("errorMsg", "잘못된 접근입니다.");
			return "redirect:/";
		}

		// schedule_time 만들기 (날짜만 받으니까 00:00:00)
		LocalDate d = LocalDate.parse(scheduleDate);
		LocalDateTime scheduleTime = d.atStartOfDay();

		// 객체 구성
		Schedule sch = new Schedule();
		sch.setScheduleSeq(scheduleSeq);
		sch.setUserSeq(userSeq);
		sch.setTitle(title.trim());
		sch.setScheduleTime(scheduleTime);
		sch.setMemo(memo != null ? memo.trim() : null);
		sch.setNotifyEnabled(notifyYn);

		// status 를 폼에서 안 받으면 기존값 유지
		sch.setStatus(status != null ? status : origin.getStatus());

		// 수정
		scheduleService.updateScheduleBySeqAndUser(sch);
		ra.addFlashAttribute("successMsg", "일정이 수정되었습니다.");

		return "redirect:/detailSchedule/" + scheduleSeq;
	}
	
	// 일정 수정 폼
	@GetMapping("/updateSchedule/{id}")
	public String udateScheduleForm(@PathVariable("id") int scheduleSeq, Authentication authentication, Model model, RedirectAttributes ra) {

		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}

		String userId = authentication.getName();
		Member loginMember = memberService.getMember(userId);
		if (loginMember == null) return "redirect:/loginForm";
		
		int userSeq = loginMember.getUserSeq();

		Schedule sch = scheduleService.selectScheduleByIdAndUser(scheduleSeq, userSeq);
		if (sch == null) {
			ra.addFlashAttribute("errorMsg", "잘못된 정보입니다.");
			return "redirect:/";
		}

		Pet pet = petService.getPetBySeq(sch.getPetSeq(), userSeq);
		if (pet != null) model.addAttribute("petName", pet.getPetName());
		model.addAttribute("schedule", sch);
		
		return "schedule/scheduleEditForm";
	}

	// 일정 삭제
	@PostMapping("/deleteSchedule/{id}")
	public String deleteSchedule(@PathVariable("id") int scheduleSeq, Authentication authentication, RedirectAttributes ra) {
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}

		String userId = authentication.getName();
		Member loginMember = memberService.getMember(userId);

		if (loginMember == null) return "redirect:/loginForm";
		int userSeq = loginMember.getUserSeq();
		
		// 내 일정인지 확인
		Schedule sch = scheduleService.selectScheduleByIdAndUser(scheduleSeq, userSeq);
		if (sch == null) {
			ra.addFlashAttribute("errorMsg", "삭제할 수 없습니다.");
			return "redirect:/";
		}

		// 삭제
		scheduleService.deleteScheduleBySeqAndUser(scheduleSeq, userSeq);
		ra.addFlashAttribute("successMsg", "일정이 삭제되었습니다.");
		
		return "redirect:/";
	}

	// 선택한 일정 조회
	@GetMapping("/detailSchedule/{id}")
	public String detailSchedule(@PathVariable("id") int scheduleSeq, Authentication authentication, Model model, RedirectAttributes ra) {
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}

		String userId = authentication.getName();
		Member loginMember = memberService.getMember(userId);

		if (loginMember == null) return "redirect:/loginForm";
		int userSeq = loginMember.getUserSeq();

		Schedule sch = scheduleService.selectScheduleByIdAndUser(scheduleSeq, userSeq);
		if (sch == null) {
			ra.addFlashAttribute("errorMsg", "잘못된 정보입니다.");
			return "redirect:/";
		}
		
		// petName 표시용
		Pet pet = petService.getPetBySeq(sch.getPetSeq(), userSeq);
		if (pet != null) model.addAttribute("petName", pet.getPetName());
		
		model.addAttribute("schedule", sch);
		return "schedule/scheduleDetail";
	}
	
	// 일정 등록
	@PostMapping("/insertSchedule")
	public String insertSchedule(Authentication authentication,
								@RequestParam("petSeq") int petSeq,
								@RequestParam("title") String title,
								@RequestParam("scheduleDate") String scheduleDate, // hidden (yyyy-MM-dd)
								@RequestParam(value = "memo", required = false) String memo,
								@RequestParam(value = "notifyYn", required = false, defaultValue = "N") NotifyYn notifyYn
					) throws Exception {
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}
		
		String userId = authentication.getName();
		Member loginMember = memberService.getMember(userId);

		if (loginMember == null) return "redirect:/loginForm";
		int userSeq = loginMember.getUserSeq();
		
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
		sch.setNotifyEnabled(notifyYn);
		
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
