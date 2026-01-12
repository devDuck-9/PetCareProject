function confirmCancelWithModal(modalId, message) {
	Modal.open(modalId, message || '작성 중인 내용이 사라집니다.\n정말 취소할까요?', {
		primaryText: '취소하기',
		secondaryText: '계속 작성',
		onPrimary: () => history.back()
	});
}