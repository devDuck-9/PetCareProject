$(function () {
	
	const $btnRoute = document.getElementById('btnRoute');
	
	function openKakaoRouteTo(d) {
		if (!d) return;

		const name = encodeURIComponent(d.place_name);
		const lat = Number(d.y);
		const lng = Number(d.x);

		// 웹(PC/모바일 브라우저)용: 카카오맵 길찾기(목적지)로 이동
		const webUrl = `https://map.kakao.com/link/to/${name},${lat},${lng}`;

		// 앱 딥링크(모바일에서 카카오맵 앱 설치되어 있으면 앱이 열림)
		// 출발지=현위치로 두고 목적지만 지정
		const appUrl = `kakaomap://route?ep=${lat},${lng}&by=CAR`;

		const isMobile = /Android|iPhone|iPad|iPod/i.test(navigator.userAgent);

		if (isMobile) {
			// 앱 먼저 시도 → 안 열리면 웹으로 fallback
			const t = Date.now();
			location.href = appUrl;
			setTimeout(() => {
				// 앱이 안 열렸을 가능성이 크면 웹으로
				if (Date.now() - t < 1500) location.href = webUrl;
			}, 800);
		} else {
			window.open(webUrl, '_blank');
		}
	}
	
	const $mapLoading = document.getElementById('mapLoading');

	function showMapLoading(msg) {
		if (!$mapLoading) return;
		const txt = $mapLoading.querySelector('.txt');
		if (txt) txt.textContent = msg || '불러오는 중...';
		$mapLoading.classList.remove('is-hidden');
	}

	function hideMapLoading() {
		if (!$mapLoading) return;
		$mapLoading.classList.add('is-hidden');
	}

	
	function waitKakaoMapsReady(timeoutMs = 8000) {
		return new Promise((resolve, reject) => {
			const start = Date.now();
			const tick = () => {
				if (window.kakao && kakao.maps && typeof kakao.maps.load === 'function') {
					return resolve();
				}
				if (Date.now() - start > timeoutMs) {
					return reject(new Error('Kakao SDK not ready (timeout)'));
				}
				setTimeout(tick, 50);
			};
			tick();
		});
	}
	
	const PAGE_SIZE = 6;

	let state = {
		lat: null,
		lng: null,
		q: '',
		page: 1,
		total: 0,
		pageable: 0,
		isEnd: false,
		selectedId: null,
		docs: []
	};

	const $q = document.getElementById('q');
	const $btnSearch = document.getElementById('btnSearch');
	const $list = document.getElementById('list');
	const $pages = document.getElementById('pages');
	const $prev = document.getElementById('prevBtn');
	const $next = document.getElementById('nextBtn');
	const $total = document.getElementById('totalCount');

	const $detailName = document.getElementById('detailName');
	const $detailDist = document.getElementById('detailDist');
	const $detailTel = document.getElementById('detailTel');

	// ===== Kakao Map =====
	let map = null;
	let myMarker = null;
	let markers = []; // 병원 마커들
	let infoWindow = null;

	function ensureMap(lat, lng) {
		if (!window.kakao || !kakao.maps) {
			console.error('Kakao Maps SDK not loaded');
			return;
		}
		const container = document.getElementById('map');
		const center = new kakao.maps.LatLng(lat, lng);

		map = new kakao.maps.Map(container, {
			center,
			level: 4
		});

		infoWindow = new kakao.maps.InfoWindow({ zIndex: 10 });

		// 내 위치 마커
		myMarker = new kakao.maps.Marker({
			position: center,
			map
		});
	}

	function clearHospitalMarkers() {
		markers.forEach(m => m.setMap(null));
		markers = [];
	}

	function addHospitalMarkers(docs) {
		clearHospitalMarkers();
		docs.forEach((d) => {
			const pos = new kakao.maps.LatLng(Number(d.y), Number(d.x));
			const marker = new kakao.maps.Marker({ position: pos, map });

			kakao.maps.event.addListener(marker, 'click', function () {
				selectDoc(d.id);
			});

			markers.push(marker);
		});
	}

	function panTo(lat, lng) {
		if (!map) return;
		map.panTo(new kakao.maps.LatLng(lat, lng));
	}

	// ===== API =====
	function getMyLocation() {
		return new Promise((resolve, reject) => {
			if (!navigator.geolocation) {
				return reject(new Error("no geolocation"));
			}

			navigator.geolocation.getCurrentPosition(
				(pos) => {
					resolve({
						lat: pos.coords.latitude,
						lng: pos.coords.longitude
					});
				},
				(err) => {
					reject(err);
				},
				{ enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
			);
		});
	}

	async function fetchHospitals() {
		const params = new URLSearchParams({
			lat: state.lat,
			lng: state.lng,
			q: state.q || '',
			page: state.page,
			size: PAGE_SIZE
		});

		const res = await fetch(`/api/hospitals?${params.toString()}`);
		if (!res.ok) throw new Error("API error");
		return await res.json();
	}

	// ===== Render =====
	function renderList() {
		$total.textContent = String(state.total);

		$list.innerHTML = '';
		state.docs.forEach((d) => {
			const row = document.createElement('div');
			row.className = 'hos-item' + (d.id === state.selectedId ? ' is-active' : '');
			row.innerHTML = `
				<div>
					<div class="name">${d.place_name} <span class="dist">${d.distance ? d.distance + 'm' : ''}</span></div>
				</div>
				<button class="btn" type="button">맵 확인</button>
			`;
			
			// 리스트 전체 클릭
			row.addEventListener('click', () => selectDoc(d.id));
			
			// 맵 확인 버튼 전용 클릭
			row.querySelector('.btn').addEventListener('click', (e) => {
				e.stopPropagation();	// 리스트 클릭 막기
				selectDoc(d.id);		// 병원 선택
				document
					.getElementById('map')
			});
			
			$list.appendChild(row);
		});
	}

	function renderPaging() {
		// 카카오 제한: pageable_count 기준 페이지 수
		const tp = Math.max(1, Math.ceil((state.pageable || state.total) / PAGE_SIZE));
		
		$pages.innerHTML = '';
		const groupSize = 5;
		
		// 현재 페이지가 속한 그룹의 시작/끝
		const start = Math.floor((state.page - 1) / groupSize) * groupSize + 1;
		const end = Math.min(start + groupSize - 1, tp);

		for (let i = start; i <= end; i++) {
			const b = document.createElement('button');
			b.textContent = String(i);
			if (i === state.page) b.classList.add('is-active');
			b.addEventListener('click', async () => {
				if (state.isEnd && i > state.page) return;
				state.page = i;
				await load();
			});
			$pages.appendChild(b);
		}
		
		// 그룹 단위 이동: 이전 그룹이 있나? 다음 그룹이 있나?
		const hasPrevGroup = start > 1;
		const hasNextGroup = end < tp;
		
		$prev.disabled = !hasPrevGroup;
		$next.disabled = !hasNextGroup;
	}
	
	// prev/next 그룹 단위 이동
	$prev.addEventListener('click', async () => {
		const tp = Math.max(1, Math.ceil((state.pageable || state.total) / PAGE_SIZE));
		const groupSize = 5;

		const start = Math.floor((state.page - 1) / groupSize) * groupSize + 1;
		if (start <= 1) return;

		// 이전 그룹의 마지막 페이지로 점프
		state.page = start - 1;
		await load();
	});

	$next.addEventListener('click', async () => {
		// is_end면 절대 못 넘어가게
		if (state.isEnd) return;
		
		const tp = Math.max(1, Math.ceil((state.pageable || state.total) / PAGE_SIZE));
		const groupSize = 5;

		const start = Math.floor((state.page - 1) / groupSize) * groupSize + 1;
		const end = Math.min(start + groupSize - 1, tp);
		if (end >= tp) return;

		// 다음 그룹의 첫 페이지로 점프
		state.page = end + 1;
		await load();
	});

	function updateDetail(d) {
		if (!d) return;
		$detailName.textContent = d.place_name;
		$detailDist.textContent = d.distance ? d.distance + 'm' : '-';
		$detailTel.textContent = d.phone || '-';
	}

	function selectDoc(id) {
		state.selectedId = id;
		const d = state.docs.find(x => x.id === id);
		if (!d) return;

		updateDetail(d);
		
		// 길찾기 버튼 활성화 + 연결
		if ($btnRoute) {
			$btnRoute.disabled = false;
			$btnRoute.onclick = () => openKakaoRouteTo(d);
		}

		// 지도 이동 + 인포윈도우
		const lat = Number(d.y);
		const lng = Number(d.x);
		panTo(lat, lng);

		if (infoWindow && map) {
			infoWindow.setContent(`<div style="padding:6px 8px;font-size:12px;font-weight:800;">${d.place_name}</div>`);
			// 선택된 병원에 해당하는 마커를 찾아서 열기
			const idx = state.docs.findIndex(x => x.id === id);
			if (idx >= 0 && markers[idx]) {
				infoWindow.open(map, markers[idx]);
			}
		}

		renderList();
	}

	// ===== Load =====
	async function load() {
		const data = await fetchHospitals();
		state.total = data.meta?.total_count ?? 0;
		state.pageable = data.meta?.pageable_count ?? state.total;

		// documents에 id가 없을 수 있음(키가 document마다 따로 없음)
		// 그래서 안정적으로 "place_url" 또는 "x+y+name" 조합으로 id 만들기
		state.docs = (data.documents || []).map(d => ({
			...d,
			id: d.place_url || `${d.place_name}_${d.x}_${d.y}`
		}));
		
		// meta.is_end OR 현재 페이지 결과가 size보다 적으면 마지막
		state.isEnd = Boolean(data.meta?.is_end) || state.docs.length < PAGE_SIZE;
		
		state.selectedId = null;
		renderList();
		renderPaging();

		// 마커
		if (map) addHospitalMarkers(state.docs);
		
	}

	function doSearch() {
		state.q = ($q.value || '').trim();
		state.page = 1;

		showMapLoading('주변 병원을 찾는 중...');

		$detailName.textContent = '병원을 선택하세요';
		$detailDist.textContent = '-';
		$detailTel.textContent = '-';

		load()
			.then(() => hideMapLoading())
			.catch((e) => {
				console.error(e);
				hideMapLoading();
			});
	}

	// ===== Bind =====
	$btnSearch.addEventListener('click', doSearch);
	$q.addEventListener('keydown', (e) => {
		if (e.key === 'Enter') { e.preventDefault(); doSearch(); }
	});
	
	// ===== init =====
	(async function init() {
		try {
			showMapLoading('현재 위치 확인 중...');
			
			const loc = await getMyLocation();
			state.lat = loc.lat;
			state.lng = loc.lng;
			
			showMapLoading('주변 병원을 찾는 중...');
			// 목록 먼저 불러오기 OK
			await load();
			
			showMapLoading('지도를 불러오는 중...');
			// SDK 준비될 때까지 기다린 다음
			await waitKakaoMapsReady();

			kakao.maps.load(() => {
				ensureMap(state.lat, state.lng);
				addHospitalMarkers(state.docs);
				if (state.docs.length > 0) selectDoc(state.docs[0].id);
				
				// 지도 준비
				hideMapLoading();
			});

		} catch (e) {
			console.error(e);
			if (e && e.code === 1) alert('위치 권한이 필요합니다. 브라우저 설정에서 허용해주세요.');
			else alert('현재 위치를 가져오지 못했습니다. (브라우저/네트워크 상태 확인)');
		}
	})();
	
	

});