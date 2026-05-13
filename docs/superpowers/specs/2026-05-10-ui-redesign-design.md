# Design Doc: ULMS UI Redesign & Simplification

## 1. Overview
The goal is to redesign the University Library Management System (ULMS) to follow a modern "Glassmorphism Lite" aesthetic. The interface will be minimal, professional, and consistent across all user roles. Additionally, the self-registration feature will be removed to ensure the system remains admin-managed only.

## 2. Visual Design (Glassmorphism Lite)
### 2.1 Theme
- **Background:** Soft linear gradient (e.g., Indigo to Rose/White).
- **Core Elements:** Semi-transparent white containers (`rgba(255, 255, 255, 0.7)`) with a strong backdrop blur (`12px`).
- **Borders:** Thin, subtle borders (`rgba(255, 255, 255, 0.3)`).
- **Corners:** High rounding across the board.
  - Main containers/cards: `24px`.
  - Buttons, inputs, and badges: `12px`.
- **Typography:** Modern sans-serif (Inter/Segoe UI) with increased line-height and letter-spacing for readability.

### 2.2 Component Updates
- **Tables:** Remove alternating row stripes. Use subtle hover effects and more padding.
- **Forms:** Translucent input fields that glow softly when focused. No heavy labels; use floating labels or high-contrast placeholders.
- **Buttons:** Solid accent colors (Deep Indigo) with soft shadows. No borders.

## 3. Structural Changes
### 3.1 Authentication & Access
- **No Self-Registration:**
  - Delete `register.html` and `RegisterRequest` usage in `AuthController`.
  - Remove "Register" links from `login.html` and `index.html`.
  - Remove registration logic from `AuthService` and `UserService`.
  - **Account Creation:** Admin dashboard will be the only way to add new students or librarians.
- **Login Page:** Centered minimal glass card. Logo, Username, Password, Sign In button. Text: "Admin-managed accounts only."

### 3.2 Navigation & Layout
- **Unified Sidebar:** A fixed glass sidebar for all roles (Admin, Librarian, Student).
- **Main Content:** A single large glass card container for all functional views.
- **Consistency:** Ensure `fragments/header.html` (or a new unified fragment) handles all navigation and sidebar logic consistently.

## 4. Technical Implementation
- **CSS:** Centralize all glassmorphism styles in `main.css`. Remove redundant CSS files.
- **Thymeleaf:** Refactor fragments to ensure all pages inherit the same layout and glass styles.
- **Security:** Ensure only ADMIN can access user creation endpoints.

## 5. Success Criteria
- The application feels cohesive and modern.
- All 500/casting errors are resolved by the refactor.
- No trace of public registration remains.
- All dashboards load correctly with the new layout.
