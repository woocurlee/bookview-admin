// 사이드바 접기/펼치기
(function () {
    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('sidebar-toggle');
    const iconCollapse = document.getElementById('icon-collapse');
    const iconExpand = document.getElementById('icon-expand');
    if (!sidebar || !toggleBtn) return;

    const STORAGE_KEY = 'sidebar-collapsed';
    const collapsed = localStorage.getItem(STORAGE_KEY) === 'true';
    if (collapsed) applyCollapsed(true, false);

    toggleBtn.addEventListener('click', () => {
        const isCollapsed = sidebar.classList.contains('w-14');
        applyCollapsed(!isCollapsed, true);
        localStorage.setItem(STORAGE_KEY, String(!isCollapsed));
    });

    function applyCollapsed(collapse, animate) {
        if (!animate) sidebar.classList.remove('transition-all', 'duration-200');
        sidebar.classList.toggle('w-56', !collapse);
        sidebar.classList.toggle('w-14', collapse);
        sidebar.querySelectorAll('.sidebar-label').forEach(el => el.classList.toggle('hidden', collapse));
        iconCollapse.classList.toggle('hidden', collapse);
        iconExpand.classList.toggle('hidden', !collapse);
        if (!animate) requestAnimationFrame(() => sidebar.classList.add('transition-all', 'duration-200'));
    }
})();

// ── 리뷰 상세 모달 ─────────────────────────────────────────────────────────

document.addEventListener('click', async (event) => {
    const button = event.target.closest('.js-review-detail');
    if (!button) return;

    const id = button.dataset.id;
    if (!id) return;

    button.disabled = true;
    try {
        const res = await fetch(`/api/reviews/${id}`, { headers: { 'Accept': 'application/json' } });
        if (!res.ok) throw new Error(`조회 실패 (${res.status})`);
        const data = await res.json();
        openReviewModal(data);
    } catch (err) {
        alert(`상세 조회 실패: ${err.message}`);
    } finally {
        button.disabled = false;
    }
});

