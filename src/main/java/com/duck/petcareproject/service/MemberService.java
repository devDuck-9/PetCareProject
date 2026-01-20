package com.duck.petcareproject.service;

import org.springframework.web.multipart.MultipartFile;

import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.MemberUpdateForm;

public interface MemberService {
	
	// 기존 비밀번호 일치 여부
	public boolean isPasswordMatched(String userId, String rawPassword);
	
	// 비밀번호 변경
	public void changePassword(String userId, String currentRaw, String newRaw);
	
	// 회원 프로필 사진 저장
	public String saveProfilePhoto(String userId, MultipartFile file) throws Exception;
	
	// 회원 조회
	public Member findByUserId(String userId);
	
	// 회원 탈퇴
	public void withdrawByUserId(String userId, String rawPassword);
	
	// 로그인 검증
	public Member getMember(String userId);
	
	// 아이디 중복확인
	public boolean existsByUserId(String userId);
	
	// 이름 중복확인
	boolean existsByUserName(String userName);

    // 회원가입
	public void addMember(Member member);
	
	// 회원 정보 수정
	public void editMember(MemberUpdateForm member);

}
