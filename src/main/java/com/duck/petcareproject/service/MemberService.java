package com.duck.petcareproject.service;

import com.duck.petcareproject.domain.Member;

public interface MemberService {
	
	// 로그인 검증
	public Member getMember(String userId);
	
	// 아이디 중복확인
	public boolean existsByUserId(String userId);
	
	// 이름 중복확인
	boolean existsByUserName(String userName);

    // 회원가입
	public void addMember(Member member);
	
	// 회원 정보 수정
	public void editMember(Member member);

}
