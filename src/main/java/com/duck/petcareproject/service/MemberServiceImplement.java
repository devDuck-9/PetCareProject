package com.duck.petcareproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImplement implements MemberService {
	
	private final MemberMapper memberMapper;
	private final PasswordEncoder passwordEncoder;
	
	//로그인 검증
	public Member getMember(String userId) {
		return memberMapper.selectMember(userId);
	}
	
	// 아이디 중복확인
	public boolean existsByUserId(String userId) {
		return memberMapper.existsByUserId(userId) > 0;
	}
	
	// 이름 중복확인
	public boolean existsByUserName(String userName) {
		return memberMapper.existsByUserName(userName) > 0;
	}
	
	// 회원가입
	public void addMember(Member member) {
		// 비밀번호 암호화
		member.setPassword(passwordEncoder.encode(member.getPassword()));
		memberMapper.insetMember(member);
	}
	
}
