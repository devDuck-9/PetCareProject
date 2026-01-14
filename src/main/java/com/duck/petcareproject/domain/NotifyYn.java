package com.duck.petcareproject.domain;

public enum NotifyYn {
	Y("동의"),
	N("미동의");

	private final String label;

	NotifyYn(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
