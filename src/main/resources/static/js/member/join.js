// 아이디 중복확인 상태 관리
const $idInput	= $('input[name="userId"]');
const $checkBtn = $('#btnCheckId');

// 중복확인 완료 여부
let isIdChecked = false;

// 변경되는 아이디값
let lastUserId = '';

// 버튼 상태 초기화
function resetIdCheck() {
	isIdChecked = false;

	if ($checkBtn.length) {
		$checkBtn.prop('disabled', false);
		$checkBtn.removeClass('is-done');
		$checkBtn.text('중복확인');
	}
}

// 아이디 입력이 바뀌면 다시 확인 필요
if ($idInput.length) {
	lastUserId = $idInput.val();

	$idInput.on('input', function () {
		const now = $(this).val();
		if (now !== lastUserId) {
			resetIdCheck();
			lastUserId = now;
		}
	});
}

// 중복확인 버튼 클릭
if ($checkBtn.length) {
	$checkBtn.on('click', async function (e) {
		e.preventDefault();

		if (isIdChecked) return;

		const userId = ($idInput.length ? $idInput.val() : '').trim();

		if (!userId) {
			Modal.open('#idCheckModal', '아이디를 입력해주세요.');
			if ($idInput.length) $idInput.trigger('focus');
			return;
		}

		try {
			const res = await fetch(
				`/api/member/check-id?userId=${encodeURIComponent(userId)}`,
				{ headers: { 'Accept': 'application/json' } }
			);

			if (!res.ok) {
				Modal.open('#idCheckModal', '서버 오류가 발생했어요.');
				return;
			}

			const data = await res.json();
			Modal.open('#idCheckModal', data.message);

			if (data.exists === false) {
				isIdChecked = true;

				if ($checkBtn.length) {
					$checkBtn.addClass('is-done');
					$checkBtn.text('확인완료 !');
				}
			}

		} catch (err) {
			Modal.open('#idCheckModal', '네트워크 오류가 발생했어요.');
		}
	});
}

// 비밀번호 확인 메세지
const $form = $('.join-form');
const $pw1	= $form.find('input[name="password"]');
const $pw2	= $form.find('input[name="passwordConfirm"]');
const $err	= $('#pwError');

function validatePw() {
	if (!$pw1.length || !$pw2.length || !$err.length) return true;

	const show = $pw1.val() && $pw2.val() && $pw1.val() !== $pw2.val();
	$err.css('display', show ? 'block' : 'none');
	return !show;
}

if ($pw1.length) $pw1.on('input', validatePw);
if ($pw2.length) $pw2.on('input', validatePw);

// 이메일 도메인
const $domainInput	= $('input[name="emailDomainInput"]');
const $domainSelect = $('select[name="emailDomainSelect"]');
const $domainFinal	= $('#emailDomainFinal');

function setManualMode() {
	if (!$domainSelect.length || !$domainInput.length || !$domainFinal.length) return;

	$domainInput.val('');
	$domainInput.prop('readonly', false);
	$domainInput.attr('placeholder', '도메인 직접입력');
	$domainInput.trigger('focus');

	$domainFinal.val('');
}

function setPresetMode(domain) {
	if (!$domainSelect.length || !$domainInput.length || !$domainFinal.length) return;

	$domainInput.val(domain);
	$domainInput.prop('readonly', true);
	$domainInput.attr('placeholder', '');

	$domainFinal.val(domain);
}

function syncDomainFromSelect() {
	if (!$domainSelect.length || !$domainInput.length || !$domainFinal.length) return;

	const v = $domainSelect.val();
	if (v === 'manual') setManualMode();
	else setPresetMode(v);
}

if ($domainSelect.length) $domainSelect.on('change', syncDomainFromSelect);

if ($domainInput.length) {
	$domainInput.on('input', function () {
		if (!$domainFinal.length || !$domainSelect.length) return;
		if ($domainSelect.val() === 'manual') {
			$domainFinal.val($domainInput.val().trim());
		}
	});
}

syncDomainFromSelect();

function getFullEmail() {
	const id = $('input[name="emailId"]').val()?.trim();
	const domain = $('#emailDomainFinal').val()?.trim();
	if (!id || !domain) return '';
	return `${id}@${domain}`;
}

// --------------------
// 이메일 인증
// --------------------
const $emailSendBtn = $('#btnSendEmailCode');
const $emailVerifyBtn = $('#btnVerifyEmailCode');
const $emailCodeInput = $('#emailCode');
const $emailMsg = $('#emailMsg');
const $emailVerified = $('#emailVerified');

