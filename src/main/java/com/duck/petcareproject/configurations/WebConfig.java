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
		
		// 회원가입 폼
		registry.addViewController("/joinForm").setViewName("member/memberJoinForm");
		
	}
	
	@Value("${app.upload.dir}")
	private String uploadDir;
	@Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/files/**")
		.addResourceLocations("file:" + uploadDir + "/")
		.setCachePeriod(1);
	}
	 
	
	
	/*
	 * @Override public void addInterceptors(InterceptorRegistry registry) {
	 * registry.addInterceptor(new LoginCheckInterceptor())
	 * .addPathPatterns("/write*", "/update*");
	 * 
	 * //.excludePathPatterns("/boardList"); }
	 */
	
	
	
}
