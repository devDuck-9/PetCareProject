package com.duck.petcareproject.domain;

/**
 * 성별 코드
 * M: 남성, F: 여성, U: 선택안함
 */
public enum Gender {
	M("남", "남아"),
	F("여", "여아"),
	U("선택안함", "선택없음");

	private final String memberLabel;
	private final String petLabel;

	Gender(String memberLabel, String petLabel) {
			this.memberLabel = memberLabel;
			this.petLabel = petLabel;
	}

	public String memberLabel() {
			return memberLabel;
	}

	public String petLabel() {
			return petLabel;
	}
}
