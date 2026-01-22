$(function () {
	
	// 검색/필터
	const $form = $('#adFilterForm');

	function getParams(page) {
		const params = {
			category: $form.find('select[name="category"]').val(),
			type: $form.find('select[name="type"]').val(),
			keyword: ($form.find('input[name="keyword"]').val() || '').trim(),
			sort: $form.find('select[name="sort"]').val(),
			page: page || 1
		};
		return params;
	}

	function parseQuery() {
		const u = new URL(location.href);
		return {
			category: u.searchParams.get('category'),
			type: u.searchParams.get('type'),
			keyword: u.searchParams.get('keyword'),
			sort: u.searchParams.get('sort'),
			page: u.searchParams.get('page')
		};
	}

	function syncFormFromQuery(q) {
		// 값이 없으면 기존 폼 값 유지
		if (q.category != null && q.category !== '') $form.find('select[name="category"]').val(q.category);
		if (q.type != null && q.type !== '') $form.find('select[name="type"]').val(q.type);
		if (q.sort != null && q.sort !== '') $form.find('select[name="sort"]').val(q.sort);
		if (q.keyword != null) $form.find('input[name="keyword"]').val(q.keyword);

		// hidden page 값
		if (q.page != null && q.page !== '') $form.find('input[name="page"]').val(q.page);
	}

	function pushUrl(params) {
		// 주소는 /admin/dashboard?... 형태로 유지
		const qs = $.param(params);
		history.pushState(params, '', '/admin/dashboard?' + qs);
	}

	/**
	 * 목록(fragment) 로드
	 * - pushState 는 옵션으로 끌 수 있게 해서 popstate 시 무한루프 방지
	 */
	function loadList(page, opts) {
		opts = opts || {};
		const params = getParams(page);
		params.page = page || 1;

		$.get('/admin/dashboard/list-fragment', params, function (html) {
			$('#adminListArea').html(html);
			$('#adminPostDetailArea').empty(); // 목록 바뀌면 상세 비우기

			if (opts.pushState !== false) {
				pushUrl(params);
			}
		});
	}

	// Enter로 submit 방지 (검색은 버튼 클릭으로만)
	$form.on('submit', function (e) {
		e.preventDefault();
		loadList(1);
	});

	// 검색 버튼 클릭
	$('#btnSearch').on('click', function () {
		loadList(1);
	});

	// 초기화 버튼 (Ajax)
	$('#btnReset').on('click', function () {
		$form.find('select[name="category"]').val('ALL');
		$form.find('select[name="type"]').val('all');
		$form.find('input[name="keyword"]').val('');
		$form.find('select[name="sort"]').val('latest');
		$form.find('input[name="page"]').val('1');
		loadList(1);
	});

	// 정렬 변경 시 즉시 Ajax
	$form.on('change', 'select[name="sort"]', function () {
		loadList(1);
	});

	// 카테고리/타입 변경 시 즉시 Ajax
	$form.on('change', 'select[name="category"], select[name="type"]', function () {
		loadList(1);
	});

	/**
	 * 페이징 클릭 (href 기반으로 동작)
	 * - paging fragment 가 교체되어도 살아있도록 #adminListArea 에 이벤트 위임
	 */
	$('#adminListArea').on('click', '.pl-paging a.pl-page', function (e) {
		const href = $(this).attr('href');
		const dataPage = $(this).attr('data-page');

		// 링크 없으면 기존 로직(data-page)로
		const page = (function () {
			if (href) {
				const u = new URL(href, location.origin);
				return u.searchParams.get('page') || '1';
			}
			return dataPage || '1';
		})();

		e.preventDefault();
		$form.find('input[name="page"]').val(page);
		loadList(page);
	});

	// 뒤로가기/앞으로가기 URL 파라미터 기준으로 목록을 다시 로드
	window.addEventListener('popstate', function () {
		const q = parseQuery();
		syncFormFromQuery(q);
		loadList(q.page || 1, { pushState: false });
	});

	// 최초 진입 시 URL 쿼리가 있으면 폼과 hidden page 를 맞춰둔다.
	// (서버가 이미 목록을 렌더링해준 상태라도, 이후 ajax 동작 일관성을 위해)
	(function initFromUrl() {
		const q = parseQuery();
		if (q.category || q.type || q.keyword || q.sort || q.page) {
			syncFormFromQuery(q);
		}
	})();

	// 게시글 클릭 -> 상세 fragment 로드
	$(document).on('click', '.ad-row', function () {
		const postSeq = $(this).data('post-seq');
		if (!postSeq) return;

		$('.ad-row').removeClass('is-active');
		$(this).addClass('is-active');

		$.get('/admin/dashboard/post-fragment', { postSeq }, function (html) {
			$('#adminPostDetailArea').html(html);
		});
	});
	
	// ===== (관리자) 댓글 삭제 =====
	$(document).on('click', '[data-admin-comment-delete]', function () {
		const commentSeq = $(this).attr('data-comment-seq');
		if (!commentSeq) return;
		
		Modal.open(
			'#confirmModal',
			'정말 이 댓글을 삭제할까요?<br>삭제하시면 복구할 수 없습니다.',
			{
				primaryText: '삭제',
				secondaryText: '취소',
				onPrimary: function () {
					$.ajax({
						type: 'POST',
						url: '/admin/dashboard/comment-delete',
						data: { commentSeq },
						success: function () {
							// 상세 패널에 있는 postSeq
							const postSeq = $('[data-admin-post-delete]').attr('data-admin-post-delete');
							
							// 목록의 해당 게시글 댓글 카운트 -1
							const $row = $(`.ad-row[data-post-seq="${postSeq}"]`);
							const $cnt = $row.find('.ad-cmt span').first();
							if ($cnt.length) {
								const n = parseInt($cnt.text(), 10) || 0;
								$cnt.text(Math.max(0, n - 1));
							}
							
							// 상세 다시 로드
							$.get('/admin/dashboard/post-fragment', { postSeq }, function (html) {
								$('#adminPostDetailArea').html(html);
							});
							
						},
						error: function () {
							Modal.open('#alertModal', '댓글 삭제에 실패했습니다.');
						}
					});
				}
			}
		);
	});

	// ===== (관리자) 게시글 삭제 =====
	$(document).on('click', '[data-admin-post-delete]', function () {
		const postSeq = $(this).attr('data-admin-post-delete');
		if (!postSeq) return;

		Modal.open('#confirmModal', '정말 이 게시글을 삭제할까요?<br>삭제하시면 복구할 수 없습니다.',{
				primaryText: '삭제',
				secondaryText: '취소',
				onPrimary: function () {
					$.ajax({
						type: 'POST',
						url: '/admin/dashboard/post-delete',
						data: { postSeq },
						success: function () {
							// 목록에서 해당 행 제거 + 상세 패널 비우기
							$(`.ad-row[data-post-seq="${postSeq}"]`).remove();
							$('#adminPostDetailArea').empty();
						},
						error: function () {
							Modal.open('#alertModal', '게시글 삭제에 실패했습니다.');
						}
					});
				}
			}
		);
	});

});