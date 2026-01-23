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

		int safePage = Math.max(1, page);
		int safeSize = Math.min(15, Math.max(1, size));

		boolean nationwide = (q != null && !q.isBlank());
		String query = nationwide ? q.trim() : "동물병원";

		return nationwide
				? searchHospitalsNationwide(query, safePage, safeSize)
				: searchHospitalsNearby(lat, lng, query, safePage, safeSize);
	}
	
	
	private KakaoKeywordResponse searchHospitalsNationwide(String query, int page, int size) {
		// 검색어 있을 때는 동물병원을 같이 붙여서 전국 검색
		String q = query.contains("동물병원") ? query : (query + " 동물병원");
		
		List<Document> all = new ArrayList<>();
		for (int kakaoPage = 1; kakaoPage <= KAKAO_MAX_PAGE; kakaoPage++) {
			final int pageParam = kakaoPage;
	
			KakaoKeywordResponse raw = webClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/v2/local/search/keyword.json")
							.queryParam("query", q)
							.queryParam("page", pageParam)
							.queryParam("size", KAKAO_MAX_SIZE) // 최대 15로 빨리 수집
							.queryParam("sort", "accuracy")
							.queryParam("category_group_code", "HP8")
							.build())
					.header("Authorization", "KakaoAK " + kakaoRestKey)
					.retrieve()
					.bodyToMono(KakaoKeywordResponse.class)
					.block();
	
			if (raw == null) break;
	
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
			if (m != null && Boolean.TRUE.equals(m.getIs_end())) break;
		}
	
		// 표시 가능한 리스트만
		int total = all.size();
		int from = (page - 1) * size;
		int to = Math.min(from + size, total);
	
		List<Document> slice = (from >= total) ? List.of() : all.subList(from, to);
		boolean isEnd = to >= total;
	
		Meta meta = new Meta();
		meta.setTotal_count(total);	// total_count = 표시가능 개수
		meta.setPageable_count(total);
		meta.setIs_end(isEnd);
		meta.setMax_page((int)Math.ceil((double) total / size));
	
		KakaoKeywordResponse result = new KakaoKeywordResponse();
		result.setMeta(meta);
		result.setDocuments(slice);
	
		return result;
	}
	
	private KakaoKeywordResponse searchHospitalsNearby(double lat, double lng, String query, int page, int size) {

		final int clientPage = page; // 서버 페이지(6개씩) -> 나중에 slice 에서 사용
		final int clientSize = size;
		
		int radius = 5000;
		
		List<Document> all = new ArrayList<>();
		
		for (int kakaoPage = 1; kakaoPage <= KAKAO_MAX_PAGE; kakaoPage++) {

			final int pageParam = kakaoPage; // 람다용 final

			KakaoKeywordResponse raw = webClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/v2/local/search/keyword.json")
							.queryParam("query", query)
							.queryParam("y", lat)
							.queryParam("x", lng)
							.queryParam("radius", radius)
							.queryParam("sort", "distance")
							.queryParam("page", pageParam)
							.queryParam("size", KAKAO_MAX_SIZE)
							.queryParam("category_group_code", "HP8")
							.build())
					.header("Authorization", "KakaoAK " + kakaoRestKey)
					.retrieve()
					.bodyToMono(KakaoKeywordResponse.class)
					.block();

			if (raw == null) break;

			List<Document> docs = raw.getDocuments();
			if (docs != null && !docs.isEmpty()) {
				for (Document d : docs) {
					String cat = d.getCategory_name() == null ? "" : d.getCategory_name();
					if (cat.contains("동물병원")) all.add(d);
				}
			}

			Meta m = raw.getMeta();
			if (m != null && Boolean.TRUE.equals(m.getIs_end())) break;
		}

		// 서버 페이징 slice
		int total = all.size();
		int from = (clientPage - 1) * clientSize;
		int to = Math.min(from + clientSize, total);

		List<Document> slice = (from >= total) ? List.of() : all.subList(from, to);
		boolean isEnd = to >= total;

		Meta meta = new Meta();
		meta.setTotal_count(total);
		meta.setPageable_count(total);
		meta.setIs_end(isEnd);
		meta.setMax_page((int)Math.ceil((double)total / clientSize));
		
		KakaoKeywordResponse result = new KakaoKeywordResponse();
		result.setMeta(meta);
		result.setDocuments(slice);
		
		return result;
	}

	
}
