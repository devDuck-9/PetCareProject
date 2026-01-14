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
}

if ($m2.length) $m2.on('input', function(){ syncMobile(); if ($mErr.length) $mErr.hide(); });
if ($m3.length) $m3.on('input', function(){ syncMobile(); if ($mErr.length) $mErr.hide(); });
if ($m1.length) $m1.on('change', function(){ syncMobile(); if ($mErr.length) $mErr.hide(); });

syncMobile();

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
	});
}