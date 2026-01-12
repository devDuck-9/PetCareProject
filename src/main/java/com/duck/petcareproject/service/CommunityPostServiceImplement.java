package com.duck.petcareproject.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.duck.petcareproject.domain.CommunityPost;
import com.duck.petcareproject.mapper.CommunityPostMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityPostServiceImplement implements CommunityPostService {
	
	private final CommunityPostMapper communityPostMapper;

	public void insertPost(CommunityPost post) {
		communityPostMapper.insertPost(post);
	}

	public List<CommunityPost> getPostsPaging(String category, int page, int pageSize) {
		if (page <= 0) page = 1;
		if (pageSize <= 0) pageSize = 10;

		int offset = (page - 1) * pageSize;
		return communityPostMapper.selectPostsPaging(category, pageSize, offset);
	}

	public int countPosts(String category) {
		return communityPostMapper.countPosts(category);
	}

}
