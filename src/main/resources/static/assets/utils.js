/* Utility functions */
function el(tag, attrs = {}, children = []) {
  const e = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === "class") e.className = v;
    else if (k === "text") e.textContent = v;
    else if (k.startsWith("on") && typeof v === "function") e.addEventListener(k.slice(2).toLowerCase(), v);
    else e.setAttribute(k, v);
  }
  for (const c of children) e.append(c);
  return e;
}

function getRespondentToken() {
  const key = "respondentToken";
  let t = localStorage.getItem(key);
  if (!t) {
    t = (crypto.randomUUID ? crypto.randomUUID() : (Math.random().toString(16).slice(2) + Date.now().toString(16))).replace(/-/g, "");
    t = t.slice(0, 64);
    localStorage.setItem(key, t);
  }
  return t;
}

function hasRole(user, roleName) {
  return user && user.roles && user.roles.includes(roleName);
}

function isAdmin(user) {
  return hasRole(user, "ADMIN");
}
