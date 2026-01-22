package com.duck.petcareproject.mapper;

import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.MemberUpdateForm;

@Mapper
public interface MemberMapper {
	
	// 알림 오늘은 그만보기(유저) 저장
	void updateScheduleToastHideUntil(@Param("userId") String userId, @Param("hideUntil") LocalDate hideUntil);
	
	// userId의 비밀번호 조회
	String selectPassword(String userId);
	// 비밀번호 변경
	int updatePassword(@Param("userId") String userId, @Param("password") String password);
	
	// 회원 조회
	Member findByUserId(@Param("userId") String userId);
	
	// 회원 프로필 이미지 수정
	int updateProfileImg(@Param("userId") String userId, @Param("profileImg") String profileImg);
	
	// 회원 탈퇴
	int withdrawSoft(@Param("userSeq") int userSeq,
					 @Param("maskedName") String maskedName,
					 @Param("maskedEmail") String maskedEmail,
					 @Param("maskedMobile") String maskedMobile,
					 @Param("maskedZip") String maskedZip,
					 @Param("maskedAddr1") String maskedAddr1,
					 @Param("maskedAddr2") String maskedAddr2
					 // 아이디 재사용 허용
					 // @Param("maskedUserId") String maskedUserId
	);
	
	// userId 의 회원 정보
	public Member selectMember(@Param("userId") String userId);
	
	// userId 의 비밀번호 조회
	public String isPassMatched(@Param("userId") String userId);
	
	// 아이디 중복확인
	public int existsByUserId(@Param("userId") String userId);
	
	// 이름 중복확인
	int existsByUserName(@Param("userName") String userName);
	
	// 회원가입
	public int insetMember(Member member);
	
	// 회원 정보 수정
	public int updateMember(MemberUpdateForm member);
	
	
}
