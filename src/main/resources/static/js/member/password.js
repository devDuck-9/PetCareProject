$(function () {

	// redirect msg -> 모달
	const msg = ($("#pageMsg").val() || "").trim();
	if (msg === "pw_changed") {
		Modal.open("#saveModal", "비밀번호가 변경되었습니다.");
	}
	
	if (msg === "pw_same") {
		Modal.open("#saveModal", "새 비밀번호는 기존 비밀번호와 달라야 합니다.");
	}

	const $current = $('input[name="currentPassword"]');
	const $newPw = $('input[name="newPassword"]');
	const $confirm = $('input[name="confirmPassword"]');

	const $btnCheck = $("#btnCheckCurrentPw");
	const $currentOk = $("#currentPwOk");
	const $currentMsg = $("#currentPwMsg");
	const $confirmMsg = $("#confirmPwMsg");
	
	// ===== 새 비밀번호 정책(p 토글) =====
	const $pwFormatErr = $('#userPwFormatError');

	function showPwFormatError(msg) {
		if (!$pwFormatErr.length) return;
		if (msg) $pwFormatErr.text(msg);
		$pwFormatErr.show();
	}
	function hidePwFormatError() {
		if ($pwFormatErr.length) $pwFormatErr.hide();
	}

	// 형식(길이/공백/허용문자) + 2종류 이상
	function validateNewPwPolicy({ show = true } = {}) {
		if (!$newPw.length) return true;

		const v = ($newPw.val() || '');

		// 입력 시작 전엔 숨김
		if (!v.length) { hidePwFormatError(); return true; }

		// 공백 금지
		if (/\s/.test(v)) { if (show) showPwFormatError('비밀번호는 공백 없이 입력해주세요.'); return false; }

		// 길이 8~20
		if (v.length < 8 || v.length > 20) { if (show) showPwFormatError('비밀번호는 8~20자로 입력해주세요.'); return false; }

		// 허용 문자만
		const allow = /^[A-Za-z\d~`!@#$%^&*()_+\-={}[\]|\\:;\"'<>,.?/]+$/;
		if (!allow.test(v)) { if (show) showPwFormatError('비밀번호는 영문/숫자/특수문자(기본)만 가능합니다.'); return false; }

		// 영문/숫자/특수 중 2개 이상 포함
		const hasAlpha = /[A-Za-z]/.test(v);
		const hasDigit = /\d/.test(v);
		const hasSpecial = /[~`!@#$%^&*()_+\-={}[\]|\\:;\"'<>,.?/]/.test(v);
		const kindCount = (hasAlpha ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);

		if (kindCount < 2) { if (show) showPwFormatError('비밀번호는 영문/숫자/특수문자 중 2종류 이상을 포함해야 합니다.'); return false; }

		hidePwFormatError();
		return true;
	}

	// 입력할 때 즉시 검증
	$newPw.on('input', function () {
		validateNewPwPolicy({ show: true });
		checkMatchTyping();
	});
	
	function showCurrentMsg(text, isError) {
		if (!$currentMsg.length) return;
		$currentMsg.text(text).show();
		$currentMsg.css("color", isError ? "#e33" : "#2a7");
	}

	function showConfirmMsg(text, isError) {
		if (!$confirmMsg.length) return;
		$confirmMsg.text(text).show();
		$confirmMsg.css("color", isError ? "#e33" : "#2a7");
	}

	// 입력 바뀌면 확인상태 리셋
	$current.on("input", function () {
		$currentOk.val("false");
		if ($currentMsg.length) $currentMsg.hide();
		if ($btnCheck.length) $btnCheck.text("확인").prop("disabled", false).removeClass("is-done");
	});

	// 새 비번 확인 실시간
	function checkMatchTyping() {
		const a = ($newPw.val() || "").trim();
		const b = ($confirm.val() || "").trim();
		if (!a && !b) { if ($confirmMsg.length) $confirmMsg.hide(); return; }
		if (a !== b) showConfirmMsg("새 비밀번호가 일치하지 않습니다.", true);
		else showConfirmMsg("비밀번호 확인 완료! ✅", false);
	}

	$confirm.on("input", checkMatchTyping);

	// 기존 비번 확인 API
	if ($btnCheck.length) {
		$btnCheck.on("click", async function () {
			const pw = ($current.val() || "").trim();
			if (!pw) {
				Modal.open("#saveModal", "기존 비밀번호를 입력해주세요.");
				$current.trigger("focus");
				return;
			}

			$(this).prop("disabled", true).text("확인중...");

			try {
				const res = await fetch("/api/member/check-password", {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({ password: pw })
				});

				const data = await res.json().catch(() => ({}));

				if (data.ok) {
					$currentOk.val("true");
					showCurrentMsg(data.message || "확인 완료! ✅", false);
					$(this).text("확인완료").addClass("is-done").prop("disabled", true);
				} else {
					$currentOk.val("false");
					showCurrentMsg(data.message || "비밀번호를 다시 입력해주세요.", true);
					$(this).prop("disabled", false).text("확인");
				}
			} catch (e) {
				$currentOk.val("false");
				showCurrentMsg("네트워크 오류가 발생했어요.", true);
				$(this).prop("disabled", false).text("확인");
			}
		});
	}

	// submit 가드
	$(".mp2-form").on("submit", function (e) {
		// 새 비번 정책 먼저
		if (!validateNewPwPolicy({ show: true })) {
			e.preventDefault();
			$newPw.trigger("focus");
			return;
		}
		
		const curOk = $currentOk.val() === "true";
		const a = ($newPw.val() || "").trim();
		const b = ($confirm.val() || "").trim();
		
		if (!curOk) {
			e.preventDefault();
			Modal.open("#saveModal", "기존 비밀번호 확인을 먼저 해주세요.");
			$btnCheck.trigger("focus");
			return;
		}

		if (!a || !b) {
			e.preventDefault();
			Modal.open("#saveModal", "새 비밀번호와 확인을 입력해주세요.");
			$newPw.trigger("focus");
			return;
		}

		if (a !== b) {
			e.preventDefault();
			Modal.open("#saveModal", "새 비밀번호가 일치하지 않습니다.");
			$confirm.trigger("focus");
			return;
		}
	});

});