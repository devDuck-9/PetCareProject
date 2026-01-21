package com.duck.petcareproject.controller;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.duck.petcareproject.domain.Category;
import com.duck.petcareproject.domain.CommunityPost;
import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.service.CommunityCommentService;
import com.duck.petcareproject.service.CommunityPostService;
import com.duck.petcareproject.service.MemberService;
import com.duck.petcareproject.service.storage.FileStorageService;
import com.duck.petcareproject.util.PagingUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CommunityPostController {
	
	private final CommunityPostService communityPostService;
	private final MemberService memberService;
	private final CommunityCommentService communityCommentService;
	private final FileStorageService fileStorageService;
	
	// 마이페이지 게시글 목록
	@GetMapping("/community/myPosts")
	public String myPostList(Authentication auth,  Model model,
								@RequestParam(value = "page", defaultValue = "1") int page,
								@RequestParam(value = "size", defaultValue = "5") int size) {
		// header 메뉴 활성화
		model.addAttribute("activeMenu", "mypage");

		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}
		Member member = memberService.getMember(auth.getName());
		if (member == null) return "redirect:/loginForm";
		
		int currentPage = PagingUtils.pageOrDefault(page);
		
		int totalCount = communityPostService.countMyPosts(member.getUserSeq());
		int totalPage = (int) Math.ceil(totalCount / (double) size);
		
		if (currentPage > totalPage) currentPage = totalPage;
		
		int offset = (currentPage - 1) * size;
		
		List<CommunityPost> posts = communityPostService.findMyPosts(member.getUserSeq(), size, offset);

		model.addAttribute("posts", posts);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("size", size);

		// 왼쪽 프로필 표시용
		model.addAttribute("profileMember", memberService.findByUserId(auth.getName()));
		
		return "community/myPosts";
	}
	
	// 커뮤니티 목록 (카테고리별)
	@GetMapping({"/community", "/community/{slug}"})
	public String communityList(Authentication auth, Model model,
								@PathVariable(value = "slug", required = false) String slug,
								@RequestParam(value = "page", defaultValue = "1") int page,
								@RequestParam(value = "size", defaultValue = "5") int size,
								@RequestParam(value = "findkey", required = false) String keyword,
								@RequestParam(value = "sort", defaultValue = "latest") String sort) {

		// header 메뉴 활성화
		model.addAttribute("activeMenu", "community");

		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}
		
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		model.addAttribute("isAdmin", isAdmin);
		
		Category category = resolveCategoryBySlug(slug); // null이면 전체
		String categoryCode = (category == null ? "" : category.name());

		// 뷰에서 쓰는 activeCategory 는 String 으로 제공
		model.addAttribute("activeCategory", (category == null ? "ALL" : category.name()));
		model.addAttribute("categorySlug", (slug == null ? "" : slug));
		
		// 검색/정렬
		String findkey = (keyword == null ? "" : keyword.trim());
		String sortKey = (sort == null ? "latest" : sort.trim().toLowerCase());
		if (!sortKey.equals("latest") && !sortKey.equals("old")) sortKey = "latest";

		/// 페이징 보정
		if (page <= 0) page = 1;
		if (size <= 0) size = 5;
		
		int totalCount = communityPostService.countPosts(categoryCode, findkey);
		int totalPage = (int) Math.ceil(totalCount / (double) size);
		if (totalPage <= 0) totalPage = 1;
		if (page > totalPage) page = totalPage;

		model.addAttribute("posts", communityPostService.getPostsPaging(categoryCode, findkey, sortKey, page, size));
		model.addAttribute("page", page);
		model.addAttribute("size", size);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("findkey", findkey);
		model.addAttribute("sort", sortKey);

		// 템플릿 삼항 제거용 타이틀
		model.addAttribute("categoryTitle", resolveCategoryTitle(category));

		// paging 링크 베이스
		model.addAttribute("basePath", (slug == null || slug.isBlank()) ? "/community" : ("/community/" + slug));
		
		return "community/postList";
	}
	
	// 게시글 상세
	@GetMapping("/detailPost/{id}")
	public String detailPost(@PathVariable("id") int postSeq, Authentication auth, Model model, RedirectAttributes ra, HttpServletRequest request) {
		
		// header 메뉴 활성화
		model.addAttribute("activeMenu", "community");
		
		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}
		
		Member member = memberService.getMember(auth.getName());
		if (member == null) return "redirect:/loginForm";

		CommunityPost post = communityPostService.getPostDetail(postSeq);
		if (post == null) {
		ra.addFlashAttribute("errorMsg", "게시글이 존재하지 않습니다.");
		return "redirect:/";
		}

		boolean isOwner = (post.getUserSeq() == member.getUserSeq());
		model.addAttribute("post", post);
		model.addAttribute("isOwner", isOwner);
		
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		model.addAttribute("isAdmin", isAdmin);

		boolean canManage = isOwner || isAdmin;
		model.addAttribute("canManage", canManage);
		
		String referer = request.getHeader("Referer");
		String returnUrl;

		if (referer == null || referer.isBlank()) {
				returnUrl = "/community";
		} else {
				// 도메인 제거해서 path 만
				returnUrl = referer.replaceFirst("^https?://[^/]+", "");
				// 안전하게 community 페이지만 허용
				if (!returnUrl.startsWith("/community")) {
						returnUrl = "/community";
				}
		}
		model.addAttribute("returnUrl", returnUrl);
		model.addAttribute("images", communityPostService.getPostImages(postSeq));
		model.addAttribute("comments", communityCommentService.getCommentsByPost(postSeq, member.getUserSeq()));
		
		return "community/postDetail";
	}

	// 게시글 수정 폼
	@GetMapping("/updatePost/{id}")
	public String updatePostForm(@PathVariable("id") int postSeq, Authentication auth, Model model, RedirectAttributes ra) {
		
		// header 메뉴 활성화
		model.addAttribute("activeMenu", "community");
		
		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}
		
		Member member = memberService.getMember(auth.getName());
		if (member == null) return "redirect:/loginForm";

		CommunityPost post = communityPostService.getPostByIdAndUser(postSeq, member.getUserSeq());
		if (post == null) {
			ra.addFlashAttribute("errorMsg", "수정할 수 없습니다.");
			return "redirect:/detailPost/" + postSeq;
		}
		
		model.addAttribute("post", post);
		model.addAttribute("images", communityPostService.getPostImages(postSeq));
		
		return "community/editPostForm";
	}
	
	// 게시글 등록 폼
	@GetMapping("/addPostForm")
	public String addPostForm(Authentication auth, Model model, @RequestParam(value = "category", required = false) String categoryRaw) {

		// header 메뉴 활성화
		model.addAttribute("activeMenu", "community");

		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}
		
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		model.addAttribute("isAdmin", isAdmin);
		
		// 넘어온 category 처리
		String c = (categoryRaw == null ? "" : categoryRaw.trim().toUpperCase());
		
		// 전체글에서 왔거나 비어있으면 FREE로
		if (c.isBlank() || c.equals("ALL")) c = "FREE";

		// GUIDE는 관리자만
		if ("GUIDE".equals(c) && !isAdmin) {
			return "redirect:/community/guide";
		}

		// 유효한 카테고리가 아니면 FREE
		try {
			Category.valueOf(c);
		} catch (Exception e) {
			c = "FREE";
		}

		model.addAttribute("selectedCategory", c);
		return "community/addPostForm";
	}
	
	// 게시글 수정
	@PostMapping("/updatePost/{id}")
	public String updatePost(@PathVariable("id") int postSeq, Authentication auth, RedirectAttributes ra, Model model,
							@RequestParam("category") String categoryRaw,
							@RequestParam("title") String title,
							@RequestParam("content") String content,
							@RequestParam(value="images", required=false) MultipartFile[] images,
							@RequestParam(value="removeImageSeqs", required=false) List<Integer> removeImageSeqs) throws Exception {

		// header 메뉴 활성화
		model.addAttribute("activeMenu", "community");
		
		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}
		
		Member member = memberService.getMember(auth.getName());
		if (member == null) return "redirect:/loginForm";
		
		Category category = parseCategory(categoryRaw);
		if (category == null) {
			ra.addFlashAttribute("errorMsg", "카테고리가 올바르지 않습니다.");
			return "redirect:/community";
		}
		
		CommunityPost post = new CommunityPost();
		post.setPostSeq(postSeq);
		post.setUserSeq(member.getUserSeq());
		post.setCategory(category);
		post.setTitle(title != null ? title.trim() : "");
		post.setContent(content != null ? content.trim() : "");

		boolean ok = communityPostService.updatePostWithImages(post, images, removeImageSeqs);
		
		if (!ok) {
			ra.addFlashAttribute("errorMsg", "수정할 수 없습니다.");
			return "redirect:/detailPost/" + postSeq;
		}
		ra.addFlashAttribute("successMsg", "게시글이 수정되었습니다.");
		
		return "redirect:/detailPost/" + postSeq;
	}

	// 게시글 삭제
	@PostMapping("/deletePost/{id}")
	public String deletePost(@PathVariable("id") int postSeq, Authentication auth, RedirectAttributes ra,
								@RequestParam(value = "returnUrl", required = false) String returnUrl) {
		
		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}
		
		Member member = memberService.getMember(auth.getName());
		if (member == null) return "redirect:/loginForm";
		
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		
		// 삭제 전에 글을 한번 조회(조회수증가 없는 메서드) : category 확보
		CommunityPost post = communityPostService.getPost(postSeq);
		
		if (post == null) {
			ra.addFlashAttribute("errorMsg", "게시글이 존재하지 않습니다.");
			return "redirect:/community";
		}
		
		boolean ok;
		ok = communityPostService.deletePostWithImages(postSeq, member.getUserSeq(), isAdmin);
		
		if (!ok) {
			ra.addFlashAttribute("errorMsg", "삭제할 수 없습니다.");
			return "redirect:/detailPost/" + postSeq;
		}
		ra.addFlashAttribute("successMsg", "게시글이 삭제되었습니다.");
		
		// 삭제된 글의 카테고리로 목록 이동
