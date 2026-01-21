$(function () {
	const MODAL_ID = "goPostModal";
	let pendingHref = null;

	function openModal(message) {
		const backdrop = document.getElementById(MODAL_ID);
		if (!backdrop) return;

		// 메시지 세팅
		const msgEl = backdrop.querySelector("[data-modal-message]");
		if (msgEl) msgEl.textContent = message;

		// primary 버튼 텍스트 변경 (선택하기 -> 이동하기)
		const primaryBtn = backdrop.querySelector("[data-modal-primary]");
		if (primaryBtn) primaryBtn.textContent = "이동하기";

		// 열기 (modal.js 없어도 동작하도록 최소 토글)
		backdrop.setAttribute("aria-hidden", "false");
		backdrop.classList.add("is-open");
	}

	function closeModal() {
		const backdrop = document.getElementById(MODAL_ID);
		if (!backdrop) return;

		backdrop.setAttribute("aria-hidden", "true");
		backdrop.classList.remove("is-open");
		pendingHref = null;
	}

	// 리스트 클릭 -> 모달 오픈
	document.addEventListener("click", function (e) {
		const item = e.target.closest(".mp-postItem");
		if (!item) return;

		e.preventDefault();
		pendingHref = item.getAttribute("data-href");
		openModal("선택한 게시글로 이동하겠습니까?");
	});

	// 모달 내 '이동하기'
	document.addEventListener("click", function (e) {
		const backdrop = document.getElementById(MODAL_ID);
		if (!backdrop) return;

		const primary = e.target.closest("#" + MODAL_ID + " [data-modal-primary]");
		if (!primary) return;

		e.preventDefault();
		if (pendingHref) window.location.href = pendingHref;
		else closeModal();
	});

	// 모달 닫기(취소/✕/확인 등 data-modal-close)
	document.addEventListener("click", function (e) {
		const closeBtn = e.target.closest("#" + MODAL_ID + " [data-modal-close]");
		if (!closeBtn) return;

		e.preventDefault();
		closeModal();
	});

	// ESC로 닫기
	document.addEventListener("keydown", function (e) {
		if (e.key === "Escape") closeModal();
	});
});