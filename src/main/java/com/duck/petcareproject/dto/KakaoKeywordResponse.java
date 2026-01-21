package com.duck.petcareproject.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoKeywordResponse {

	private Meta meta;
	private List<Document> documents;

	@Getter @Setter
	public static class Meta {
		private Integer total_count;
		private Integer pageable_count;
		private Boolean is_end;
	}

	@Getter @Setter
	public static class Document {
		private String id;
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

