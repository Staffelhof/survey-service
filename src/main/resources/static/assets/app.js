/* Main application file */
const app = document.getElementById("app");
const btnLogout = document.getElementById("btnLogout");

let currentUser = null;

btnLogout.addEventListener("click", async () => {
  await API.auth.logout().catch(() => {});
  btnLogout.classList.add("hidden");
  currentUser = null;
  setHash("/login");
  await safeRender();
});

async function render() {
  app.innerHTML = "";
  const r = route();
  const publicId = r.params.get("id");

  // Public route: /public?id=UUID
  if (r.path === "/public" && publicId) {
    return renderPublicSurvey(publicId);
  }

  // Try auth for private routes
  let me = null;
  try { 
    me = await API.auth.me();
    currentUser = me;
  } catch (_) {
    currentUser = null;
  }

  if (!me) {
    btnLogout.classList.add("hidden");
    if (r.path !== "/login") {
      setHash("/login");
      return;
    }
    return renderLogin();
  }

  btnLogout.classList.remove("hidden");

  // Admin routes
  if (isAdmin(me) && r.path === "/users") {
    return renderUsers();
  }

  // Creator routes
  if (r.path === "/create") return renderCreateSurvey();
  if (r.path === "/results" && publicId) return renderResults(publicId);
  return renderDashboard();
}

let isRendering = false;
async function safeRender() {
  if (isRendering) return;
  isRendering = true;
  try {
    await render();
  } finally {
    isRendering = false;
  }
}

window.addEventListener("hashchange", safeRender);
safeRender();
