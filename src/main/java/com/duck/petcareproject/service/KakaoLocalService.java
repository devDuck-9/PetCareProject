package com.duck.petcareproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoLocalService {

	@Value("${kakao.rest.key}")
	private String kakaoRestKey;

	private final WebClient webClient = WebClient.builder().baseUrl("https://dapi.kakao.com").build();

	public String searchHospitals(double lat, double lng, String q, int page, int size) {
		String query = (q == null || q.isBlank()) ? "동물병원" : q.trim();

		// 반경 5km 고정
		int radius = 5000;

		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/v2/local/search/keyword.json")
						.queryParam("query", query)
						.queryParam("y", lat)	// 위도
						.queryParam("x", lng)	// 경도
						.queryParam("radius", radius) // 5000m
						.queryParam("sort", "distance")
						.queryParam("page", page)
						.queryParam("size", size)
						.build())
				.header("Authorization", "KakaoAK " + kakaoRestKey)
				.retrieve()
				.bodyToMono(String.class)
				.block();
	}
}
