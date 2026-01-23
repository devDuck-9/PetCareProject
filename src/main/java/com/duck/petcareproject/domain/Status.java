package com.duck.petcareproject.domain;

/**
 * 계정 상태
 * ACTIVE: 정상, INACTIVE: 탈퇴(처리), PURGED: 탈퇴(삭제)
 */
public enum Status {
	ACTIVE, WITHDRAWN, PURGED
}
