package com.duck.petcareproject.domain;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {
	private int userSeq;

	@NotBlank(message = "아이디는 필수입니다.")
	@Size(min = 2, max = 50, message = "아이디는 2~50자로 입력해주세요.")
	private String userId;

	@NotBlank(message = "이름은 필수입니다.")
	@Size(min = 2, max = 50, message = "이름은 2~50자로 입력해주세요.")
	private String userName;

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 4, max = 255, message = "비밀번호는 4자 이상으로 입력해주세요.")
	private String password;

	private String email;

	// enum 적용
	private Gender gender; // M/F/U

	@NotBlank(message = "휴대폰 번호는 필수입니다.")
	@Pattern(
		regexp = "^01[0-9]-\\d{3,4}-\\d{4}$",
		message = "휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
	)
	private String mobile;

	private String zipcode;
	private String address1;
	private String address2;
	
	private String profileImg;
	
	private LocalDateTime createdAt, updatedAt;

	// enum 적용
	private Role role;
	private Status status;

	/* 이메일 _ 뷰 */
	private String emailId, emailDomain;
}
