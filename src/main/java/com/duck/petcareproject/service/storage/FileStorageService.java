package com.duck.petcareproject.service.storage;

import java.io.File;
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

	public String savePetImage(MultipartFile file) throws Exception {
		if (file == null || file.isEmpty()) return null;

		// 저장 폴더
		File dir = new File(uploadDir, "pet");
		if (!dir.exists()) dir.mkdirs();

		// 확장자
		String original = file.getOriginalFilename();
		String ext = getExtLower(original);

		// 확장자 화이트리스트 체크
		if (!ALLOWED_EXT.contains(ext)) {
			throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다: " + ext);
		}

		// 파일명
		String savedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

		// 저장
		File target = new File(dir, savedName);
		file.transferTo(target);

		// DB에 저장할 웹 접근 경로
		return "/resources/files/pet/" + savedName;
	}

	private String getExtLower(String filename) {
		if (filename == null) return "jpg";
		int idx = filename.lastIndexOf('.');
		if (idx < 0) return "jpg";
		return filename.substring(idx + 1).toLowerCase();
	}

}
