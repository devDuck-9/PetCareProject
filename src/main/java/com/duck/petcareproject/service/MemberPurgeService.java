package com.duck.petcareproject.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberPurgeService {
	
	private final JdbcTemplate jdbcTemplate;

	private static final int MAX_LOOPS = 200; // 안전장치

	/**
	 * 탈퇴 후 7일 지난 회원을 실제 삭제 (CASCADE로 연관 데이터도 같이 삭제)할 시 커뮤니티 품질 붕괴위험
	 * 개인정보만 삭제 + 기존 아이디 재사용 가능
	 * @return 삭제된 member 수
	 * 
	 * 아이디 NULL 로 받지 않는 이유: 식별,관리,안정성이 깨진다. 시스템무결성을 유지하기위해 의미없는 고유값으로 치환한다.
	 * 고정접두어('purged_')붙혀서 파기된계정 의미 + 해당 회원의 고유 PK 값(user_seq) + 가독성을 위한 구분자 + 랜덤문자열 8개
	 * UUID() → 랜덤 문자열
	 * LEFT(..., 8) → 앞 8글자만 사용
	 */
	public int purgeWithdrawnMembers() {
			int totalPurged = 0;

			for (int i = 0; i < MAX_LOOPS; i++) {
				int updated = jdbcTemplate.update("""
						UPDATE member
							SET
								status = 'PURGED',
								user_id = CONCAT('purged_', user_seq, '_', LEFT(UUID(), 8)),
								user_name = '(탈퇴회원)',
								email = NULL,
								mobile = NULL,
								zipcode = NULL,
								address1 = NULL,
								address2 = NULL,
								profile_img = NULL,
								updated_at = NOW()
							WHERE status = 'WITHDRAWN'
								AND withdrawn_at IS NOT NULL
								AND withdrawn_at < NOW() - INTERVAL 7 DAY
							LIMIT 1000
					""");

				totalPurged += updated;

					if (updated == 0) break; // 더 이상 수정할 대상 없음
			}

			log.info("purgeWithdrawnMembers finished. totalPurged={}", totalPurged);
			return totalPurged;
	}

}
