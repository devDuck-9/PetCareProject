$(function () {
	const $toast = $('#scheduleToast');
	const $list	= $('#scheduleToastList');

	const POLL_MS = 60000;

	function closeToast() {
		$toast.removeClass('is-show');
		setTimeout(() => $toast.hide(), 220);
	}

	async function hideToday() {
		try {
			const res = await fetch('/api/schedules/toast/hide-today', {
				method: 'POST',
				headers: { 'Accept': 'application/json' }
			});
			// 서버 저장 성공/실패와 상관없이 UI는 닫기
		} catch (e) {
			// 네트워크 에러여도 UI는 닫기
		}
		closeToast();
	}

	$('#btnToastDismissToday').on('click', hideToday);
	$('#btnToastClose').on('click', closeToast);

	// 리스트 아이템 클릭하면 상세로 이동 (이벤트 위임)
	$toast.on('click', '.pc-toast__item', function () {
		const id = $(this).data('id');
		if (!id) return;
		location.href = `/detailSchedule/${id}`;
	});

	function showToast() {
		$toast.show();
		requestAnimationFrame(() => $toast.addClass('is-show'));
	}

	// XSS 방지용 escape
	function escapeHtml(str) {
		return String(str)
			.replaceAll('&', '&amp;')
			.replaceAll('<', '&lt;')
			.replaceAll('>', '&gt;')
			.replaceAll('"', '&quot;')
			.replaceAll("'", '&#039;');
	}

	let lastSignature = '';

	async function pollSoon() {

		try {
			const res = await fetch('/api/schedules/soon', { headers: { 'Accept': 'application/json' } });
			if (!res.ok) return;

			const data = await res.json();
			
			if (data && data.hiddenToday) {
				closeToast();
				$list.empty();
				lastSignature = '';
				return;
			}
			
			if (!data || !data.count || data.count <= 0) {
				closeToast();
				$list.empty();
				lastSignature = '';
				return;
			}

			// 중복 렌더 방지 (date + ids)
			const ids = (data.items || []).map(x => x.scheduleSeq).join(',');
			const sig = `${data.date}|${data.count}|${ids}`;
			if (sig === lastSignature) return;
			lastSignature = sig;

			// 리스트 렌더
			$list.empty();

			(data.items || []).forEach(item => {
				const title = escapeHtml(item.title || '일정');
				$list.append(`
					<li class="pc-toast__item" data-id="${item.scheduleSeq}">
						<span class="pc-toast__text">TITLE&nbsp&nbsp&nbsp;:&nbsp&nbsp&nbsp;${title}</span>
						<span class="pc-toast__arrow">&gt;</span>
					</li>
				`);
			});

			// 3개 초과면 +N건 표시
			const $more = $('#scheduleToastMore');
			
			const shown = (data.items || []).length;
			if (data.count > shown) {
				$more.text(`+ ${data.count - shown}건 더 있습니다.`).show();
			}else {
				$more.text('').hide(); // 없으면 비우기
			}

			showToast();
		} catch (e) {}
	}

	pollSoon();
	setInterval(pollSoon, POLL_MS);
	window.addEventListener('pageshow', pollSoon);
});
