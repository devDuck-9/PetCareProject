/* =========================
	 공통 Modal 유틸
========================= */
(function () {
	let lastFocusEl = null;

	function $get(selector) { return $(selector); }

	function setText($el, txt) {
		if (!$el.length) return;
		$el.text(txt);
	}

	window.Modal = {
		// open(selector, message)	or	open(selector, message, options)
		open(selector, message, options) {
			const $backdrop = $get(selector);
			if (!$backdrop.length) return;

			lastFocusEl = document.activeElement;

			// message 세팅
			if (typeof message === "string") {
				const $msg = $backdrop.find("[data-modal-message]").first();
				if ($msg.length) $msg.html(message);
			}

			// options 처리 (액션 모달용)
			const opts = options || {};
			$backdrop.data("modalOptions", opts);

			// 버튼 텍스트 변경
			if (opts.primaryText) setText($backdrop.find("[data-modal-primary]").first(), opts.primaryText);
			if (opts.secondaryText) setText($backdrop.find("[data-modal-close]").last(), opts.secondaryText);

			// slot 내용 삽입 (select 등)
			const $slot = $backdrop.find("[data-modal-slot]").first();
			if ($slot.length) {
				const slotHtml = (opts.slotHtml ?? '').trim();

					if (slotHtml === '') {
						// slot 안 쓰는 모달 → 공간 자체 제거
						$slot.empty().hide();
					} else {
						// slot 사용하는 모달 → 표시
						$slot.html(slotHtml).show();
					}
			}

			// 열기
			$backdrop.addClass("is-open").attr("aria-hidden", "false");

			// 포커스 : primary 버튼 우선, 없으면 close
			const $focusTarget =
				$backdrop.find("[data-modal-primary]").first().length
					? $backdrop.find("[data-modal-primary]").first()
					: $backdrop.find("[data-modal-close]").first();

			if ($focusTarget.length) $focusTarget.trigger("focus");
		},

		close(selector) {
			const $backdrop = $get(selector);
			if (!$backdrop.length) return;

			$backdrop.removeClass("is-open").attr("aria-hidden", "true");

			if (lastFocusEl && typeof lastFocusEl.focus === "function") {
				lastFocusEl.focus();
			}
		},

		bind(selector) {
			const $backdrop = $get(selector);
			if (!$backdrop.length) return;

			if ($backdrop.data("modalBound")) return;
			$backdrop.data("modalBound", true);

			// primary 버튼 클릭 (선택하기/등록하기)
			$backdrop.on("click", "[data-modal-primary]", function (e) {
				e.preventDefault();
				const opts = $backdrop.data("modalOptions") || {};
				if (typeof opts.onPrimary === "function") {
					// onPrimary가 false 반환하면 닫지 않음 (검증용)
					const r = opts.onPrimary($backdrop);
					if (r === false) return;
				}
				window.Modal.close(selector);
			});

			// 닫기 버튼들
			$backdrop.on("click", "[data-modal-close]", function (e) {
				e.preventDefault();
				const opts = $backdrop.data("modalOptions") || {};
				if (typeof opts.onClose === "function") opts.onClose($backdrop);
				window.Modal.close(selector);
			});

			// 배경 클릭 닫기
			$backdrop.on("click", function (e) {
				if (e.target !== this) return;
				const opts = $backdrop.data("modalOptions") || {};
				if (typeof opts.onClose === "function") opts.onClose($backdrop);
				window.Modal.close(selector);
			});

			// ESC 닫기
			$(document).on("keydown.modal", function (e) {
				if (e.key !== "Escape") return;
				if ($backdrop.hasClass("is-open")) {
					const opts = $backdrop.data("modalOptions") || {};
					if (typeof opts.onClose === "function") opts.onClose($backdrop);
					window.Modal.close(selector);
				}
			});
		}
	};
})();
