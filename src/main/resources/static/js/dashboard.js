$(function () {
	const MODAL_ID = '#pickPetModal';

	// ==============================
	// 일정 추가 버튼: 펫 선택 모달
	// ==============================
	const $btn = $('#btnAddSchedule');

	function openPickModal(pets) {
		const optionsHtml = pets
			.map(p => `<option value="${p.petSeq}">${p.petName}</option>`)
			.join('');

		const slotHtml = `
			<select class="pc-modal-select" id="pickPetSelect">
				${optionsHtml}
			</select>
		`;

		Modal.open(MODAL_ID, '반려동물을 선택해주세요.', {
			primaryText: '선택하기',
			secondaryText: '취소',
			slotHtml,
			onPrimary: function ($backdrop) {
				const v = $backdrop.find('#pickPetSelect').val();
				if (!v) return false;
				location.href = `/addScheduleForm?petSeq=${encodeURIComponent(v)}`;
			}
		});
	}

	function openNoPetModal() {
		Modal.open(MODAL_ID, '반려동물이 없습니다.\n반려동물을 등록하시겠습니까?', {
			primaryText: '등록하기',
			secondaryText: '취소',
			clearSlot: true,
			onPrimary: function () {
				location.href = '/addPetForm';
			}
		});
	}

	if ($btn.length) {
		$btn.on('click', async function (e) {
			e.preventDefault();

			try {
				const res = await fetch('/api/pets/mine', {
					headers: { 'Accept': 'application/json' }
				});

				if (!res.ok) {
					Modal.open(MODAL_ID, '펫 정보를 불러오지 못했습니다.');
					return;
				}

				const pets = await res.json();

				if (!Array.isArray(pets) || pets.length === 0) {
					openNoPetModal();
				} else {
					openPickModal(pets);
				}
			} catch (err) {
				console.error(err);
				Modal.open(MODAL_ID, '네트워크 오류가 발생했어요.');
			}
		});
	}

	// ==========================================
	// 대시보드 패널 페이징 : fragment 비동기 교체
	// ==========================================
	const panels = {
		pets: {
			wrapSelector: '#petPanelWrap',
			loadingSelector: '.pc-loading[data-loading="pets"]'
		},
		schedules: {
			wrapSelector: '#schedulePanelWrap',
			loadingSelector: '.pc-loading[data-loading="schedules"]'
		},
		posts: {
			wrapSelector: '#postPanelWrap',
			loadingSelector: '.pc-loading[data-loading="posts"]'
		}
	};

	function setLoading(key, isLoading) {
		const cfg = panels[key];
		if (!cfg) return;

		const $wrap = $(cfg.wrapSelector);
		const $loading = $wrap.closest('.panel-body').find(cfg.loadingSelector);

		if (!$loading.length) return;

		if (isLoading) {
			$loading.addClass('show').show();
		} else {
			$loading.removeClass('show').hide();
		}
	}

	async function loadFragmentInto(key, url) {
		const cfg = panels[key];
		if (!cfg) return;

		const $wrap = $(cfg.wrapSelector);
		if (!$wrap.length) return;

		try {
			setLoading(key, true);

			const res = await fetch(url, {
				headers: { 'X-Requested-With': 'XMLHttpRequest' }
			});

			if (!res.ok) {
				throw new Error(`HTTP ${res.status}`);
			}

			const html = await res.text();

			// 서버가 fragment 만 반환하도록 컨트롤러에서 "views/dashboard/fragments :: xxx" 를 리턴 중이라
			// 여기서는 받은 HTML 을 wrap 영역에 그대로 교체
			$wrap.html(html);
		} catch (err) {
			console.error(err);
			// 사용자에게 너무 시끄럽지 않게 안내
			Modal.open(MODAL_ID, '내용을 불러오지 못했습니다.\n잠시 후 다시 시도해주세요.');
		} finally {
			setLoading(key, false);
		}
	}

	/**
	 * 이벤트 위임
	 * - fragment 가 교체되어도 pager 클릭 이벤트가 계속 살아있도록 wrap 에 위임한다.
	 * 그래서 fragments.html에서도 링크가 동작할 수 있도록 /dashboard/... 로 맞춰주었다.
	 */
	function bindPager(key) {
		const cfg = panels[key];
		const $wrap = $(cfg.wrapSelector);
		if (!$wrap.length) return;

		$wrap.on('click', '.pager a', function (e) {
			const href = $(this).attr('href');
			if (!href) return;

			e.preventDefault();

			// href에서 page 값 뽑기 (petPage/schPage/postPage)
			const u = new URL(href, location.origin);
			const page =
				u.searchParams.get('petPage') ||
				u.searchParams.get('schPage') ||
				u.searchParams.get('postPage') ||
				'1';

			// 1) URL을 /?petPage=2 형태로 히스토리에 기록
			if (key === 'pets') updateUrlParam('petPage', page);
			if (key === 'schedules') updateUrlParam('schPage', page);
			if (key === 'posts') updateUrlParam('postPage', page);

			// 2) 실제 fragment는 기존대로 /dashboard/...로 fetch
			loadFragmentInto(key, href);
		});
		
	}
	
	function updateUrlParam(param, value) {
		const u = new URL(location.href);
		u.searchParams.set(param, value);
		history.pushState({ [param]: value }, '', u.pathname + '?' + u.searchParams.toString());
	}

	function parseQuery() {
		const u = new URL(location.href);
		return {
			petPage: u.searchParams.get('petPage'),
			schPage: u.searchParams.get('schPage'),
			postPage: u.searchParams.get('postPage')
		};
	}

	function buildFragmentUrl(key, page) {
		if (key === 'pets') return `/dashboard/pets?petPage=${encodeURIComponent(page)}`;
		if (key === 'schedules') return `/dashboard/schedules?schPage=${encodeURIComponent(page)}`;
		if (key === 'posts') return `/dashboard/today-posts?postPage=${encodeURIComponent(page)}`;
		return '/';
	}

	bindPager('pets');
	bindPager('schedules');
	bindPager('posts');
	
	window.addEventListener('popstate', function () {
		const q = parseQuery();

		// 쿼리가 없으면 1페이지로
		loadFragmentInto('pets', buildFragmentUrl('pets', q.petPage || 1));
		loadFragmentInto('schedules', buildFragmentUrl('schedules', q.schPage || 1));
		loadFragmentInto('posts', buildFragmentUrl('posts', q.postPage || 1));
	});
	
});