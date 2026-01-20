package com.duck.petcareproject.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberPasswordForm {
	
	// 컨트롤러에서 auth 로 세팅할거라 화면에는 안 보여도 됨
	private String userId;

	@NotBlank(message = "기존 비밀번호를 입력해주세요.")
	private String currentPassword;

	@NotBlank(message = "새 비밀번호를 입력해주세요.")
	@Size(min = 4, max = 255, message = "비밀번호는 4자 이상으로 입력해주세요.")
	private String newPassword;

	@NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
	private String confirmPassword;

}
