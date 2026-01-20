package com.duck.petcareproject.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateForm {
	
	private String userId;
	private String userName;
	private Gender gender;
	private String mobile;
	private String zipcode;
	private String address1;
	private String address2;
	private String email;
	
	/* 이메일 _ 뷰 */
	private String emailId, emailDomain;
}
