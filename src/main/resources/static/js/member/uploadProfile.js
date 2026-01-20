$(function () {
	const btn = document.getElementById('btnProfilePhoto');
	const fileInput = document.getElementById('profileFile');
	const img = document.getElementById('profileImg');

	function openModal(msg) {
		const el = document.querySelector('#saveModal [data-modal-message]');
		if (el) el.textContent = msg;
		Modal.open('#saveModal');
	}

	btn.addEventListener('click', () => fileInput.click());

	fileInput.addEventListener('change', async () => {
		const file = fileInput.files[0];
		if (!file) return;

		if (!file.type.startsWith('image/')) {
			openModal('이미지 파일만 업로드할 수 있어요.');
			fileInput.value = '';
			return;
		}

		if (file.size > 3 * 1024 * 1024) {
			openModal('이미지는 3MB 이하만 가능합니다.');
			fileInput.value = '';
			return;
		}

		// 미리보기
		img.src = URL.createObjectURL(file);

		const formData = new FormData();
		formData.append('profile', file);

		try {
			const res = await fetch('/member/profile/photo', {
				method: 'POST',
				body: formData
			});
			const data = await res.json();

			if (data.ok) {
				img.src = data.url;
				openModal('프로필 사진이 변경되었습니다.');
			} else {
				openModal(data.message);
			}
		} catch {
			openModal('업로드 중 오류가 발생했습니다.');
		} finally {
			fileInput.value = '';
		}
	});
});
