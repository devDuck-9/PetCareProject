package com.duck.petcareproject.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.duck.petcareproject.interceptor.LoginCheckInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer{
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		
		// 게시글 등록 폼
		registry.addViewController("/addPostForm").setViewName("community/addPostForm");
		
		// 반려동물 등록 폼
		registry.addViewController("/addPetForm").setViewName("pets/addPetForm");
		
		// 회원가입 폼은 Controller 에서 모델을 주입해야 검증 메시지를 표시할 수 있으므로 제거
//		registry.addViewController("/joinForm").setViewName("member/memberJoinForm");
		
	}
	
	// 파일
	@Value("${app.upload.dir}")
	private String uploadDir;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/files/**")
		.addResourceLocations("file:" + uploadDir + "/")
		.setCachePeriod(1);
	}
	
}
