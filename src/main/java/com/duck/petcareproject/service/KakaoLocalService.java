package com.duck.petcareproject.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.duck.petcareproject.dto.KakaoKeywordResponse;
import com.duck.petcareproject.dto.KakaoKeywordResponse.Document;
import com.duck.petcareproject.dto.KakaoKeywordResponse.Meta;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoLocalService {

	@Value("${kakao.rest.key}")
	private String kakaoRestKey;

	private final WebClient webClient = WebClient.builder().baseUrl("https://dapi.kakao.com").build();
	
	// 카카오 제한
	private static final int KAKAO_MAX_PAGE = 45;
	private static final int KAKAO_MAX_SIZE = 15;

	public KakaoKeywordResponse searchHospitals(double lat, double lng, String q, int page, int size) {
			
		if (page < 1) page = 1;
			if (size < 1) size = 6;
			if (size > 15) size = 15;
	
			String query = (q == null || q.isBlank()) ? "동물병원" : q.trim();
	
			int radius = 5000;
	
			// 1) 끝까지 긁어서 "동물병원" 결과 전체를 확보
			List<Document> all = new ArrayList<>();
	
			for (int kakaoPage = 1; kakaoPage <= KAKAO_MAX_PAGE; kakaoPage++) {
				
				final int pageParam = kakaoPage; // 람다용 final 변수
	
				KakaoKeywordResponse raw = webClient.get()
												.uri(uriBuilder -> uriBuilder
													.path("/v2/local/search/keyword.json")
													.queryParam("query", query)
													.queryParam("y", lat)
													.queryParam("x", lng)
													.queryParam("radius", radius)
													.queryParam("sort", "distance")
													.queryParam("page", pageParam)
													.queryParam("size", KAKAO_MAX_SIZE) // 최대치로 빠르게 수집
													.queryParam("category_group_code", "HP8") // 병원 카테고리
													.build())
												.header("Authorization", "KakaoAK " + kakaoRestKey)
												.retrieve()
												.bodyToMono(KakaoKeywordResponse.class)
												.block();
					
					if (raw == null) break;
					
					// HP8인데도 섞이면 안전하게 한번 더 필터
					List<Document> docs = raw.getDocuments();
					if (docs != null && !docs.isEmpty()) {
							for (Document d : docs) {
									String cat = d.getCategory_name() == null ? "" : d.getCategory_name();
									if (cat.contains("동물병원")) {
											all.add(d);
									}
							}
					}
	
					Meta m = raw.getMeta();
					if (m != null && Boolean.TRUE.equals(m.getIs_end())) {
							break;
					}
			}
	
			// 2) 서버 페이징(6개씩) -> all에서 잘라서 내려줌
			int total = all.size();
			int from = (page - 1) * size;
			int to = Math.min(from + size, total);
	
			List<Document> slice;
			if (from >= total) {
					slice = List.of();
			} else {
					slice = all.subList(from, to);
			}
	
			boolean isEnd = to >= total;
	
			// 카카오 meta 형태 맞춰서 응답
			Meta meta = new Meta();
			meta.setTotal_count(total);
			meta.setPageable_count(total); 
			meta.setIs_end(isEnd);
	
			KakaoKeywordResponse result = new KakaoKeywordResponse();
			result.setMeta(meta);
			result.setDocuments(slice);
	
			return result;
	}
	
}
