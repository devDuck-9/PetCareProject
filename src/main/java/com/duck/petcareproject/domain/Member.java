package com.duck.petcareproject.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {
	private int userSeq;
	private String userId;
	private String userName;
	private String password;
	private String email;
	private String gender;
	private String mobile;
	private String zipcode;
	private String address1;
	private String address2;
	private LocalDateTime createdAt, updatedAt;
    private String role, status;
    
    /* 이메일 _ 뷰 */
    private String emailId, emailDomain;
    

}