function openReviewModal(data) {
    document.getElementById('review-detail-modal')?.remove();
    const r = data.review;

    const statusColor = r.status === 'ACTIVE' ? 'bg-green-100 text-green-700'
        : r.status === 'BLOCK' ? 'bg-amber-100 text-amber-700'
        : 'bg-red-100 text-red-700';

    const stars = '★'.repeat(r.rating ?? 0) + '☆'.repeat(5 - (r.rating ?? 0));

    const blockReasonHtml = data.blockReason
        ? `<div class="mt-3 p-3 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-800">
               <span class="font-medium">차단 사유:</span> ${escHtml(data.blockReason)}
           </div>`
        : '';

    const modal = document.createElement('div');
    modal.id = 'review-detail-modal';
    modal.className = 'fixed inset-0 z-50 flex items-center justify-center bg-black/40';
    modal.innerHTML = `
        <div class="bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[85vh] flex flex-col">
            <div class="flex items-center justify-between px-6 py-4 border-b">
                <div class="flex items-center gap-2">
                    <span class="font-semibold text-gray-800">#${r.reviewNo ?? '-'}</span>
                    <span class="inline-block px-2 py-0.5 rounded text-xs font-medium ${statusColor}">${r.status}</span>
                </div>
                <button class="modal-close text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
            </div>
            <div class="overflow-y-auto px-6 py-4 space-y-4 text-sm">
                <!-- 책 정보 -->
                <div class="flex gap-4">
                    ${r.bookThumbnail ? `<img src="${escHtml(r.bookThumbnail)}" class="w-16 h-20 object-cover rounded shadow-sm shrink-0" />` : ''}
                    <div class="space-y-1">
                        <p class="font-semibold text-base text-gray-800">${escHtml(r.bookTitle ?? '-')}</p>
                        <p class="text-gray-500">${escHtml(r.bookAuthor ?? '-')}</p>
                        <p class="text-amber-500 text-base tracking-widest">${stars}</p>
                    </div>
                </div>
                <!-- 리뷰 제목 -->
                <div>
                    <p class="text-xs text-gray-400 mb-0.5">제목</p>
                    <p class="text-gray-800 font-medium">${escHtml(r.title ?? '-')}</p>
                </div>
                <!-- 인용구 -->
                ${r.quote ? `<div class="border-l-4 border-gray-300 pl-3 text-gray-500 italic">${escHtml(r.quote)}</div>` : ''}
                <!-- 본문 -->
                <div>
                    <p class="text-xs text-gray-400 mb-0.5">본문</p>
                    <div class="text-gray-700 leading-relaxed whitespace-pre-wrap">${r.content ?? '-'}</div>
                </div>
                <!-- 메타 -->
                <div class="flex gap-6 text-xs text-gray-400 pt-2 border-t">
                    <span>작성자: <span class="text-gray-600">${escHtml(data.authorNickname ?? '-')}</span></span>
                    <span>좋아요: <span class="text-gray-600">${r.likeCount ?? 0}</span></span>
                    <span>작성일: <span class="text-gray-600">${fmtDate(r.createdAt)}</span></span>
                </div>
                ${blockReasonHtml}
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    modal.querySelector('.modal-close').addEventListener('click', () => modal.remove());
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.remove(); });
}

function escHtml(str) {
    return String(str ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function fmtDate(iso) {
    if (!iso) return '-';
    return iso.replace('T', ' ').substring(0, 16);
}

// ── 차단 모달 ──────────────────────────────────────────────────────────────
// 차단: 사유 입력 모달 → PATCH /api/{type}/{id}/status { reason }
// 차단 해제: 바로 PATCH (사유 불필요)

document.addEventListener('click', (event) => {
    const button = event.target.closest('.js-block-action');
    if (!button) return;

    const type = button.dataset.type;
    const id = button.dataset.id;
    const status = button.dataset.status;
    if (!type || !id) return;

    if (status === 'ACTIVE') {
        openBlockModal(button, type, id);
    } else {
        submitStatus(button, type, id, null);
    }
});

function openBlockModal(triggerButton, type, id) {
    // 기존 모달 제거
    document.getElementById('block-modal')?.remove();

    const modal = document.createElement('div');
    modal.id = 'block-modal';
    modal.className = 'fixed inset-0 z-50 flex items-center justify-center bg-black/40';
    modal.innerHTML = `
        <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h2 class="text-base font-semibold text-gray-800 mb-1">차단 사유 입력</h2>
            <p class="text-xs text-gray-400 mb-4">차단 후에도 사유는 기록으로 남습니다.</p>
            <textarea id="block-reason-input"
                class="w-full border rounded-lg px-3 py-2 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-gray-300"
                rows="3" placeholder="차단 사유를 입력하세요" maxlength="200" autocomplete="off"></textarea>
            <p class="text-xs text-gray-400 text-right mt-1"><span id="reason-count">0</span> / 200</p>
            <div class="flex justify-end gap-2 mt-4">
                <button id="block-cancel" class="text-sm px-4 py-2 rounded-lg border hover:bg-gray-50 transition-colors text-gray-600">취소</button>
                <button id="block-confirm" class="text-sm px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700 transition-colors">차단</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);

    const textarea = modal.querySelector('#block-reason-input');
    const counter = modal.querySelector('#reason-count');
    textarea.focus({ preventScroll: true });
    textarea.addEventListener('input', () => { counter.textContent = textarea.value.length; });

    modal.querySelector('#block-cancel').addEventListener('click', () => modal.remove());
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.remove(); });
    modal.querySelector('#block-confirm').addEventListener('click', () => {
        const reason = textarea.value.trim();
        if (!reason) {
            textarea.classList.add('border-red-400');
            textarea.focus({ preventScroll: true });
            return;
        }
        modal.remove();
        submitStatus(triggerButton, type, id, reason);
    });
}

async function submitStatus(button, type, id, reason) {
    button.disabled = true;
    try {
        const res = await fetch(`/api/${type}/${id}/status`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: reason !== null ? JSON.stringify({ reason }) : null,
        });
        if (!res.ok) throw new Error(`요청 실패 (${res.status})`);
        const data = await res.json();
        updateStatusBadge(button, data.status);
        button.dataset.status = data.status;
        button.textContent = data.status === 'ACTIVE' ? '차단' : '차단 해제';
    } catch (err) {
        alert(`상태 변경 실패: ${err.message}`);
    } finally {
        button.disabled = false;
    }
}

// 같은 행(tr)에서 상태 배지를 찾아 새 상태로 갱신한다.
function updateStatusBadge(button, status) {
    const row = button.closest('tr');
    if (!row) return;
    const badge = findBadge(row);
    if (!badge) return;

    badge.textContent = status;
    const isActive = status === 'ACTIVE';
    const isBlock = status === 'BLOCK';
    badge.classList.toggle('bg-green-100', isActive);
    badge.classList.toggle('text-green-700', isActive);
    badge.classList.toggle('bg-amber-100', isBlock);
    badge.classList.toggle('text-amber-700', isBlock);
    badge.classList.toggle('bg-red-100', !isActive && !isBlock);
    badge.classList.toggle('text-red-700', !isActive && !isBlock);
}

function findBadge(row) {
    return row.querySelector('span.bg-green-100, span.bg-red-100, span.bg-amber-100');
}
