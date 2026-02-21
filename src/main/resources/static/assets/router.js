/* Router */
function route() {
  const hash = location.hash || "#/";
  const [path, q] = hash.slice(1).split("?");
  const params = new URLSearchParams(q || "");
  return { path, params };
}

function setHash(path) {
  location.hash = `#${path}`;
}
