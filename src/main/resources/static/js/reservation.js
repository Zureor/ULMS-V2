// Reservation page JavaScript
document.addEventListener('DOMContentLoaded', function () {
    // Animate status badges on load
    const badges = document.querySelectorAll('.badge');
    badges.forEach(badge => {
        badge.style.opacity = '0';
        badge.style.transform = 'translateX(-10px)';
        setTimeout(() => {
            badge.style.transition = 'all 0.3s ease';
            badge.style.opacity = '1';
            badge.style.transform = 'translateX(0)';
        }, 100);
    });
});