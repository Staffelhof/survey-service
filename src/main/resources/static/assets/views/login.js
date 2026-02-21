/* Login/Registration view */
async function renderLogin() {
  const state = { err: "" };
  const errEl = el("div", { class: "error" });

  const email = el("input", { type: "email", placeholder: "email@example.com" });
  const pass = el("input", { type: "password", placeholder: "пароль" });
  const name = el("input", { type: "text", placeholder: "имя (для регистрации)" });

  const loginBtn = el("button", { class: "btn btn-primary", text: "Войти", onclick: async () => {
    state.err = ""; errEl.textContent = "";
    try {
      await API.auth.login(email.value, pass.value);
      const me = await API.auth.me();
      if (me) {
        setHash("/"); 
        await safeRender();
      } else {
        errEl.textContent = "Ошибка авторизации";
      }
    } catch (e) { errEl.textContent = e.message; }
  }});

  const regBtn = el("button", { class: "btn", text: "Регистрация", onclick: async () => {
    state.err = ""; errEl.textContent = "";
    try {
      await API.auth.register(email.value, pass.value, name.value || "Пользователь");
      await API.auth.login(email.value, pass.value);
      const me = await API.auth.me();
      if (me) {
        setHash("/"); 
        await safeRender();
      } else {
        errEl.textContent = "Ошибка авторизации";
      }
    } catch (e) { errEl.textContent = e.message; }
  }});

  app.append(
    el("div", { class: "card" }, [
      el("h2", { text: "Вход / Регистрация" }),
      errEl,
      el("div", { class: "row" }, [
        el("div", { class: "col" }, [
          el("div", { class: "field" }, [ el("label", { text: "Email" }), email ]),
          el("div", { class: "field" }, [ el("label", { text: "Пароль" }), pass ]),
        ]),
        el("div", { class: "col" }, [
          el("div", { class: "field" }, [ el("label", { text: "Имя (для регистрации)" }), name ]),
          el("div", { class: "row" }, [ loginBtn, regBtn ]),
        ]),
      ]),
      el("div", { class: "divider" }),
      el("div", { class: "muted", text: "Публичные опросы открываются по ссылке: #/public?id=UUID" }),
    ])
  );
}