let lastEmail = '';
let sendEmaildownTimer = null;

if ($emailCodeInput.length) {
	$emailCodeInput.on('input', function () {
		this.value = this.value.replace(/\D/g, '').slice(0, 6);
	});
}

/* 이메일 재발송 시간 제한 */
function setSendEmaildown(seconds) {
	if (!$emailSendBtn.length) return;
	if (sendEmaildownTimer) {
		clearInterval(sendEmaildownTimer);
		sendEmaildownTimer = null;
	}

	let remain = seconds;
	$emailSendBtn.prop('disabled', true);
	$emailSendBtn.text(`재발송 (${remain}s)`);

	sendEmaildownTimer = setInterval(() => {
		remain -= 1;
		if (remain <= 0) {
			clearInterval(sendEmaildownTimer);
			sendEmaildownTimer = null;
			$emailSendBtn.prop('disabled', false);
			$emailSendBtn.text('인증번호 발송');
			return;
		}
		$emailSendBtn.text(`재발송 (${remain}s)`);
	}, 1000);
}

function showEmailMsg(text) {
	if ($emailMsg.length) $emailMsg.text(text).show();
}

function resetEmailVerify() {
	if ($emailVerified.length) $emailVerified.val('false');
	if ($emailCodeInput.length) $emailCodeInput.val('').prop('disabled', false);
	if ($emailMsg.length) $emailMsg.hide();
	if ($emailVerifyBtn.length) $emailVerifyBtn.prop('disabled', false).text('확인');
}

// 이메일 변경 시 인증 무효
$('input[name="emailId"], input[name="emailDomainInput"], select[name="emailDomainSelect"]').on('change input', function () {
	const now = getFullEmail();
	if (now !== lastEmail) {
		resetEmailVerify();
		lastEmail = now;
	}
});

// 발송
$emailSendBtn.on('click', async function () {
	const $btn = $(this);

	// 이미 카운트다운 중이면 무시
	if ($btn.prop('disabled')) return;

	const email = getFullEmail();
	if (!email) {
		showEmailMsg('이메일을 먼저 정확히 입력해주세요.');
		return;
	}

	// 전송중
	$btn.prop('disabled', true).text('전송중...');

	try {
		const res = await fetch('/api/email/send', {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ email })
		});

		const data = await res.json().catch(() => ({}));

		if (!res.ok) {
			showEmailMsg(data.message || '이메일 발송 실패');
			// 실패면 버튼 원복
			$btn.prop('disabled', false).text('인증번호 발송');
			return;
		}

		resetEmailVerify();
		showEmailMsg(data.message || '인증번호를 이메일로 발송했어요.');

		// 성공하면 재발송 카운트다운 시작 (60초)
		setSendEmaildown(60);
		
	} catch (e) {
		showEmailMsg('네트워크 오류');
		// 예외면 버튼 원복
		$btn.prop('disabled', false).text('인증번호 발송');
	}
});

// 확인
$emailVerifyBtn.on('click', async function () {
	const email = getFullEmail();
	const code = $emailCodeInput.val().replace(/\D/g, '').trim();

	if (!email) return showEmailMsg('이메일을 입력해주세요.');
	if (!/^\d{6}$/.test(code)) return showEmailMsg('인증번호 6자리를 입력해주세요.');

	try {
		const res = await fetch('/api/email/verify', {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({ email, code })
		});

		const data = await res.json().catch(() => ({}));
		if (!res.ok) {
			showEmailMsg(data.message || '인증 실패');
			return;
		}

		$emailVerified.val('true');
		$emailVerifyBtn.text('완료').prop('disabled', true);
		$emailCodeInput.prop('disabled', true);
		showEmailMsg(data.message);
	} catch (e) {
		showEmailMsg('네트워크 오류');
	}
});

// --------------------
// 휴대폰(select + input)
// --------------------
const $m1 = $('#mobile1');
const $m2 = $('#mobile2');
const $m3 = $('#mobile3');
const $mFinal = $('#mobileFinal');
const $mErr = $('#mobileError');

