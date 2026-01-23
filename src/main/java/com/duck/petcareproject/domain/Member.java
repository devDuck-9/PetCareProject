package com.duck.petcareproject.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {
	private int userSeq;
	
	// userId: 영문/숫자/_ 만, 4~20, 공백 금지
	@NotBlank(message = "아이디는 필수입니다.")
	@Size(min = 4, max = 20, message = "아이디는 4~20자로 입력해주세요.")
	@Pattern(regexp = "^[A-Za-z0-9_]{4,20}$", message = "아이디는 영문/숫자/_ 만 사용 가능합니다.")
	private String userId;
	
	// userName: 2~12, trim, 공백 금지, 한글/영문/숫자/_ 만
	@NotBlank(message = "이름(닉네임)은 필수입니다.")
	@Size(min = 2, max = 12, message = "이름(닉네임)은 2~12자로 입력해주세요.")
	@Pattern(regexp = "^[가-힣A-Za-z0-9_]{2,12}$", message = "이름(닉네임)은 한글/영문/숫자/_ 만 가능하며 공백은 사용할 수 없습니다.")
	private String userName;
	
	// password: 8~20, 공백 금지 + 영문/숫자/특수(일반적인)만 허용
	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요.")
	@Pattern(regexp = "^(?=\\S+$)[A-Za-z\\d~`!@#$%^&*()_+\\-={}\\[\\]|\\\\:;\"'<>,.?/]{8,20}$", message = "비밀번호는 공백 없이 영문/숫자/기본 특수문자만 사용 가능합니다.")
	private String password;
	
	// 최종 이메일 검증은 아래 isEmailValid
	private String email;

	// enum 적용
	private Gender gender; // M/F/U

	@NotBlank(message = "휴대폰 번호는 필수입니다.")
	@Pattern(regexp = "^01[0-9]-\\d{3,4}-\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
	private String mobile;
	
	private String zipcode;
	private String address1;
	
	// address2: 길이 제한 + 저장 전에 escape/strip(컨트롤러에서 처리)
	@Size(max = 100, message = "상세주소는 100자 이내로 입력해주세요.")
	private String address2;
	
	private String profileImg;
	
	private LocalDateTime createdAt, updatedAt;
	
	//일정 알림 토스트 숨김 날짜 (오늘 포함)
	private LocalDate scheduleToastHideUntil;
	
	// enum 적용
	private Role role;
	private Status status;

	// 이메일 입력용
	@NotBlank(message = "이메일을 입력해주세요.")
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+$", message = "이메일 아이디는 영문/숫자/._%+- 만 가능합니다.")
	private String emailId;
	
	@NotBlank(message = "이메일 도메인을 입력해주세요.")
	@Pattern(regexp = "^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "이메일 도메인 형식이 올바르지 않습니다. (예: gmail.com)")
	private String emailDomain;
	
	/*	--------------------
	 *	정책성 검증(커스텀/컨트롤러에서 쓸 메서드)
	 *	--------------------
	 */
	public boolean isPasswordPolicyValid() {
		if (password == null) return false;
		
		// 특수: 영문/숫자/공백 제외
		boolean hasLetter = password.matches(".*[A-Za-z].*");	// 영문 포함 여부
		boolean hasDigit	= password.matches(".*\\d.*");	// 숫자 포함 여부
		boolean hasSpecial = password.matches(".*[^A-Za-z0-9\\s].*");	// 영문/숫자/공백 제외 (영문·숫자·공백이 아닌 문자) = 특수문자
		
		int kinds = 0;
		if (hasLetter) kinds++;
		if (hasDigit) kinds++;
		if (hasSpecial) kinds++;

		return kinds >= 2;
	}
	
	public boolean isEmailValid() {
		if (emailId == null || emailDomain == null) return false;

		String id = emailId.trim();
		String domain = emailDomain.trim();
		if (id.isEmpty() || domain.isEmpty()) return false;

		String combined = id + "@" + domain;

		// email 정규식
		return combined.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	}
	
	
	/*	--------------------
	 *	trim 을 setter 에서 처리
	 *	--------------------
	 */
	 public void setUserId(String userId) {
		this.userId = (userId == null) ? null : userId.trim();
	}

	public void setUserName(String userName) {
		this.userName = (userName == null) ? null : userName.trim();
	}

	public void setEmailId(String emailId) {
		this.emailId = (emailId == null) ? null : emailId.trim();
	}

	public void setEmailDomain(String emailDomain) {
		this.emailDomain = (emailDomain == null) ? null : emailDomain.trim();
	}

	public void setAddress2(String address2) {
		this.address2 = (address2 == null) ? null : address2.trim();
	}
	
	
}
