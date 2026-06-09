// 상태 토글 버튼 공통 처리.
// 버튼에 data-type(users/reviews/comments)과 data-id 가 있어야 한다.
// PATCH /api/{type}/{id}/status 호출 후, 같은 행의 상태 배지를 갱신한다.
document.addEventListener('click', async (event) => {
    const button = event.target.closest('.js-toggle-status');
    if (!button) return;

    const type = button.dataset.type;
    const id = button.dataset.id;
    if (!type || !id) return;

    button.disabled = true;
    try {
        const res = await fetch(`/api/${type}/${id}/status`, {
            method: 'PATCH',
            headers: { 'Accept': 'application/json' },
        });
        if (!res.ok) {
            throw new Error(`요청 실패 (${res.status})`);
        }
        const data = await res.json();
        updateStatusBadge(button, data.status);
    } catch (err) {
        alert(`상태 변경 실패: ${err.message}`);
    } finally {
        button.disabled = false;
    }
});

// 같은 행(tr)에서 상태 배지를 찾아 새 상태로 갱신한다.
function updateStatusBadge(button, status) {
    const row = button.closest('tr');
    if (!row) return;
    const badge = row.querySelector('[data-status-badge], .status-badge') || findBadge(row);
    if (!badge) return;

    badge.textContent = status;
    const active = status === 'ACTIVE';
    badge.classList.toggle('bg-green-100', active);
    badge.classList.toggle('text-green-700', active);
    badge.classList.toggle('bg-red-100', !active);
    badge.classList.toggle('text-red-700', !active);
}

// 상태 배지는 green/red 계열 배경 클래스를 가진 span 으로 식별한다.
function findBadge(row) {
    return row.querySelector('span.bg-green-100, span.bg-red-100');
}
