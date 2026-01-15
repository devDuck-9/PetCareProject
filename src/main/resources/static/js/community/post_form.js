$(function () {
	const MODAL_ID = '#postModal';

	const $form = $('#postForm');
	const $category = $('#categorySelect');
	const $title = $('#title');
	const $content = $('#content');

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
