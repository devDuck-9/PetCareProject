package com.duck.petcareproject.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
		@Bean
		public PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}
		
		@Bean
		public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

				http
					// CSRF 비활성화 (개발 단계 편의용)
					.csrf(csrf -> csrf.disable())
	
					// iframe 사용 허용 (개발용, 필요 시)
					.headers(headers ->
							headers.frameOptions(frame -> frame.disable())
					)
	
					// 권한 설정
					.authorizeHttpRequests(auth -> auth
							// 비로그인 시 접근가능	
							.requestMatchers(
										"/", "/joinForm","/loginForm",
										"/joinResult","/loginResult",
										"/api/**","/error",
										"/css/**", "/js/**", "/images/**", "/bootstrap/**", "/fonts/**", "/files/**").permitAll()
								.anyRequest().authenticated()
						)
	
					// 폼 로그인
					.formLogin(form -> form
							.loginPage("/loginForm")
							.loginProcessingUrl("/loginResult")
							.usernameParameter("userId")
							.passwordParameter("password")
//							.failureUrl("/loginForm?error")
							.failureHandler((request, response, exception) -> {
								exception.printStackTrace();
								System.out.println("FAILURE HANDLER HIT: " + exception.getClass().getSimpleName());
								request.getSession().setAttribute("LOGIN_ERROR_MSG", "아이디 또는 비밀번호가 올바르지 않습니다.");
								response.sendRedirect("/loginForm");
							})
//							.defaultSuccessUrl("/", true)
							.successHandler((request, response, authentication) -> {
								// 성공 시 혹시 남아있을 에러 메시지 제거
								request.getSession().removeAttribute("LOGIN_ERROR_MSG");
								response.sendRedirect("/");
							})
							.permitAll()
					)
	
					// 로그아웃
					.logout(logout -> logout
							.logoutUrl("/logout")
							.logoutSuccessUrl("/")
							.invalidateHttpSession(true) // 기존 세션 삭제
					);

			return http.build();
		}
}

