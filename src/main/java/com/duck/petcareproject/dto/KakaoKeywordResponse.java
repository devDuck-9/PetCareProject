package com.duck.petcareproject.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

//카카오 키워드 장소 검색 API 응답(JSON)
//API 전체 응답을 한 번에 받는 컨테이너

@Getter
@Setter
public class KakaoKeywordResponse {
	
	private Meta meta;
	private List<Document> documents;

	@Getter
	@Setter
	public static class Meta {	// 내부클래스 Meta: 검색 결과 요약 정보
		// 카카오 API는 값이 null일 수도 있으므로 Integer / Boolean 사용
		private Integer total_count;	// 검색된 전체 결과 수
		private Integer pageable_count;	// 실제 페이지네이션 가능한 결과 수
		private Boolean is_end;	// 현재 페이지가 마지막인지 여부
		private int max_page;
	}

	@Getter
	@Setter
	public static class Document {	// 내부클래스 Document: 병원(장소) 1개 정보
		private String id;	// 카카오 장소 고유 ID
		private String place_name;
		private String phone;
		private String address_name;
		private String road_address_name;
		private String place_url;
		private String distance;
		private String x;
		private String y;
		private String category_name;
	}
}

