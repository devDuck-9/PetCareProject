$(function () {
	const agree = document.getElementById('agreeDelete');
	const btnWithdraw = document.getElementById('btnWithdraw');

	const backdrop = document.getElementById('pwdBackdrop');
	const pwdInput = document.getElementById('withdrawPassword');
	const pwdError = document.getElementById('pwdError');
	const btnConfirm = document.getElementById('btnConfirmDelete');
	const btnCancel = document.getElementById('btnCancelDelete');
	const togglePwd = document.getElementById('togglePwd');

	// ===== 공통 모달 유틸 =====
	function openCommonModal(message) {
		const modal = document.querySelector('#saveModal');
		const msgEl = modal?.querySelector('[data-modal-message]');
		if (msgEl) msgEl.textContent = message;
		Modal.open('#saveModal');
	}

	function openPwdModal() {
		pwdError.style.display = 'none';
		pwdError.textContent = '';
		pwdInput.value = '';
		backdrop.classList.add('is-open');
		backdrop.setAttribute('aria-hidden', 'false');
		setTimeout(() => pwdInput.focus(), 0);
	}

	function closePwdModal() {
		backdrop.classList.remove('is-open');
		backdrop.setAttribute('aria-hidden', 'true');
	}

	function shake() {
		const modal = backdrop.querySelector('.wdp-modal');
		modal.classList.remove('is-shake');
		void modal.offsetWidth;
		modal.classList.add('is-shake');
	}

	function getCsrf() {
		const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
		const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
		return { token, header };
	}

	// 체크해야 버튼 활성화
	agree.addEventListener('change', () => {
		btnWithdraw.disabled = !agree.checked;
	});

	btnWithdraw.addEventListener('click', () => {
		// 혹시 disabled 무시하고 오는 케이스 대비
		if (!agree.checked) {
			openCommonModal('탈퇴 전, 체크박스를 선택해 주세요.');
			return;
		}
		openPwdModal();
	});

	btnCancel.addEventListener('click', closePwdModal);

	backdrop.addEventListener('click', (e) => {
		if (e.target === backdrop) closePwdModal();
	});

	window.addEventListener('keydown', (e) => {
		if (e.key === 'Escape' && backdrop.classList.contains('is-open')) closePwdModal();
	});

	togglePwd.addEventListener('click', () => {
		const isPwd = pwdInput.type === 'password';
		pwdInput.type = isPwd ? 'text' : 'password';
		togglePwd.innerHTML = isPwd ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
		pwdInput.focus();
	});

	async function submitWithdraw() {
		const password = pwdInput.value.trim();
		if (!password) {
			pwdError.textContent = '비밀번호를 입력해주세요.';
			pwdError.style.display = 'block';
			shake();
			pwdInput.focus();
			return;
		}

		btnConfirm.disabled = true;

		try {
			const { token, header } = getCsrf();

			const res = await fetch('/member/withdraw', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					...(token && header ? { [header]: token } : {})
				},
				body: JSON.stringify({ password })
			});

			const data = await res.json();

			if (data.ok) {
				closePwdModal();
				openCommonModal('탈퇴되었습니다.');
				// 안내 읽을 시간 살짝 주고 이동
				setTimeout(() => window.location.href = '/', 900);
			} else {
				pwdError.textContent = data.message || '비밀번호가 틀리셨습니다. 다시 입력해주세요.';
				pwdError.style.display = 'block';
				shake();
				pwdInput.select();
				pwdInput.focus();
			}
		} catch (e) {
			closePwdModal();
			openCommonModal('요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
		} finally {
			btnConfirm.disabled = false;
		}
	}

	btnConfirm.addEventListener('click', submitWithdraw);
	pwdInput.addEventListener('keydown', (e) => {
		if (e.key === 'Enter') submitWithdraw();
	});
});