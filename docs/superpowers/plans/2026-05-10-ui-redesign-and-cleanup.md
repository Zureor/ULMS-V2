# ULMS UI Redesign & Registration Removal Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the ULMS UI to a modern "Glassmorphism Lite" aesthetic and remove all public self-registration features.

**Architecture:** Centralized CSS for consistent themes, refactored Thymeleaf fragments for unified layout, and simplified security/auth logic by removing registration.

**Tech Stack:** Java 17, Spring Boot 3.4.x, Spring Security, Thymeleaf, Vanilla CSS, FontAwesome, Bootstrap 5.

---

### Task 1: Remove Registration Logic & UI

**Files:**
- Delete: `src/main/resources/templates/auth/register.html`
- Delete: `src/main/java/com/ulms/dto/request/RegisterRequest.java` (if only used for registration)
- Modify: `src/main/java/com/ulms/controller/AuthController.java`
- Modify: `src/main/java/com/ulms/service/AuthService.java`
- Modify: `src/main/java/com/ulms/service/impl/AuthServiceImpl.java`
- Modify: `src/main/java/com/ulms/config/SecurityConfig.java`
- Modify: `src/main/resources/templates/index.html`
- Modify: `src/main/resources/templates/auth/login.html`

- [ ] **Step 1: Remove registration methods from AuthService and AuthServiceImpl**

```java
// AuthService.java: Remove User register(RegisterRequest request);
// AuthServiceImpl.java: Remove the register method implementation.
```

- [ ] **Step 2: Remove registration endpoints from AuthController**

```java
// AuthController.java: Remove /register (GET) and /register (POST) and /api/auth/register (POST)
```

- [ ] **Step 3: Update SecurityConfig to remove public registration access**

```java
// SecurityConfig.java: Remove "/register" from permitAll() list.
```

- [ ] **Step 4: Update index.html and login.html to remove register links**

```html
<!-- index.html: Remove the Register button -->
<!-- login.html: Remove the "Create Account" button and add "Admin-managed accounts only" text -->
```

- [ ] **Step 5: Delete redundant files**

```bash
rm src/main/resources/templates/auth/register.html
```

- [ ] **Step 6: Verify application still builds and runs**

Run: `mvn clean compile`
Expected: BUILD SUCCESS

---

### Task 2: Implement Glassmorphism CSS

**Files:**
- Modify: `src/main/resources/static/css/main.css`
- Delete: `src/main/resources/static/css/dashboard.css`, `forms.css`, `navbar.css`, `tables.css`

- [ ] **Step 1: Replace main.css with Glassmorphism Lite styles**

```css
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

:root {
    --glass-bg: rgba(255, 255, 255, 0.7);
    --glass-border: rgba(255, 255, 255, 0.3);
    --accent: #6366f1;
    --accent-hover: #4f46e5;
    --text-primary: #1f2937;
    --text-secondary: #6b7280;
    --sidebar-width: 260px;
}

body {
    font-family: 'Inter', sans-serif;
    background: linear-gradient(135deg, #e0e7ff 0%, #fef2f2 100%);
    background-attachment: fixed;
    min-height: 100vh;
    color: var(--text-primary);
}

.glass-card {
    background: var(--glass-bg);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border: 1px solid var(--glass-border);
    border-radius: 24px;
    box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.07);
}

.sidebar {
    width: var(--sidebar-width);
    height: 100vh;
    position: fixed;
    top: 0;
    left: 0;
    background: rgba(255, 255, 255, 0.4);
    backdrop-filter: blur(10px);
    border-right: 1px solid var(--glass-border);
    padding: 2rem 1.5rem;
}

.main-content {
    margin-left: var(--sidebar-width);
    padding: 2rem;
}

.btn-primary {
    background: var(--accent) !important;
    border: none !important;
    border-radius: 12px !important;
    padding: 10px 24px !important;
    transition: 0.3s !important;
}

.btn-primary:hover {
    background: var(--accent-hover) !important;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.form-control {
    background: rgba(255, 255, 255, 0.5) !important;
    border: 1px solid rgba(0, 0, 0, 0.1) !important;
    border-radius: 12px !important;
    padding: 12px 16px !important;
}

.form-control:focus {
    background: #fff !important;
    border-color: var(--accent) !important;
    box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1) !important;
}

.table {
    border-collapse: separate;
    border-spacing: 0 8px;
}

.table tr {
    background: rgba(255, 255, 255, 0.4);
    border-radius: 12px;
}

.table td, .table th {
    padding: 16px !important;
    border: none !important;
}

.table tr:hover {
    background: rgba(255, 255, 255, 0.6) !important;
}
```

- [ ] **Step 2: Delete redundant CSS files and update templates to only use main.css**

```bash
rm src/main/resources/static/css/dashboard.css src/main/resources/static/css/forms.css src/main/resources/static/css/navbar.css src/main/resources/static/css/tables.css
```

---

### Task 3: Redesign Layout & Fragments

**Files:**
- Modify: `src/main/resources/templates/fragments/header.html`
- Modify: `src/main/resources/templates/admin/dashboard.html`
- Modify: `src/main/resources/templates/librarian/dashboard.html`
- Modify: `src/main/resources/templates/student/dashboard.html`

- [ ] **Step 1: Update header.html fragment to include unified sidebar and navbar-less layout**

```html
<!-- Unified Layout Fragment -->
<div th:fragment="layout(content)">
    <div class="sidebar">
        <div class="logo mb-5 text-center">
            <i class="fas fa-book-open fa-2x text-primary"></i>
            <h4 class="mt-2 fw-bold">ULMS</h4>
        </div>
        <ul class="nav flex-column">
            <!-- Dynamic links based on Role -->
            <li class="nav-item mb-2" sec:authorize="hasRole('ADMIN')">
                <a class="nav-link" th:href="@{/dashboard/admin}"><i class="fas fa-chart-pie me-2"></i> Dashboard</a>
            </li>
            <!-- ... other links ... -->
            <li class="nav-item mt-auto">
                <a class="nav-link text-danger" th:href="@{/logout}"><i class="fas fa-sign-out-alt me-2"></i> Logout</a>
            </li>
        </ul>
    </div>
    <div class="main-content">
        <div class="glass-card p-5">
            <div th:replace="${content}"></div>
        </div>
    </div>
</div>
```

- [ ] **Step 2: Update all Dashboard pages to use the new layout fragment**

```html
<!-- Example for admin/dashboard.html -->
<div th:replace="~{fragments/header :: layout(~{::#admin-content})}">
    <div id="admin-content">
        <h2>Admin Overview</h2>
        <!-- Statistics cards etc -->
    </div>
</div>
```

---

### Task 4: Final Polishing & Verification

**Files:**
- Modify: `src/main/resources/templates/auth/login.html`
- Modify: `src/main/resources/templates/index.html`

- [ ] **Step 1: Redesign Login Page for maximum minimalism**

```html
<!-- login.html: Centered card, no sidebar, background gradient -->
```

- [ ] **Step 2: Run and manually verify all routes**

- Login as Admin: Check dashboard and "Manage Users".
- Login as Student: Check book browse and reservations.
- Verify "Register" is gone from all pages.

- [ ] **Step 3: Commit all changes**

```bash
git add .
git commit -m "feat: complete UI redesign to Glassmorphism Lite and remove registration"
```
