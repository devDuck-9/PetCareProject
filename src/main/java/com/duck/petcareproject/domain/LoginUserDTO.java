package com.duck.petcareproject.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginUserDTO {
	
		private final int userSeq;
		private final String userId;
		private final String userName;
		private final String role;
		
}
