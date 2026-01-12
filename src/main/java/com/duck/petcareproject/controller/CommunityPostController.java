package com.duck.petcareproject.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.duck.petcareproject.domain.CommunityPost;
import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.service.CommunityPostService;
import com.duck.petcareproject.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CommunityPostController {
	
	private final CommunityPostService communityPostService;
	private final MemberService memberService;
	
	// 게시글 등록
	@PostMapping("/insertPost")
	public String insertPost( Authentication auth,
								@RequestParam("category") String category,
								@RequestParam("title") String title,
								@RequestParam("content") String content) {
		
		// 로그인 체크(안전)
		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}

		Member member = memberService.getMember(auth.getName());
		if (member == null) return "redirect:/loginForm";

		CommunityPost post = new CommunityPost();
		post.setUserSeq(member.getUserSeq());
		post.setCategory(category);
		post.setTitle(title);
		post.setContent(content);

		communityPostService.insertPost(post);
		
		// *수정 필요
		return "redirect:/";
		
		// 등록 후 커뮤니티 목록으로 이동
//		return "redirect:/community?category=" + category;
	}

}
