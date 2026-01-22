package com.duck.petcareproject.security;

import org.springframework.security.core.AuthenticationException;

public class WithdrawnAccountException extends AuthenticationException {
	
	public WithdrawnAccountException(String msg) {
		super(msg);
	}

}
