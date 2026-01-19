$(function () {
	const MODAL_ID = '#postModal';

	const $form = $('#postEditForm');
	const $category = $('#categorySelect');
	const $title = $('#title');
	const $content = $('#content');

	const MAX_TITLE = 100;
	const MAX_CONTENT = 10000;
	
	const $images = document.getElementById('images');
	const $preview = document.getElementById('imagePreview');
	const $fileLabel = document.getElementById('fileLabel');
	const $removeOldInputs = document.getElementById('removeOldInputs');
	
	const MAX_IMAGES = 5;
	
	let items = []; // old/new 섞음
	let uid = 0;
	
	// -----------------------
	// helpers
	// -----------------------
	function addRemoveOldInput(imageSeq) {
		if (!$removeOldInputs) return;
		const input = document.createElement('input');
		input.type = 'hidden';
		input.name = 'removeImageSeqs';
		input.value = String(imageSeq);
		input.dataset.seq = String(imageSeq);
		$removeOldInputs.appendChild(input);
	}

	function removeRemoveOldInput(imageSeq) {
		if (!$removeOldInputs) return;
		const el = $removeOldInputs.querySelector(`input[name="removeImageSeqs"][data-seq="${imageSeq}"]`);
		if (el) el.remove();
	}

	function fileKey(file) {
		return `${file.name}__${file.size}__${file.lastModified}`;
	}

	function countTotalImages() {
		return items.length;
	}

	function countNewFiles() {
		return items.filter(x => x.type === 'new').length;
	}

	function updateFileLabel() {
		if (!$fileLabel) return;

		const n = countTotalImages();
		if (n === 0) $fileLabel.textContent = '선택된 파일 없음';
		else if (n === 1) {
			const one = items[0];
			$fileLabel.textContent = one.type === 'new' ? one.file.name : '기존 이미지 1장';
		} else {
			$fileLabel.textContent = `파일 ${n}개`;
		}
	}

	function rebuildInputFiles() {
		// input.files는 "새 파일"만 담아야 함
		if (!$images) return;
		const dt = new DataTransfer();
		items.filter(x => x.type === 'new').forEach(x => dt.items.add(x.file));
		$images.files = dt.files;
	}

	function renderPreview() {
		if (!$preview) return;
		$preview.innerHTML = '';

		items.forEach((it) => {
			const wrap = document.createElement('div');
			wrap.style.position = 'relative';
			wrap.style.width = '90px';
			wrap.style.height = '90px';

			const img = document.createElement('img');
			img.style.width = '90px';
			img.style.height = '90px';
			img.style.objectFit = 'cover';
			img.style.borderRadius = '10px';
			img.style.border = '1px solid rgba(0,0,0,.15)';

			// src 결정
			if (it.type === 'old') {
				img.src = it.imagePath; // 서버 저장 경로
			} else {
				const reader = new FileReader();
				reader.onload = (e) => (img.src = e.target.result);
				reader.readAsDataURL(it.file);
			}

			// X 버튼
			const btn = document.createElement('button');
			btn.type = 'button';
			btn.textContent = '×';
			btn.style.position = 'absolute';
			btn.style.top = '6px';
			btn.style.right = '6px';
			btn.style.width = '22px';
			btn.style.height = '22px';
			btn.style.borderRadius = '999px';
			btn.style.border = '0';
			btn.style.cursor = 'pointer';
			btn.style.fontWeight = '900';
			btn.style.lineHeight = '22px';
			btn.style.padding = '0';
			btn.style.background = 'rgba(0,0,0,.6)';
			btn.style.color = '#fff';

			btn.addEventListener('click', () => {
				//	1장만 제거
				items = items.filter(x => x.id !== it.id);

				//	기존이미지면 삭제목록에 추가
				if (it.type === 'old') {
					addRemoveOldInput(it.imageSeq);
				}

				rebuildInputFiles();
				renderPreview();
				updateFileLabel();
			});

			wrap.appendChild(img);
			wrap.appendChild(btn);
			$preview.appendChild(wrap);
		});

		updateFileLabel();
	}

	// -----------------------
	// 1) 기존 이미지 preload
	// -----------------------
	const old = window.__OLD_IMAGES__ || [];
	old.forEach((img) => {
		// img: { imageSeq, postSeq, imagePath, sortOrder, createdAt }
		items.push({
			id: ++uid,
			type: 'old',
			imageSeq: img.imageSeq,
			imagePath: img.imagePath
		});
	});

	// 처음 렌더
	renderPreview();
	updateFileLabel();

	// -----------------------
	// 2) 새 파일 누적첨부
	// -----------------------
	if ($images) {
		$images.addEventListener('change', () => {
			const selected = [...$images.files];

			selected.forEach((file) => {
				if (!file.type.startsWith('image/')) return;

				//	전체 개수 제한(기존+새 합쳐서)
				if (countTotalImages() >= MAX_IMAGES) {
					Modal.open(MODAL_ID, `사진은 최대 ${MAX_IMAGES}장까지 첨부할 수 있어요.`);
					return;
				}

				//	새 파일 중복 방지
				const exists = items.some(x => x.type === 'new' && fileKey(x.file) === fileKey(file));
				if (exists) return;

				items.push({ id: ++uid, type: 'new', file });
			});

			rebuildInputFiles();
			renderPreview();
			
		});
		updateFileLabel();
	}
	
	function openError(msg, $focusEl) {
		Modal.open(MODAL_ID, msg);
		setTimeout(function () {
			if ($focusEl && $focusEl.length) $focusEl.trigger('focus');
		}, 0);
	}

	const $btnPickImages = document.getElementById('btnPickImages');
	if ($btnPickImages && $images) {
		$btnPickImages.addEventListener('click', () => $images.click());
	}
	
	function bindCounter($el, $counter, max) {
		function update() {
			let v = $el.val() || '';
			if (v.length > max) {
				v = v.substring(0, max);
				$el.val(v);
			}
			$counter.text(v.length + '/' + max);
		}
		$el.on('input', update);
		update(); // 초기값(기존 글) 카운트 표시
	}

	bindCounter($title, $('#titleCounter'), MAX_TITLE);
	bindCounter($content, $('#contentCounter'), MAX_CONTENT);

	$form.on('submit', function (e) {
		const cat = ($category.val() || '').trim();
		const title = ($title.val() || '').trim();
		const content = ($content.val() || '').trim();

		if (!cat) { e.preventDefault(); openError('카테고리를 선택해주세요.', $category); return; }
		if (!title) { e.preventDefault(); openError('제목을 입력해주세요.', $title); return; }
		if (title.length > MAX_TITLE) { e.preventDefault(); openError(`제목은 최대 ${MAX_TITLE}자까지 입력할 수 있어요.`, $title); return; }
		if (!content) { e.preventDefault(); openError('본문을 입력해주세요.', $content); return; }
		if (content.length > MAX_CONTENT) { e.preventDefault(); openError(`본문은 최대 ${MAX_CONTENT}자까지 입력할 수 있어요.`, $content); return; }
	});
	
});
