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
	 * 탈퇴 후 7일 지난 회원을 실제 삭제 (CASCADE로 연관 데이터도 같이 삭제)
	 * @return 삭제된 member 수
	 */
	public int purgeWithdrawnMembers() {
			int totalDeleted = 0;

			for (int i = 0; i < MAX_LOOPS; i++) {
				int deleted = jdbcTemplate.update("""
						DELETE FROM `member`
						WHERE status = 'WITHDRAWN'
							AND withdrawn_at IS NOT NULL
							AND withdrawn_at < NOW() - INTERVAL 7 DAY
							AND NOT EXISTS (SELECT 1 FROM guide g WHERE g.user_seq = `member`.user_seq)
						LIMIT 1000
					""");

					totalDeleted += deleted;

					if (deleted == 0) break; // 더 이상 삭제할 대상 없음
			}

			log.info("purgeWithdrawnMembers finished. totalDeleted={}", totalDeleted);
			return totalDeleted;
	}

}
