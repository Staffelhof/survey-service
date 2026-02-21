/* Results view - просмотр результатов опроса */
async function renderResults(publicId) {
  const errEl = el("div", { class: "error" });
  let res = null;
  try { res = await API.results.get(publicId); } catch (e) { errEl.textContent = e.message; }
  if (!res) {
    app.append(el("div", { class: "card" }, [ el("h2", { text: "Результаты" }), errEl ]));
    return;
  }

  const card = el("div", { class: "card" }, [
    el("h2", { text: `Результаты: ${res.title}` }),
    el("div", { class: "muted", text: `Всего респондентов: ${res.submissionsCount}` }),
    el("div", { class: "row" }, [
      el("button", { class: "btn", text: "Назад", onclick: () => setHash("/") }),
      el("a", { class: "btn btn-primary", href: `/api/results/${publicId}/export.csv`, text: "Скачать CSV" })
    ])
  ]);

  app.append(card);

  for (const q of res.questions) {
    const qc = el("div", { class: "card" }, [
      el("div", { class: "muted", text: `Вопрос ${q.position} (${q.type})` }),
      el("h3", { text: q.text }),
    ]);

    if (q.type === "TEXT") {
      const list = el("div");
      const arr = q.textAnswers || [];
      if (!arr.length) list.append(el("div", { class: "muted", text: "Нет текстовых ответов" }));
      else {
        for (const t of arr.slice(0, 50)) list.append(el("div", { text: `- ${t}` }));
        if (arr.length > 50) list.append(el("div", { class: "muted", text: `... ещё ${arr.length - 50}` }));
      }
      qc.append(list);
    } else {
      const table = el("table", { class: "table" });
      table.append(el("thead", {}, [ el("tr", {}, [
        el("th", { text: "Вариант" }),
        el("th", { text: "Кол-во" }),
      ]) ]));
      const tb = el("tbody");
      for (const oc of q.optionCounts || []) {
        tb.append(el("tr", {}, [
          el("td", { text: oc.text }),
          el("td", { text: String(oc.count) }),
        ]));
      }
      table.append(tb);
      qc.append(table);
    }
    app.append(qc);
  }
}
