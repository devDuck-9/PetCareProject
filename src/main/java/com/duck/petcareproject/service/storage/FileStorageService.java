package com.duck.petcareproject.service.storage;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
	
	@Value("${app.upload.dir}")
	private String uploadDir;

	private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
	
	// 프로필 이미지 profile 폴더
	public String saveProfileImage(MultipartFile file) throws Exception {
		return saveImage(file, "profile");
	}
	
	// 반려동물 프로필 pet 폴더
	public String savePetImage(MultipartFile file) throws Exception {
		return saveImage(file, "pet");
	}
	
	// 게시글 post 폴더
	public String savePostImage(MultipartFile file) throws Exception {
		return saveImage(file, "post");
	}
	
	// 웹 접근 경로
	private String saveImage(MultipartFile file, String folder) throws Exception {
		if (file == null || file.isEmpty()) return null;

		File dir = new File(uploadDir, folder);
		if (!dir.exists()) dir.mkdirs();

		String original = file.getOriginalFilename();
		String ext = getExtLower(original);

		if (!ALLOWED_EXT.contains(ext)) {
			throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다: " + ext);
		}

		String savedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

		File target = new File(dir, savedName);
		file.transferTo(target);

		// folder 에 따라 분리
		return "/resources/files/" + folder + "/" + savedName;
	}
	
	// 게시글 이미지 삭제 (다건)
	public void deletePostImages(List<String> imagePaths) {
		if (imagePaths == null || imagePaths.isEmpty()) return;

		for (String path : imagePaths) {
			deleteSingleImage(path);
		}
	}
	
	// 내부 공통 삭제 로직
	private void deleteSingleImage(String imagePath) {
		if (imagePath == null || imagePath.isBlank()) return;

		// 예: /resources/files/post/abc.jpg
		if (!imagePath.startsWith("/resources/files/")) return;

		// /resources/files/ 제거 → post/abc.jpg
		String relativePath = imagePath.replaceFirst("/resources/files/", "");

		File file = new File(uploadDir, relativePath);

		if (file.exists() && file.isFile()) {
			boolean deleted = file.delete();
			if (!deleted) {
				System.err.println("[WARN] 파일 삭제 실패: " + file.getAbsolutePath());
			}
		}
	}
	
	private String getExtLower(String filename) {
		if (filename == null) return "jpg";
		int idx = filename.lastIndexOf('.');
		if (idx < 0) return "jpg";
		return filename.substring(idx + 1).toLowerCase();
	}

}
