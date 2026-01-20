$(function () {
	const MODAL_ID = '#postModal';

	const $form = $('#postForm');
	const $category = $('#categorySelect');
	const $title = $('#title');
	const $content = $('#content');
	
	// ==============================
	// 이미지 누적첨부 + 미리보기 + 개별삭제(X) (고유ID 방식)
	// 이미지 갯수 제한
	// ==============================
	const $images = document.getElementById('images');
	const $preview = document.getElementById('imagePreview');
	const MAX_IMAGES = 5;
	
	let items = [];	// [{ id, file }]
	let uid = 0;		// 고유번호 발급용

	function rebuildInputFiles() {
		const next = new DataTransfer();
		items.forEach(({ file }) => next.items.add(file));
		$images.files = next.files;
	}

	function renderPreview() {
		if (!$preview) return;
		$preview.innerHTML = '';

		items.forEach(({ id, file }) => {
			if (!file.type.startsWith('image/')) return;

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
				// id로 정확히 1개만 제거
				items = items.filter(x => x.id !== id);
				rebuildInputFiles();
				renderPreview();
			});

			const reader = new FileReader();
			reader.onload = (e) => (img.src = e.target.result);
			reader.readAsDataURL(file);

			wrap.appendChild(img);
			wrap.appendChild(btn);
			$preview.appendChild(wrap);
		});
	}

	if ($images) {
		$images.addEventListener('change', () => {
			const selected = [...$images.files];

			selected.forEach((file) => {
				if (!file.type.startsWith('image/')) return;
				
				// 개수 제한 체크 (추가 전에 검사)
				if (items.length >= MAX_IMAGES) {
					Modal.open('#postModal', `사진은 최대 ${MAX_IMAGES}장까지 첨부할 수 있어요.`);
					return;
				}
				
				// 완전 동일 파일 중복 방지
				const exists = items.some(x => x.file.name === file.name && x.file.size === file.size);
				if (exists) return;

				items.push({ id: ++uid, file });
			});

			rebuildInputFiles();
			renderPreview();

		});
	}
	
	// 글자수 제한
	const MAX_TITLE = 100;
	const MAX_CONTENT = 10000;

	function openError(msg, $focusEl) {
		Modal.open(MODAL_ID, msg);
		setTimeout(function () {
			if ($focusEl && $focusEl.length) $focusEl.trigger('focus');
		}, 0);
	}

	function lenOf(v) {
		return (v || '').length;
	}

	// 입력 중 초과 방지 + 카운터
	function bindCounter($el, $counter, max) {
		if (!$el.length) return;

		function update() {
			let v = $el.val() || '';
			if (v.length > max) {
				v = v.substring(0, max);
				$el.val(v);
			}
			if ($counter && $counter.length) {
				$counter.text(v.length + '/' + max);
			}
		}

		$el.on('input', update);
		update();
	}

	// 카운터
	bindCounter($title, $('#titleCounter'), MAX_TITLE);
	bindCounter($content, $('#contentCounter'), MAX_CONTENT);

	$form.on('submit', function (e) {
		const cat = ($category.val() || '').trim();
		const title = ($title.val() || '').trim();
		const content = ($content.val() || '').trim();

		if (!cat) {
			e.preventDefault();
			openError('카테고리를 선택해주세요.', $category);
			return;
		}

		if (!title) {
			e.preventDefault();
			openError('제목을 입력해주세요.', $title);
			return;
		}

		if (lenOf(title) > MAX_TITLE) {
			e.preventDefault();
			openError(`제목은 최대 ${MAX_TITLE}자까지 입력할 수 있어요.`, $title);
			return;
		}

		if (!content) {
			e.preventDefault();
			openError('본문을 입력해주세요.', $content);
			return;
		}

		if (lenOf(content) > MAX_CONTENT) {
			e.preventDefault();
			openError(`본문은 최대 ${MAX_CONTENT}자까지 입력할 수 있어요.`, $content);
			return;
		}
	});
});
