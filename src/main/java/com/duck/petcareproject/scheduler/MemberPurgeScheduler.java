package com.duck.petcareproject.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.duck.petcareproject.service.MemberPurgeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberPurgeScheduler {
	
	private final MemberPurgeService memberPurgeService;

	// 매일 03:00 (서버 TZ가 KST가 아니면 zone 지정 권장)
	@Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
	public void purgeWithdrawnMembersDaily() {
		int total = memberPurgeService.purgeWithdrawnMembers();
		log.info("purgeWithdrawnMembersDaily done. deleted={}", total);
	}
	
	
//	// 테스트 1분마다 실행
//	@Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
//	public void purgeWithdrawnMembersDaily() {
//			log.info("scheduler triggered");
//			int total = memberPurgeService.purgeWithdrawnMembers();
//			log.info("purgeWithdrawnMembersDaily done. deleted={}", total);
//	}
	
}
