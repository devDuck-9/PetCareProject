package com.duck.petcareproject.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.duck.petcareproject.domain.CommunityComment;
import com.duck.petcareproject.domain.CommunityPost;
import com.duck.petcareproject.service.CommunityCommentService;
import com.duck.petcareproject.service.CommunityPostService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

	private final CommunityPostService communityPostService;
	private final CommunityCommentService communityCommentService;

	@GetMapping("/admin/dashboard")
	public String dashboard(Model model,
					@RequestParam(value = "category", required = false) String category,
					@RequestParam(value = "type", defaultValue = "all") String type,
					@RequestParam(value = "keyword", required = false) String keyword,
					@RequestParam(value = "sort", defaultValue = "latest") String sort,
					@RequestParam(value = "page", defaultValue = "1") int page) {

			model.addAttribute("activeMenu", "dashboard");
			
			// 비동기 목록 조회에서도 동일 사용
			setDashboardListModel(model, category, type, keyword, sort, page);

			return "admin/adminDashboard";
	}

	// ===== 게시글 상세 fragment (Ajax) =====
	@GetMapping("/admin/dashboard/post-fragment")
	public String postFragment(Model model, @RequestParam("postSeq") int postSeq, Authentication auth) {

			// 상세는 조회수 증가 없이 단순 조회
			CommunityPost post = communityPostService.getPost(postSeq);
			
			// 댓글 조회
			List<CommunityComment> comments = communityCommentService.getCommentsByPostAdmin(postSeq, "latest");

			model.addAttribute("post", post);
			model.addAttribute("comments", comments);
			
			return "admin/adminFragments :: postDetailPanel";
	}
	
	// ===== 관리자 유저 게시글 삭제 =====
	@PostMapping("/admin/dashboard/post-delete")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> deletePostAdmin(@RequestParam("postSeq") int postSeq) {

			Map<String, Object> res = new HashMap<>();

			boolean ok = communityPostService.deletePostAdmin(postSeq);
			if (!ok) {
					res.put("ok", false);
					res.put("msg", "삭제에 실패했습니다.");
					return ResponseEntity.internalServerError().body(res);
			}

			res.put("ok", true);
			res.put("msg", "삭제되었습니다.");
			return ResponseEntity.ok(res);
	}
	
	// ===== 관리자 유저 댓글 삭제 =====
	@PostMapping("/admin/dashboard/comment-delete")
	@ResponseBody
	public Map<String, Object> deleteCommentAdmin(@RequestParam("commentSeq") int commentSeq) {

		Map<String, Object> res = new HashMap<>();

		boolean ok = communityCommentService.deleteCommentAdmin(commentSeq);
		if (!ok) {
			res.put("ok", false);
			res.put("msg", "댓글 삭제에 실패했습니다.");
			return res;
		}

		res.put("ok", true);
		res.put("msg", "댓글이 삭제되었습니다.");
		return res;
	}
	
	// 비동기 게시글 목록
	@GetMapping("/admin/dashboard/list-fragment")
	public String dashboardListFragment(Model model,
					@RequestParam(value = "category", required = false) String category,
					@RequestParam(value = "type", defaultValue = "all") String type,
					@RequestParam(value = "keyword", required = false) String keyword,
					@RequestParam(value = "sort", defaultValue = "latest") String sort,
					@RequestParam(value = "page", defaultValue = "1") int page) {

		setDashboardListModel(model, category, type, keyword, sort, page);

		return "admin/adminFragments :: dashboardListArea";
	}
	
	// 목록 조회 함수
	private void setDashboardListModel(Model model, String category, String type, String keyword, String sort, int page) {
	
		// 파라미터 카테고리 정리
		String cat = (category == null ? "" : category.trim().toUpperCase());
		if ("ALL".equals(cat)) cat = "";
		// type (검색기준) 정리
		String t = (type == null ? "all" : type.trim().toLowerCase());
		if (!List.of("all", "name", "id", "title", "content").contains(t)) t = "all";
		// keyword 정리
		String k = (keyword == null ? "" : keyword.trim());
		// sort (정렬) 정리
		String s = (sort == null ? "latest" : sort.trim().toLowerCase());
		if (!s.equals("latest") && !s.equals("old")) s = "latest";
		
		// ===== count + paging =====
		int size = 5;
		if (page <= 0) page = 1;
		
		int totalCount = communityPostService.countPostsAdmin(cat, t, k);
		int totalPage = (int) Math.ceil(totalCount / (double) size);
		if (totalPage <= 0) totalPage = 1;
		if (page > totalPage) page = totalPage;
		
		List<CommunityPost> posts = communityPostService.getPostsAdminPaging(cat, t, k, s, page, size);
		
		// ===== 페이지 그룹(3개씩) =====
		int groupSize = 3;
		int startPage = ((page - 1) / groupSize) * groupSize + 1;
		int endPage = Math.min(startPage + groupSize - 1, totalPage);
		
		// ===== model =====
		model.addAttribute("posts", posts);
		model.addAttribute("page", page);
		model.addAttribute("size", size);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		
		model.addAttribute("category", (cat.isBlank() ? "ALL" : cat));
		model.addAttribute("type", t);
		model.addAttribute("keyword", k);
		model.addAttribute("sort", s);
	}
	
}
