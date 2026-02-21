/* Users management view - только для администраторов */
async function renderUsers() {
  const errEl = el("div", { class: "error" });
  const listWrap = el("div");
  
  const load = async () => {
    errEl.textContent = "";
    listWrap.innerHTML = "";
    try {
      const users = await API.admin.listUsers();
      if (!users.length) {
        listWrap.append(el("div", { class: "muted", text: "Пользователей нет." }));
        return;
      }
      
      const table = el("table", { class: "table" });
      table.append(el("thead", {}, [ el("tr", {}, [
        el("th", { text: "Email" }),
        el("th", { text: "Имя" }),
        el("th", { text: "Роли" }),
        el("th", { text: "Статус" }),
        el("th", { text: "Действия" }),
      ]) ]));
      
      const tbody = el("tbody");
      for (const u of users) {
        const rolesText = u.roles && u.roles.length > 0 ? u.roles.join(", ") : "нет";
        const statusText = u.enabled ? "активен" : "заблокирован";
        const statusClass = u.enabled ? "pill" : "pill error";
        
        const rolesSelect = el("select", { class: "roles-select" });
        rolesSelect.append(el("option", { value: "CREATOR", text: "CREATOR" }));
        rolesSelect.append(el("option", { value: "ADMIN", text: "ADMIN" }));
        rolesSelect.append(el("option", { value: "CREATOR,ADMIN", text: "CREATOR + ADMIN" }));
        
        // Устанавливаем текущие роли
        const currentRoles = u.roles || [];
        if (currentRoles.includes("ADMIN") && currentRoles.includes("CREATOR")) {
          rolesSelect.value = "CREATOR,ADMIN";
        } else if (currentRoles.includes("ADMIN")) {
          rolesSelect.value = "ADMIN";
        } else {
          rolesSelect.value = "CREATOR";
        }
        
        const tr = el("tr", {}, [
          el("td", { text: u.email }),
          el("td", { text: u.displayName }),
          el("td", {}, [ rolesSelect ]),
          el("td", {}, [ el("span", { class: statusClass, text: statusText }) ]),
          el("td", {}, [
            el("div", { class: "row" }, [
              el("button", { 
                class: "btn btn-small btn-primary", 
                text: "Сохранить роли", 
                onclick: async () => {
                  try {
                    const selectedValue = rolesSelect.value;
                    const roles = selectedValue.split(",").map(r => r.trim());
                    await API.admin.updateUserRoles(u.id, roles);
                    await load();
                  } catch (e) {
                    errEl.textContent = e.message;
                  }
                }
              }),
              el("button", { 
                class: "btn btn-small", 
                text: u.enabled ? "Заблокировать" : "Разблокировать", 
                onclick: async () => {
                  try {
                    await API.admin.updateUserEnabled(u.id, !u.enabled);
                    await load();
                  } catch (e) {
                    errEl.textContent = e.message;
                  }
                }
              }),
              el("button", { 
                class: "btn btn-small btn-danger", 
                text: "Удалить", 
                onclick: async () => {
                  if (confirm(`Удалить пользователя ${u.email}?`)) {
                    try {
                      await API.admin.deleteUser(u.id);
                      await load();
                    } catch (e) {
                      errEl.textContent = e.message;
                    }
                  }
                }
              }),
            ])
          ]),
        ]);
        tbody.append(tr);
      }
      table.append(tbody);
      listWrap.append(table);
    } catch (e) { 
      errEl.textContent = e.message; 
    }
  };

  app.append(
    el("div", { class: "card" }, [
      el("div", { class: "row" }, [
        el("h2", { text: "Управление пользователями" }),
        el("div", { style: "flex:1" }),
        el("button", { class: "btn", text: "Назад", onclick: () => setHash("/") })
      ]),
      errEl,
      listWrap
    ])
  );
  
  await load();
}
