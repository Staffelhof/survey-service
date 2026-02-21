/* Dashboard view - список опросов */
async function renderDashboard() {
  // Проверяем, есть ли у пользователя роль ADMIN
  let me = null;
  try { me = await API.auth.me(); } catch (_) {}
  
  const adminBtn = isAdmin(me) 
    ? el("button", { class: "btn btn-secondary", text: "Управление пользователями", onclick: () => setHash("/users") })
    : null;
  const errEl = el("div", { class: "error" });
  const listWrap = el("div");
  const load = async () => {
    errEl.textContent = "";
    listWrap.innerHTML = "";
    try {
      const surveys = await API.surveys.list();
      if (!surveys.length) {
        listWrap.append(el("div", { class: "muted", text: "Опросов пока нет. Создай первый." }));
        return;
      }
      const table = el("table", { class: "table" });
      table.append(el("thead", {}, [ el("tr", {}, [
        el("th", { text: "Название" }),
        el("th", { text: "Статус" }),
        el("th", { text: "Ссылка" }),
        el("th", { text: "Действия" }),
      ]) ]));
      const tbody = el("tbody");
      for (const s of surveys) {
        const linkHash = `#/public?id=${s.publicId}`;
        const status = s.published ? (s.active ? "опубликован" : "закрыт") : "черновик";
        const tr = el("tr", {}, [
          el("td", { text: s.title }),
          el("td", {}, [ el("span", { class: "pill", text: status }) ]),
          el("td", {}, [
            el("a", { href: linkHash, text: "открыть", target: "_self" }),
            el("div", { class: "muted", text: s.publicId })
          ]),
          el("td", {}, [
            el("div", { class: "row" }, [
              el("button", { class: "btn btn-small btn-primary", text: "Результаты", onclick: () => setHash(`/results?id=${s.publicId}`) }),
              !s.published ? el("button", { class: "btn btn-small", text: "Опубликовать", onclick: async () => { await API.surveys.publish(s.publicId); await load(); } }) : null,
              s.published ? el("button", { class: "btn btn-small", text: s.active ? "Закрыть" : "Открыть", onclick: async () => { await API.surveys.setActive(s.publicId, !s.active); await load(); } }) : null,
              el("button", { class: "btn btn-small btn-danger", text: "Удалить", onclick: async () => { await API.surveys.del(s.publicId); await load(); } }),
            ].filter(Boolean))
          ]),
        ]);
        tbody.append(tr);
      }
      table.append(tbody);
      listWrap.append(table);
    } catch (e) { errEl.textContent = e.message; }
  };

  app.append(
    el("div", { class: "card" }, [
      el("div", { class: "row" }, [
        el("h2", { text: "Мои опросы" }),
        el("div", { style: "flex:1" }),
        adminBtn,
        el("button", { class: "btn btn-primary", text: "Создать опрос", onclick: () => setHash("/create") })
      ].filter(Boolean)),
      errEl,
      listWrap
    ])
  );
  await load();
}
