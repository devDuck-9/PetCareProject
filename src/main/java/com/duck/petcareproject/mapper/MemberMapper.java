package com.duck.petcareproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.duck.petcareproject.domain.Member;

@Mapper
public interface MemberMapper {
	
	// userId 의 회원 정보
	public Member selectMember(@Param("userId") String userId);
	
	// userId 의 비밀번호 조회
	public String isPassMatched(@Param("userId") String userId);
	
	// 아이디 중복확인
	public int existsByUserId(@Param("userId") String userId);
	
	// 이름 중복확인
	int existsByUserName(@Param("userName") String userName);
	
	// 회원가입
	public void insetMember(Member member);
	
	
}
