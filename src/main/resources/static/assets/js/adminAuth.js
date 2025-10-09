// Simpel demo-auth: gemmer et "token" i localStorage og tjekker det på admin-sider.
// Brugernavn/kode er hardcodet for demo.
const DEMO_USER = { username: "admin", password: "kinotest123" };
const LS_KEY = "kino_admin_token";

export function login(username, password) {
    if (username === DEMO_USER.username && password === DEMO_USER.password) {
        const token = `demo.${Math.random().toString(36).slice(2)}`;
        localStorage.setItem(LS_KEY, token);
        return true;
    }
    return false;
}

export function logout() {
    localStorage.removeItem(LS_KEY);
}

export function isAuthed() {
    return !!localStorage.getItem(LS_KEY);
}

// Guard, kald på sider der skal være beskyttede:
export function requireAuth(redirectTo = "/admin-login.html") {
    if (!isAuthed()) {
        window.location.href = redirectTo + "?next=" + encodeURIComponent(location.pathname);
    }
}