function syncMobile() {
	if (!$m1.length || !$m2.length || !$m3.length || !$mFinal.length) return;

	const v1 = $m1.val();
	const v2 = ($m2.val() || '').replace(/\D/g, '').slice(0, 4);
	const v3 = ($m3.val() || '').replace(/\D/g, '').slice(0, 4);

	$m2.val(v2);
	$m3.val(v3);

	// 가운데 3~4자리, 끝 4자리
	if (v2.length >= 3 && v3.length === 4) $mFinal.val(`${v1}-${v2}-${v3}`);
	else $mFinal.val('');
	
	// 번호가 바뀌면 인증 무효 처리
	const nowMobile = $mFinal.val();
	if (nowMobile !== mobileLast) {
		resetSmsVerification();
		mobileLast = nowMobile;
	}
	
}

// 숫자만 입력
$('#smsCode').on('input', function () {
	this.value = this.value.replace(/\D/g, '').slice(0, 6);
});

// 휴대폰 인증
const $sendBtn = $('#btnSendCode');
const $verifyBtn = $('#btnVerifyCode');
const $codeInput = $('#smsCode');
const $smsMsg = $('#smsMsg');
const $mobileVerified = $('#mobileVerified');

let mobileLast = '';
let sendCooldownTimer = null;

function hideSmsMsg() {
	if ($smsMsg.length) $smsMsg.hide();
}

function showSmsMsg(text) {
	if (!$smsMsg.length) return;
	$smsMsg.text(text).show();
}

function resetSmsVerification() {
	if ($mobileVerified.length) $mobileVerified.val('false');
	if ($codeInput.length) $codeInput.val('');
	hideSmsMsg();
	if ($verifyBtn.length) $verifyBtn.prop('disabled', false);
}

function setSendCooldown(seconds) {
	if (!$sendBtn.length) return;
	if (sendCooldownTimer) {
		clearInterval(sendCooldownTimer);
		sendCooldownTimer = null;
	}

	let remain = seconds;
	$sendBtn.prop('disabled', true);
	$sendBtn.text(`재발송 (${remain}s)`);

	sendCooldownTimer = setInterval(() => {
		remain -= 1;
		if (remain <= 0) {
			clearInterval(sendCooldownTimer);
			sendCooldownTimer = null;
			$sendBtn.prop('disabled', false);
			$sendBtn.text('인증번호 발송');
			return;
		}
		$sendBtn.text(`재발송 (${remain}s)`);
	}, 1000);
}

// --------------------
// 휴대폰 인증
// --------------------
if ($sendBtn.length) {
	$sendBtn.on('click', async function () {
		syncMobile();
		const mobile = ($mFinal.length ? $mFinal.val() : '').trim();
		if (!mobile) {
			showSmsMsg('휴대폰 번호를 먼저 정확히 입력해주세요.');
			if ($m2.length) $m2.trigger('focus');
			return;
		}

		try {
			const res = await fetch('/api/sms/send', {
				method: 'POST',
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
				body: new URLSearchParams({ mobile })
			});

			const data = await res.json().catch(() => ({}));
			
			// 콘솔로 SMS 인증코드 찍어주기
			if (data.devCode) {
				console.log('[DEV SMS CODE] =', data.devCode);
			}
			
			if (!res.ok) {
				showSmsMsg(data.message || '인증번호 발송에 실패했어요.');
				return;
			}

			resetSmsVerification();
			showSmsMsg(data.message || '인증번호를 발송했어요.');
			setSendCooldown(60);
		} catch (e) {
			showSmsMsg('네트워크 오류가 발생했어요.');
		}
	});
}

if ($verifyBtn.length) {
	$verifyBtn.on('click', async function () {
		syncMobile();
		const mobile = ($mFinal.length ? $mFinal.val() : '').trim();
		const code = ($codeInput.length ? $codeInput.val() : '').replace(/\D/g, '').trim();

		if (!mobile) return showSmsMsg('휴대폰 번호를 먼저 입력해주세요.');
		if (!/^\d{6}$/.test(code)) return showSmsMsg('인증번호 6자리를 입력해주세요.');

		try {
			const res = await fetch('/api/sms/verify', {
				method: 'POST',
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
				body: new URLSearchParams({ mobile, code })
			});

			const data = await res.json().catch(() => ({}));
			if (!res.ok) {
				if ($mobileVerified.length) $mobileVerified.val('false');
				showSmsMsg(data.message || '인증에 실패했어요.');
				return;
			}

			if ($mobileVerified.length) $mobileVerified.val('true');
			showSmsMsg(data.message || '인증 완료! ✅');
			if ($verifyBtn.length) $verifyBtn.prop('disabled', true);
		} catch (e) {
			showSmsMsg('네트워크 오류가 발생했어요.');
		}
	});
}