//		return "redirect:" + buildRedirectToCategory(post.getCategory());
		
		
		// returnUrl 우선
		if (returnUrl != null && !returnUrl.isBlank()) {
			// 보안상 내부 경로만 허용
			if (returnUrl.startsWith("/community")) {
				return "redirect:" + returnUrl;
			}
		}
		
		// fallback : 전체글
		return "redirect:/community";
	}
		
	// 게시글 등록
	@PostMapping("/insertPost")
	public String insertPost( Authentication auth, RedirectAttributes ra,
								@RequestParam("category") String categoryRaw,
								@RequestParam("title") String title,
								@RequestParam("content") String content,
								@RequestParam(value = "images", required = false) MultipartFile[] images) throws Exception {
		
		// 로그인 체크(안전)
		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return "redirect:/loginForm";
		}

		Member member = memberService.getMember(auth.getName());
		if (member == null) return "redirect:/loginForm";
		
		Category category = parseCategory(categoryRaw);
		if (category == null) {
			ra.addFlashAttribute("errorMsg", "카테고리가 올바르지 않습니다.");
			return "redirect:/community";
		}
		
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

		if (category == Category.GUIDE && !isAdmin) {
			ra.addFlashAttribute("errorMsg", "공지사항은 관리자만 작성할 수 있습니다.");
			return "redirect:/community/guide";
		}
		
		int MAX_IMAGES = 5;
		if (images != null) {
			int count = 0;
			for (MultipartFile f : images) {
				if (f != null && !f.isEmpty()) count++;
			}
			if (count > MAX_IMAGES) {
				ra.addFlashAttribute("errorMsg", "사진은 최대 " + MAX_IMAGES + "장까지 첨부할 수 있습니다.");
				return "redirect:/addPostForm";
			}
		}
		
		CommunityPost post = new CommunityPost();
		post.setUserSeq(member.getUserSeq());
		post.setCategory(category);
		post.setTitle(title != null ? title.trim() : "");
		post.setContent(content != null ? content.trim() : "");

		communityPostService.insertPostWithImages(post, images);
		
		ra.addFlashAttribute("successMsg", "게시글이 등록되었습니다.");
		
		// 등록한 카테고리 목록으로 이동
		return "redirect:" + buildRedirectToCategory(category);
	}
	
	
	// slug -> enum (목록 라우팅용)
	private Category resolveCategoryBySlug(String slug) {
		if (slug == null || slug.isBlank()) return null;
		return switch (slug.toLowerCase()) {
			case "free" -> Category.FREE;
			case "guide" -> Category.GUIDE;
			case "info" -> Category.INFO;
			case "boast" -> Category.BOAST;
			case "qna" -> Category.QNA;
			default -> null; // 알 수 없는 slug 는 전체
		};
	}

	// form value(String) -> enum (등록/수정용)
	private Category parseCategory(String raw) {
		if (raw == null || raw.isBlank()) return null;
		try {
			return Category.valueOf(raw.trim().toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}

	// enum -> 목록 URL (redirect용)
	private String buildRedirectToCategory(Category category) {
		if (category == null) return "/community";
		return switch (category) {
			case FREE -> "/community/free";
			case GUIDE -> "/community/guide";
			case INFO -> "/community/info";
			case BOAST -> "/community/boast";
			case QNA -> "/community/qna";
		};
	}

	// 화면 타이틀
	private String resolveCategoryTitle(Category category) {
		if (category == null) return "전체글";
		return switch (category) {
			case GUIDE -> "공지사항";
			case FREE -> "자유 게시판";
			case INFO -> "정보 공유";
			case BOAST -> "자랑 하기";
			case QNA -> "질문 & 답변";
		};
	}


}
