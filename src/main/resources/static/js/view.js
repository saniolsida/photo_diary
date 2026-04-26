document.addEventListener("DOMContentLoaded", function() {
    const carouselEl = document.querySelector('#diaryCarousel');
    const items = carouselEl.querySelectorAll('.carousel-item');
    const dots = carouselEl.querySelectorAll('.dot-indicator');

    if (items.length > 0) {
        items[0].classList.add('active');
        dots[0].classList.add('active');
    }

    dots.forEach((dot, idx) => {
        dot.setAttribute('data-bs-slide-to', idx);
    });

    carouselEl.addEventListener('slid.bs.carousel', function (event) {
        const activeIndex = event.to; // 현재 활성화된 슬라이드 번호

        // 모든 점의 active 클래스 제거 후 현재 번호만 추가
        dots.forEach(dot => dot.classList.remove('active'));
        if(dots[activeIndex]) {
            dots[activeIndex].classList.add('active');
        }
    });

    if (items.length <= 1) {
        const controls = document.querySelector('.carousel-controls-bar');
        if(controls) controls.style.display = 'none';
    }
});