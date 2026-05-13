// ULMS JavaScript

document.addEventListener('DOMContentLoaded', function () {
    // Flash message auto-dismiss
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Confirm delete actions
    const deleteForms = document.querySelectorAll('form[onsubmit*="confirm"]');
    deleteForms.forEach(form => {
        form.addEventListener('submit', function (e) {
            if (!confirm('Are you sure you want to proceed?')) {
                e.preventDefault();
            }
        });
    });

    // Search input focus
    const searchInput = document.querySelector('input[name="query"]');
    if (searchInput && document.activeElement !== searchInput) {
        // Don't auto-focus on login page
    }

    // Sidebar Toggle Logic
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    const sidebar = document.getElementById('sidebar');

    if (sidebarToggle && sidebar && sidebarOverlay) {
        sidebarToggle.addEventListener('click', function () {
            sidebar.classList.toggle('show');
            sidebarOverlay.classList.toggle('show');
        });

        sidebarOverlay.addEventListener('click', function () {
            sidebar.classList.remove('show');
            sidebarOverlay.classList.remove('show');
        });
    }
});

// Reservation confirmation toast
function showReservationToast(bookTitle) {
    const toastContainer = document.getElementById('reservation-toast');
    if (toastContainer) {
        const toastEl = toastContainer.querySelector('.toast');
        const toastBody = toastEl.querySelector('.toast-body');
        toastBody.textContent = 'Successfully reserved: ' + bookTitle;
        const toast = new bootstrap.Toast(toastEl);
        toast.show();
    }
}

// Copy ISBN to clipboard
function copyISBN(isbn) {
    navigator.clipboard.writeText(isbn).then(() => {
        const btn = event.target;
        btn.textContent = 'Copied!';
        setTimeout(() => { btn.textContent = 'Copy'; }, 2000);
    });
}