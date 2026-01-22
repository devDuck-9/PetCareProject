package com.duck.petcareproject.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.MemberUpdateForm;
import com.duck.petcareproject.mapper.MemberMapper;
import com.duck.petcareproject.service.storage.FileStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImplement implements MemberService {
	
	private final MemberMapper memberMapper;
	private final PasswordEncoder passwordEncoder;
	private final FileStorageService fileStorageService;
	
	// 일정알림 오늘그만보기(유저)
	public void updateScheduleToastHideUntilByUser(String userId) {
		memberMapper.updateScheduleToastHideUntil(userId, LocalDate.now());
	};
	
	// 기존 비밀번호 일치여부
	public boolean isPasswordMatched(String userId, String rawPassword) {
		String hashed = memberMapper.selectPassword(userId);
		if (hashed == null) return false;
		return passwordEncoder.matches(rawPassword, hashed);
	}
	
	// 비밀번호 변경
	@Transactional
	public void changePassword(String userId, String currentRaw, String newRaw) {
			String hashed = memberMapper.selectPassword(userId);
			if (hashed == null) throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
	
			if (!passwordEncoder.matches(currentRaw, hashed)) {
					throw new IllegalArgumentException("비밀번호를 다시 입력해주세요.");
			}
	
			// 새 비번이 기존과 동일한지 방지
			if (passwordEncoder.matches(newRaw, hashed)) {
					throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
			}
	
			String newHashed = passwordEncoder.encode(newRaw);
			memberMapper.updatePassword(userId, newHashed);
	}

	// 회원 조회
	public Member findByUserId(String userId) {
		return memberMapper.findByUserId(userId);
	}
	
	// 회원 탈퇴
	@Transactional
	public void withdrawByUserId(String userId, String rawPassword) {
		Member member = memberMapper.findByUserId(userId);
		if (member == null) throw new IllegalArgumentException("로그인이 필요합니다.");

		// 이미 탈퇴 상태면 처리 막기
		if (member.getStatus() != null && member.getStatus().toString().equals("WITHDRAWN")) {
			throw new IllegalArgumentException("이미 탈퇴 처리된 계정입니다.");
		}

		// 비밀번호 검증
		if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 틀리셨습니다. 다시 입력해주세요.");
		}

		// 개인정보 마스킹
		String stamp = UUID.randomUUID().toString().substring(0, 8);

		String maskedName = "(탈퇴회원)";
		String maskedEmail = "withdrawn_" + member.getUserSeq() + "_" + stamp + "@deleted.local";
		String maskedMobile = "000-0000-0000";

		// 아이디 재사용 허용은 user_id도 바꾸기
		// String maskedUserId = "withdrawn_" + member.getUserSeq() + "_" + stamp;

		int updated = memberMapper.withdrawSoft(
				member.getUserSeq(),
				maskedName,
				maskedEmail,
				maskedMobile,
				null, null, null // zipcode/address1/address2 null 처리
				
				// (아이디 재사용 허용)
				// , maskedUserId
		);

		if (updated != 1) {
			throw new IllegalStateException("탈퇴 처리에 실패했습니다.");
		}
	}
	
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
		int result = memberMapper.insetMember(member);
		
		if (result != 1) {
			throw new RuntimeException("회원가입에 실패했습니다.");
		}
		
	}
	
	// 회원 정보수정
	public void editMember(MemberUpdateForm member) {
		
		int updated = memberMapper.updateMember(member);
		System.out.println("updated rows=" + updated);
		
		int result = memberMapper.updateMember(member);
		
		if (result != 1) {
			throw new RuntimeException("정보수정에 실패했습니다.");
		}
		
	}
	
	// 회원 프로필 사진 저장
	public String saveProfilePhoto(String userId, MultipartFile file) throws Exception {
		// 저장 (URL 생성까지 FileStorageService가 처리)
		String url = fileStorageService.saveProfileImage(file);
		
		if (url == null) {
			throw new IllegalArgumentException("파일이 비어있습니다.");
		}
	
		// DB 저장
		memberMapper.updateProfileImg(userId, url);
	
		// 캐시 방지
		return url + "?v=" + System.currentTimeMillis();
	}
	
	private String getExt(String original) {
			if (original == null) return ".png";
			int dot = original.lastIndexOf('.');
			return (dot > -1) ? original.substring(dot) : ".png";
	}
	
}
