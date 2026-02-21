/* Public Survey view - прохождение опроса */
async function renderPublicSurvey(publicId) {
  const errEl = el("div", { class: "error" });
  const okEl = el("div", { class: "ok" });
  let survey = null;
  try { survey = await API.public.getSurvey(publicId); } catch (e) { errEl.textContent = e.message; }

  if (!survey) {
    app.append(el("div", { class: "card" }, [ el("h2", { text: "Опрос" }), errEl ]));
    return;
  }

  const respondentId = el("input", { placeholder: "например: email или имя" });

  const answersUI = [];
  for (const q of survey.questions) {
    let widget = null;
    if (q.type === "TEXT") {
      const t = el("textarea", { 
        placeholder: "Введите ответ...",
        style: "width: 100%; min-height: 120px;"
      });
      widget = el("div", { class: "field", style: "width: 100%;" }, [ t ]);
      answersUI.push({ q, get: () => ({ questionId: q.id, textValue: t.value, optionIds: [] }) });
    } else if (q.type === "SINGLE_CHOICE") {
      // Используем радиокнопки вместо выпадающего списка
      const wrap = el("div", { class: "radio-group" });
      const radios = [];
      for (const o of q.options) {
        const radio = el("input", { 
          type: "radio", 
          name: `question_${q.id}`,
          value: String(o.id),
          id: `option_${o.id}`
        });
        const label = el("label", { 
          for: `option_${o.id}`,
          class: "radio-label",
          text: o.text
        });
        radios.push({ id: o.id, radio });
        wrap.append(el("div", { class: "radio-option" }, [ radio, label ]));
      }
      widget = wrap;
      answersUI.push({ q, get: () => {
        const selected = radios.find(r => r.radio.checked);
        return { questionId: q.id, textValue: null, optionIds: selected ? [ selected.id ] : [] };
      }});
    } else {
      // MULTIPLE_CHOICE
      const wrap = el("div", { class: "checkbox-group" });
      const checks = [];
      for (const o of q.options) {
        const cb = el("input", { 
          type: "checkbox", 
          id: `option_${o.id}`,
          value: String(o.id)
        });
        const label = el("label", { 
          for: `option_${o.id}`,
          class: "checkbox-label",
          text: o.text
        });
        checks.push({ id: o.id, cb });
        wrap.append(el("div", { class: "checkbox-option" }, [ cb, label ]));
      }
      widget = wrap;
      answersUI.push({ q, get: () => ({ questionId: q.id, textValue: null, optionIds: checks.filter(x => x.cb.checked).map(x => x.id) }) });
    }
    app.append(el("div", { class: "card question-card-public" }, [
      el("div", { class: "question-header", text: `Вопрос ${q.position}` }),
      el("h3", { class: "question-text", text: q.text }),
      widget
    ]));
  }

  const submitBtn = el("button", { class: "btn btn-primary", text: "Отправить ответы", onclick: async () => {
    errEl.textContent = ""; okEl.textContent = "";
    try {
      const token = getRespondentToken();
      const payload = {
        respondentIdentifier: survey.anonymous ? null : respondentId.value,
        answers: answersUI.map(x => x.get()),
      };
      const resp = await API.public.submit(publicId, token, payload);
      okEl.textContent = "Готово! Ответы отправлены.";
    } catch (e) { errEl.textContent = e.message; }
  }});

  const head = el("div", { class: "card" }, [
    el("h2", { text: survey.title }),
    el("div", { class: "muted", text: survey.description || "" }),
    errEl,
    okEl,
    !survey.anonymous ? el("div", { class: "field" }, [ el("label", { text: "Идентификатор респондента (для неанонимного опроса)" }), respondentId ]) : el("div", { class: "muted", text: "Опрос анонимный" }),
    submitBtn
  ]);

  app.prepend(head);
}
