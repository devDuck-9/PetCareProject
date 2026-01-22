/* =========================
	 마이페이지(내정보)
========================= */

$(function () {
	// --------------------
	// util
	// --------------------
	function onlyDigits(v, maxLen) {
		return (v || "").toString().replace(/\D/g, "").slice(0, maxLen);
	}

	// ======================================================================
	// MOBILE
	// ======================================================================
	const $m1 = $("#mobile1");
	const $m2 = $("#mobile2");
	const $m3 = $("#mobile3");
	const $mFinal = $("#mobileFinal");
	const $smsMsg = $("#smsMsg");
	const $mobileVerified = $("#mobileVerified");

	const $sendSmsBtn = $("#btnSendCode");
	const $verifySmsBtn = $("#btnVerifyCode");
	const $smsCode = $("#smsCode");

	function syncMobile() {
		if (!$mFinal.length) return;

		const p1 = $m1.length ? $m1.val() : "";
		const p2 = $m2.length ? onlyDigits($m2.val(), 4) : "";
		const p3 = $m3.length ? onlyDigits($m3.val(), 4) : "";

		if ($m2.length) $m2.val(p2);
		if ($m3.length) $m3.val(p3);

		const ok = p1 && p2.length >= 3 && p3.length === 4;
		$mFinal.val(ok ? `${p1}-${p2}-${p3}` : "");
	}
	
	// 휴대폰 입력 UX 메시지(번호 형식)
	const $mobileMsg = $('#mobileMsg');

	function showMobileMsg(text) {
		if (!$mobileMsg.length) return;
		$mobileMsg.text(text).show();
	}

	function hideMobileMsg() {
		if ($mobileMsg.length) $mobileMsg.hide();
	}

	// 휴대폰 입력 길이 UX 체크 (입력 중 안내용)
	function validateMobileTyping() {
		const p2 = ($m2.val() || '').replace(/\D/g, '');
		const p3 = ($m3.val() || '').replace(/\D/g, '');

		// 아무것도 안 치고 있을 때는 조용히
		if (!p2 && !p3) {
			hideMobileMsg();
			return;
		}

		// 가운데자리 1~2자리면 안내
		if (p2.length > 0 && p2.length < 3) {
			showMobileMsg('휴대폰 번호 가운데 자리는 최소 3자리 이상 입력해주세요.');
			return;
		}

		// 끝자리 1~3자리면 안내
		if (p3.length > 0 && p3.length < 4) {
			showMobileMsg('휴대폰 번호 끝자리는 4자리로 입력해주세요.');
			return;
		}

		hideMobileMsg();
	}
	
	function splitMobileFromHidden() {
		if (!$mFinal.length) return;

		const raw = ($mFinal.val() || "").trim();
		const m = raw.match(/^(01\d)-(\d{3,4})-(\d{4})$/);
		if (!m) {
			syncMobile();
			return;
		}

		if ($m1.length) $m1.val(m[1]);
		if ($m2.length) $m2.val(m[2]);
		if ($m3.length) $m3.val(m[3]);
	}

	function showSmsMsg(text) {
		if (!$smsMsg.length) return;
		$smsMsg.text(text).show();
	}

	// 휴대폰 인증 리셋 (입력 중에는 모달 X, 저장 시에만 모달)
	function resetMobileVerify() {
		if ($mobileVerified.length) $mobileVerified.val("false");
		if ($smsMsg.length) $smsMsg.hide();
		if ($smsCode.length) $smsCode.val("");
		if ($verifySmsBtn.length) $verifySmsBtn.prop("disabled", false).text("확인");
	}

	// init mobile
	splitMobileFromHidden();
	syncMobile();
	let originalMobile = ($mFinal.val() || "").trim();

	// mobile input events (값 바뀌면 인증 무효)
	if ($m2.length)
		$m2.on("input", function () {
			$(this).val(onlyDigits(this.value, 4));
			syncMobile();
			resetMobileVerify();
			validateMobileTyping();
		});

	if ($m3.length)
		$m3.on("input", function () {
			$(this).val(onlyDigits(this.value, 4));
			syncMobile();
			resetMobileVerify();
			validateMobileTyping();
		});

	if ($m1.length)
		$m1.on("change", function () {
			syncMobile();
			resetMobileVerify();
			validateMobileTyping();
		});

	if ($smsCode.length) {
		$smsCode.on("input", function () {
			this.value = onlyDigits(this.value, 6);
		});
	}

	// sms send
	if ($sendSmsBtn.length) {
		$sendSmsBtn.on("click", async function () {
			syncMobile();
			const mobile = $mFinal.val();

			if (!mobile) {
				Modal.open("#smsCheckModal", "휴대폰 번호를 먼저 정확히 입력해주세요.");
				return;
			}
			
			if (mobile === originalMobile) {
				Modal.open("#smsCheckModal", "기존 정보와 같습니다.");
				return;
			}
			
			$(this).prop("disabled", true).text("전송중...");

			try {
				const res = await fetch("/api/sms/send", {
					method: "POST",
					headers: { "Content-Type": "application/x-www-form-urlencoded" },
					body: new URLSearchParams({ mobile }),
				});

				const data = await res.json().catch(() => ({}));
				
				// 콘솔로 SMS 인증코드 찍어주기
				if (data.devCode) {
					console.log('[DEV SMS CODE] =', data.devCode);
				}
				
				if (!res.ok) {
					showSmsMsg(data.message || "인증번호 발송 실패");
					$(this).prop("disabled", false).text("인증번호 발송");
					return;
				}

				showSmsMsg(data.message || "인증번호를 전송했어요.");
				$(this).prop("disabled", false).text("인증번호 발송");
				$("#smsCode").trigger("focus");
			} catch (e) {
				showSmsMsg("네트워크 오류가 발생했어요.");
				$(this).prop("disabled", false).text("인증번호 발송");
			}
		});
	}

	// sms verify
	if ($verifySmsBtn.length) {
		$verifySmsBtn.on("click", async function () {
			syncMobile();
			const mobile = $mFinal.val();
			const code = ($smsCode.val() || "").trim();

			if (!mobile) {
				Modal.open("#smsCheckModal", "휴대폰 번호를 먼저 정확히 입력해주세요.");
				return;
			}
			if (!code || code.length !== 6) {
				showSmsMsg("인증번호 6자리를 입력해주세요.");
				return;
			}

			$(this).prop("disabled", true).text("확인중...");

			try {
				const res = await fetch("/api/sms/verify", {
					method: "POST",
					headers: { "Content-Type": "application/x-www-form-urlencoded" },
					body: new URLSearchParams({ mobile, code }),
				});

				const data = await res.json().catch(() => ({}));

				if (!res.ok) {
					showSmsMsg(data.message || "인증 실패");
					$(this).prop("disabled", false).text("확인");
					return;
				}

				showSmsMsg(data.message || "인증 완료! ✅");
				if ($mobileVerified.length) $mobileVerified.val("true");

				// 인증 성공 후 현재 값 기준으로 원래값 갱신
				originalMobile = ($mFinal.val() || "").trim();

				$(this).prop("disabled", true).text("확인");
			} catch (e) {
				showSmsMsg("네트워크 오류가 발생했어요.");
				$(this).prop("disabled", false).text("확인");
			}
		});
	}

	// ======================================================================
	// ADDRESS (daum postcode)
	// ======================================================================
	$("#btnSearchAddress").on("click", function () {
		if (typeof daum === "undefined" || !daum.Postcode) {
			Modal.open("#saveModal", "주소찾기 모듈이 로드되지 않았어요.");
			return;
		}

		new daum.Postcode({
			oncomplete: function (data) {
				const zipcode = data.zonecode;
				const addr = data.roadAddress || data.jibunAddress;

				$("#zipcode").val(zipcode);
				$("#address1").val(addr);
				$("#address2").val("").focus();
			},
		}).open();
	});

	// ======================================================================
	// EMAIL
	// ======================================================================
	const $domainInput = $('input[name="emailDomainInput"]');
	const $domainSelect = $('select[name="emailDomainSelect"]');
	const $domainFinal = $("#emailDomainFinal");

	const $emailSendBtn = $("#btnSendEmailCode");
	const $emailVerifyBtn = $("#btnVerifyEmailCode");
	const $emailCodeInput = $("#emailCode");
	const $emailMsg = $("#emailMsg");
	const $emailVerified = $("#emailVerified");

	function getFullEmail() {
		const id = ($('input[name="emailId"]').val() || "").trim();
		const domain = ($domainFinal.val() || "").trim();
		if (!id || !domain) return "";
		return `${id}@${domain}`;
	}

	function showEmailMsg(text) {
		if ($emailMsg.length) $emailMsg.text(text).show();
	}

	function setManualMode() {
		if (!$domainInput.length || !$domainFinal.length) return;
		$domainInput.prop("readonly", false);
		$domainInput.attr("placeholder", "도메인 직접입력");
		$domainFinal.val(($domainInput.val() || "").trim());
	}

	function setPresetMode(domain) {
		if (!$domainInput.length || !$domainFinal.length) return;
		$domainInput.val(domain);
		$domainInput.prop("readonly", true);
		$domainInput.attr("placeholder", "");
		$domainFinal.val(domain);
	}

	function initEmailDomain() {
		if (!$domainSelect.length || !$domainInput.length || !$domainFinal.length) return;

		const current = ($domainFinal.val() || "").trim();
		const presets = Array.from($domainSelect[0].options).map((o) => o.value);

		if (current && presets.includes(current)) {
			$domainSelect.val(current);
			setPresetMode(current);
		} else {
			$domainSelect.val("manual");
			$domainInput.val(current);
			setManualMode();
		}
	}

	// 이메일 인증 리셋(입력 중에는 모달 X, 저장 시에만 모달)
	function resetEmailVerify() {
		if ($emailVerified.length) $emailVerified.val("false");
		if ($emailMsg.length) $emailMsg.hide();
		if ($emailCodeInput.length) $emailCodeInput.val("");
		if ($emailVerifyBtn.length) $emailVerifyBtn.prop("disabled", false).text("확인");
	}

	// init email
	initEmailDomain();
	let originalEmail = getFullEmail();

	// domain select
	if ($domainSelect.length) {
		$domainSelect.on("change", function () {
			const v = $(this).val();
			if (v === "manual") {
				setManualMode();
				$domainInput.val("").trigger("focus");
				$domainFinal.val("");
			} else {
				setPresetMode(v);
			}
			resetEmailVerify();
		});
	}

	// domain input
	if ($domainInput.length) {
		$domainInput.on("input", function () {
			if ($domainSelect.length && $domainSelect.val() === "manual") {
				$domainFinal.val(($domainInput.val() || "").trim());
			}
			resetEmailVerify();
		});
	}

	// emailId change
	$('input[name="emailId"]').on("input change", function () {
		resetEmailVerify();
	});

	if ($emailCodeInput.length) {
		$emailCodeInput.on("input", function () {
			this.value = onlyDigits(this.value, 6);
		});
	}

	// email send
	if ($emailSendBtn.length) {
		$emailSendBtn.on("click", async function () {
			const email = getFullEmail();
			if (!email) {
				Modal.open("#emailCheckModal", "이메일을 먼저 정확히 입력해주세요.");
				return;
			}
			
			if (email === originalEmail) {
				Modal.open("#emailCheckModal", "기존 정보와 같습니다.");
				return;
			}
			
			$(this).prop("disabled", true).text("전송중...");

			try {
				const res = await fetch("/api/email/send", {
					method: "POST",
					headers: { "Content-Type": "application/x-www-form-urlencoded" },
					body: new URLSearchParams({ email }),
				});

				const data = await res.json().catch(() => ({}));
				
				// 콘솔로 이메일 인증코드 찍어주기
				if (data.devCode) {
					console.log("[DEV EMAIL CODE]", data.devCode);
				}
				
				if (!res.ok) {
					showEmailMsg(data.message || "이메일 발송 실패");
					$(this).prop("disabled", false).text("인증번호 발송");
					return;
				}

				showEmailMsg(data.message || "인증번호를 전송했어요.");
				$(this).prop("disabled", false).text("인증번호 발송");
				$("#emailCode").trigger("focus");
			} catch (e) {
				showEmailMsg("네트워크 오류가 발생했어요.");
				$(this).prop("disabled", false).text("인증번호 발송");
			}
		});
	}

	// email verify
	if ($emailVerifyBtn.length) {
		$emailVerifyBtn.on("click", async function () {
			const email = getFullEmail();
			const code = ($emailCodeInput.val() || "").trim();

			if (!email) {
				Modal.open("#emailCheckModal", "이메일을 먼저 정확히 입력해주세요.");
				return;
			}
			if (!code || code.length !== 6) {
				showEmailMsg("인증번호 6자리를 입력해주세요.");
				return;
			}

			$(this).prop("disabled", true).text("확인중...");

			try {
				const res = await fetch("/api/email/verify", {
					method: "POST",
					headers: { "Content-Type": "application/x-www-form-urlencoded" },
					body: new URLSearchParams({ email, code }),
				});

				const data = await res.json().catch(() => ({}));
				
				if (!res.ok) {
					showEmailMsg(data.message || "인증 실패");
					$(this).prop("disabled", false).text("확인");
					return;
				}

				showEmailMsg(data.message || "인증 완료! ✅");
				if ($emailVerified.length) $emailVerified.val("true");

				// 인증 성공 후 현재 값 기준으로 원래값 갱신
				originalEmail = getFullEmail();

				$(this).prop("disabled", true).text("확인");
			} catch (e) {
				showEmailMsg("네트워크 오류가 발생했어요.");
				$(this).prop("disabled", false).text("확인");
			}
		});
	}

	// ======================================================================
	// NICKNAME DUP CHECK
	// ======================================================================
	const $nameInput = $('input[name="userName"]');
	const $nameBtn = $("#btnCheckName");
	let originalName = ($nameInput.val() || "").trim();
	let isNameChecked = false;

	function resetNameCheck() {
		isNameChecked = false;
		if ($nameBtn.length) {
			$nameBtn.prop("disabled", false);
			$nameBtn.removeClass("is-done");
			$nameBtn.text("중복확인");
		}
	}

	if ($nameInput.length) {
		$nameInput.on("input", function () {
			const now = ($nameInput.val() || "").trim();
			if (now !== originalName) resetNameCheck();
		});
	}

	if ($nameBtn.length) {
		$nameBtn.on("click", async function (e) {
			e.preventDefault();

			const userName = ($nameInput.val() || "").trim();
			if (!userName) {
				Modal.open("#nameCheckModal", "닉네임을 입력해주세요.");
				$nameInput.trigger("focus");
				return;
			}

			// 그대로면 확인 완료로 처리
			if (userName === originalName) {
				isNameChecked = true;
				$nameBtn.addClass("is-done").text("확인완료 !");
				Modal.open("#nameCheckModal", "현재 닉네임을 그대로 사용합니다.");
				return;
			}

			try {
				const res = await fetch(
					`/api/member/check-name?userName=${encodeURIComponent(userName)}`,
					{ headers: { Accept: "application/json" } }
				);

				if (!res.ok) {
					Modal.open("#nameCheckModal", "서버 오류가 발생했어요.");
					return;
				}

				const data = await res.json();
				Modal.open("#nameCheckModal", data.message);

				if (data.exists === false) {
					isNameChecked = true;
					$nameBtn.addClass("is-done").text("확인완료 !");
				}
			} catch (err) {
				Modal.open("#nameCheckModal", "네트워크 오류가 발생했어요.");
			}
		});
	}

	// ======================================================================
	// SUBMIT VALIDATION (UX: 저장 시에만 모달 + 포커스 유도)
	// ======================================================================
	const $form = $(".mp2-form");
	if ($form.length) {
		$form.on("submit", function (e) {
			syncMobile();

			const nowMobile = ($mFinal.val() || "").trim();
			const nowEmail = getFullEmail();

			// 1) 빈값 먼저
			if (!nowMobile) {
				e.preventDefault();
				Modal.open("#smsCheckModal", "휴대폰 번호를 입력해주세요.");
				$("#mobile2").trigger("focus");
				return;
			}
			if (!nowEmail) {
				e.preventDefault();
				Modal.open("#emailCheckModal", "이메일을 입력해주세요.");
				$('input[name="emailId"]').trigger("focus");
				return;
			}

			// 2) manual 도메인 빈값 방지
			if ($domainSelect.length && $domainSelect.val() === "manual") {
				const d = ($domainInput.val() || "").trim();
				if (!d) {
					e.preventDefault();
					Modal.open("#emailCheckModal", "이메일 도메인을 입력해주세요.");
					$domainInput.trigger("focus");
					return;
				}
			}

			// 3) 닉네임 변경 시 중복확인
			const nowName = ($nameInput.val() || "").trim();
			if (nowName && nowName !== originalName && !isNameChecked) {
				e.preventDefault();
				Modal.open("#nameCheckModal", "닉네임 중복확인을 먼저 해주세요.");
				$nameInput.trigger("focus");
				return;
			}

			// 4) 휴대폰 변경 시 인증 필요
			const mobileChanged = nowMobile !== originalMobile;
			if (mobileChanged && $("#mobileVerified").val() !== "true") {
				e.preventDefault();
				Modal.open("#smsCheckModal", "휴대폰 번호가 변경되었습니다. 인증을 완료해주세요.");
				$("#btnSendCode").trigger("focus");
				return;
			}

			// 5) 이메일 변경 시 인증 필요
			const emailChanged = nowEmail !== originalEmail;
			if (emailChanged && $("#emailVerified").val() !== "true") {
				e.preventDefault();
				Modal.open("#emailCheckModal", "이메일이 변경되었습니다. 인증을 완료해주세요.");
				$("#btnSendEmailCode").trigger("focus");
				return;
			}
		});
		
	}
	
	// 저장 모달
	const pageMsg = ($("#pageMsg").val() || "").trim();
	if (pageMsg) {
		Modal.open("#saveModal", pageMsg);
	}
	
	
	// 핸드폰 인증 번호 입력 숨김
	function getCurrentMobile() {
		const m1 = document.getElementById('mobile1').value;
		const m2 = document.getElementById('mobile2').value.trim();
		const m3 = document.getElementById('mobile3').value.trim();
		return `${m1}-${m2}-${m3}`;
	}

	const origin = document.getElementById('originMobile')?.value?.trim() || '';
	const smsLine = document.getElementById('smsLine');
	const mobileVerified = document.getElementById('mobileVerified');

	function updateSmsUI() {
		const cur = getCurrentMobile();
		const changed = origin && cur !== origin;

		if (changed) {
			smsLine.style.display = 'flex';	 // mp2-line2가 flex면
			mobileVerified.value = 'false';	 // 변경했으니 인증 다시 필요
		} else {
			smsLine.style.display = 'none';
		}
	}

	['mobile1','mobile2','mobile3'].forEach(id => {
		document.getElementById(id).addEventListener('input', updateSmsUI);
		document.getElementById(id).addEventListener('change', updateSmsUI);
	});

	updateSmsUI();
	
	// 이메일 인증번호 입력 숨김
	function getCurrentEmail() {
		const emailId = document.querySelector('.mp2-emailId')?.value.trim() || '';

		const select = document.querySelector('select[name="emailDomainSelect"]');
		const manualInput = document.querySelector('input[name="emailDomainInput"]');

		const selectVal = select?.value || '';
		const domain = (selectVal && selectVal !== 'manual')
			? selectVal
			: (manualInput?.value.trim() || '');

		return (emailId && domain) ? `${emailId}@${domain}` : '';
	}

	const originEmail = document.getElementById('originEmail')?.value?.trim() || '';
	const emailLine = document.getElementById('emailLine');
	const emailVerified = document.getElementById('emailVerified');

	function updateEmailUI() {
		const cur = getCurrentEmail();
		const changed = originEmail && cur && cur !== originEmail;

		if (changed) {
			emailLine.style.display = 'flex';
			emailVerified.value = 'false';
		} else {
			emailLine.style.display = 'none';
		}
	}

	['.mp2-emailId', 'input[name="emailDomainInput"]', 'select[name="emailDomainSelect"]']
		.forEach(sel => {
			const el = document.querySelector(sel);
			if (!el) return;
			el.addEventListener('input', updateEmailUI);
			el.addEventListener('change', updateEmailUI);
		});

	updateEmailUI();

	
	
});