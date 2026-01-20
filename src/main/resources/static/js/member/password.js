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

	$newPw.on("input", checkMatchTyping);
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