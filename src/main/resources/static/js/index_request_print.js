const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('orderSuccess') === 'true') {
    alert('📦 인쇄 주문이 성공적으로 접수되었습니다!');
    window.history.replaceState({}, document.title, window.location.pathname);
}

document.addEventListener("DOMContentLoaded", function() {
    const selectAll = document.getElementById('selectAll');
    const orderForm = document.getElementById('orderForm');

    if (selectAll) {
        selectAll.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('.print-checkbox');
            checkboxes.forEach(cb => {
                cb.checked = selectAll.checked;
            });
            updateCount();
        });
    }

    if (orderForm) {
        orderForm.addEventListener('submit', function(e) {
            const checkedCount = document.querySelectorAll('.print-checkbox:checked').length;
            if (checkedCount === 0) {
                e.preventDefault();
                alert("인쇄할 다이어리를 최소 하나 이상 선택해주세요!");
            }
        });
    }
});

function toggleCheck(id) {
    const cb = document.getElementById('check-' + id);
    if (cb) {
        cb.checked = !cb.checked;
        updateCount();
    }
}

function updateCount() {
    const checkboxes = document.querySelectorAll('.print-checkbox');
    const checkedCount = document.querySelectorAll('.print-checkbox:checked').length;
    const selectAll = document.getElementById('selectAll');

    document.getElementById('selectedCount').innerText = checkedCount + "개 선택됨";

    if (selectAll) {
        selectAll.checked = (checkboxes.length > 0 && checkedCount === checkboxes.length);
    }
}
