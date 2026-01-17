package com.duck.petcareproject.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.duck.petcareproject.domain.CommunityComment;
import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.service.CommunityCommentService;
import com.duck.petcareproject.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CommunityCommentController {

	private final CommunityCommentService communityCommentService;
	private final MemberService memberService;

	// 댓글 등록
	@PostMapping("/insertComment")
	public String insertComment(Authentication auth, RedirectAttributes ra, @RequestParam("postSeq") int postSeq, @RequestParam("content") String content) {

		if (auth == null || auth instanceof AnonymousAuthenticationToken) return "redirect:/loginForm";

		Member member = memberService.getMember(auth.getName());
		if (member == null) return "redirect:/loginForm";

		String text = (content == null ? "" : content.trim());
		if (text.isBlank()) {
			ra.addFlashAttribute("errorMsg", "댓글 내용을 입력해주세요.");
			return "redirect:/detailPost/" + postSeq;
		}
		
		CommunityComment c = new CommunityComment();
		c.setPostSeq(postSeq);
		c.setUserSeq(member.getUserSeq());
		c.setContent(text);
		
		communityCommentService.insertComment(c);
		ra.addFlashAttribute("successMsg", "댓글이 등록되었습니다.");
		return "redirect:/detailPost/" + postSeq;
	}
	
	// 댓글 수정
	@PostMapping("/updateComment")
	public String updateComment(Authentication auth, RedirectAttributes ra, @RequestParam("commentSeq") int commentSeq, @RequestParam("content") String content) {
		
		if (auth == null || auth instanceof AnonymousAuthenticationToken) return "redirect:/loginForm";
		
		Member member = memberService.getMember(auth.getName());
		if (member == null) return "redirect:/loginForm";
		
		CommunityComment comment = communityCommentService.getCommentByIdAndUser(commentSeq, member.getUserSeq());
		if (comment == null) {
			ra.addFlashAttribute("errorMsg", "수정할 수 없습니다.");
			return "redirect:/community";
		}
		
		String text = (content == null ? "" : content.trim());
		if (text.isBlank()) {
			ra.addFlashAttribute("errorMsg", "댓글 내용을 입력해주세요.");
			return "redirect:/detailPost/" + comment.getPostSeq();
		}
		
		CommunityComment up = new CommunityComment();
		up.setCommentSeq(commentSeq);
		up.setUserSeq(member.getUserSeq());
		up.setContent(text);
		
		boolean ok = communityCommentService.updateComment(up);
		if (!ok) {
			ra.addFlashAttribute("errorMsg", "수정할 수 없습니다.");
			return "redirect:/detailPost/" + comment.getPostSeq();
		}
		
		ra.addFlashAttribute("successMsg", "댓글이 수정되었습니다.");
		return "redirect:/detailPost/" + comment.getPostSeq();
	}

	// 댓글 삭제
	@PostMapping("/deleteComment")
	public String deleteComment(Authentication auth, RedirectAttributes ra, @RequestParam("commentSeq") int commentSeq, @RequestParam("postSeq") int postSeq) {
		
		if (auth == null || auth instanceof AnonymousAuthenticationToken) return "redirect:/loginForm";
		
		Member member = memberService.getMember(auth.getName());
		if (member == null) return "redirect:/loginForm";
		
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

		boolean ok;
		if (isAdmin) {
			ok = communityCommentService.deleteCommentAdmin(commentSeq);
		} else {
			ok = communityCommentService.deleteComment(commentSeq, member.getUserSeq());
		}
		
		if (!ok) {
			ra.addFlashAttribute("errorMsg", "삭제할 수 없습니다.");
		} else {
			ra.addFlashAttribute("successMsg", "댓글이 삭제되었습니다.");
		}
		
		return "redirect:/detailPost/" + postSeq;
	}
}