if ($m2.length) $m2.on('input', function(){ syncMobile(); if ($mErr.length) $mErr.hide(); });
if ($m3.length) $m3.on('input', function(){ syncMobile(); if ($mErr.length) $mErr.hide(); });
if ($m1.length) $m1.on('change', function(){ syncMobile(); if ($mErr.length) $mErr.hide(); });

syncMobile();


// --------------------
// 주소 찾기 (다음 우편번호)
// --------------------
$('#btnSearchAddress').on('click', function () {
	new daum.Postcode({
		oncomplete: function (data) {
			const zipcode = data.zonecode;
			const addr = data.roadAddress || data.jibunAddress;

			$('#zipcode').val(zipcode);
			$('#address1').val(addr);
			$('#address2').val('').focus();
		}
	}).open();
});


// --------------------
// 이름(닉네임) 중복확인
// --------------------
const $nameInput = $('input[name="userName"]');
const $nameBtn	 = $('#btnCheckName');

let isNameChecked = false;
let lastUserName	= '';

function resetNameCheck() {
	isNameChecked = false;
	if ($nameBtn.length) {
		$nameBtn.prop('disabled', false);
		$nameBtn.removeClass('is-done');
		$nameBtn.text('중복확인');
	}
}

if ($nameInput.length) {
	lastUserName = $nameInput.val();
	$nameInput.on('input', function () {
		const now = $(this).val();
		if (now !== lastUserName) {
			resetNameCheck();
			lastUserName = now;
		}
	});
}

if ($nameBtn.length) {
	$nameBtn.on('click', async function (e) {
		e.preventDefault();
		if (isNameChecked) return;

		const userName = ($nameInput.length ? $nameInput.val() : '').trim();
		if (!userName) {
			Modal.open('#nameCheckModal', '이름(닉네임)을 입력해주세요.');
			$nameInput.trigger('focus');
			return;
		}

		try {
			const res = await fetch(
				`/api/member/check-name?userName=${encodeURIComponent(userName)}`,
				{ headers: { 'Accept': 'application/json' } }
			);

			if (!res.ok) {
				Modal.open('#nameCheckModal', '서버 오류가 발생했어요.');
				return;
			}

			const data = await res.json();
			Modal.open('#nameCheckModal', data.message);

			if (data.exists === false) {
				isNameChecked = true;
				$nameBtn.addClass('is-done');
				$nameBtn.text('확인완료 !');
			}
		} catch (err) {
			Modal.open('#nameCheckModal', '네트워크 오류가 발생했어요.');
		}
	});
}

// --------------------
// submit 이벤트
// --------------------
if ($form.length) {
	$form.on('submit', function (e) {
		// 비밀번호 불일치 방지
		const pwOk = validatePw();
		if (!pwOk) {
			e.preventDefault();
			return;
		}

		// 휴대폰 hidden 동기화 + 검증
		syncMobile();
		if ($mFinal.length && !$mFinal.val()) {
			e.preventDefault();
			if ($mErr.length) $mErr.show();
			if ($m2.length) $m2.trigger('focus');
			return;
		}

		// 이메일 도메인 검증 (manual일 때만)
		if ($domainSelect.length && $domainSelect.val() === 'manual') {
			const d = $domainInput.length ? $domainInput.val().trim() : '';
			if (!d) {
				e.preventDefault();
				if ($domainInput.length) $domainInput.trigger('focus');
				Modal.open('#emailCheckModal', '이메일 도메인을 입력해주세요.');
				return;
			}
		}

		// 아이디 중복확인
		if (!isIdChecked) {
			e.preventDefault();
			Modal.open('#idCheckModal', '아이디 중복확인을 먼저 해주세요.');
			return;
		}
		
		// 이름 중복확인
		if (!isNameChecked) {
			e.preventDefault();
			Modal.open('#nameCheckModal', '이름(닉네임) 중복확인을 먼저 해주세요.');
			return;
		}
		
		// 휴대폰 인증
		if ($mobileVerified.length && $mobileVerified.val() !== 'true') {
			e.preventDefault();
			showSmsMsg('휴대폰 인증을 완료해주세요.');
			if ($codeInput.length) $codeInput.trigger('focus');
			return;
		}
		
		// 이메일 인증
		if ($('#emailVerified').val() !== 'true') {
			e.preventDefault();
			showEmailMsg('이메일 인증을 완료해주세요.');
			return;
		}
		
	});
}