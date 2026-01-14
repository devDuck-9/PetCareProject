package com.duck.petcareproject.domain;

import java.util.Arrays;

/**
 * 카테고리
 * FREE: 자유게시판,
 * GUIDE: 펫케어소식(유저는 읽기만 가능), 
 * INFO: 정보공유, 
 * BOAST: 자랑하기, 
 * QNA: 질의응답
 * 그외: 전체글
 */
public enum Category {
	
	FREE("free", "자유 게시판"),
	GUIDE("guide", "공지사항"),
	INFO("info", "정보 공유"),
	BOAST("boast", "자랑 하기"),
	QNA("qna", "질문 & 답변");

	private final String slug;
	private final String label;

	Category(String slug, String label) {
		this.slug = slug;
		this.label = label;
	}

	public String getSlug() { return slug; }
	public String getLabel() { return label; }

	public static Category fromSlug(String slug) {
		if (slug == null || slug.isBlank()) return null;
		final String s = slug.trim().toLowerCase();
		return Arrays.stream(values())
					.filter(c -> c.slug.equals(s))
					.findFirst()
					.orElse(null);
	}
	
}
