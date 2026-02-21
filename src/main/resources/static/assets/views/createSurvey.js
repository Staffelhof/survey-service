/* Create/Edit Survey view */
async function renderCreateSurvey() {
  const errEl = el("div", { class: "error" });
  
  // Основные поля опроса
  const title = el("input", { type: "text", placeholder: "Введите название опроса" });
  const desc = el("textarea", { placeholder: "Описание опроса (необязательно)", rows: 3 });
  
  const anonymous = el("select", {}, [
    el("option", { value: "true", text: "Анонимный" }),
    el("option", { value: "false", text: "С идентификацией" }),
  ]);
  anonymous.value = "true";

  const single = el("select", {}, [
    el("option", { value: "true", text: "Одно прохождение" }),
    el("option", { value: "false", text: "Множественные прохождения" }),
  ]);
  single.value = "true";

  // Контейнер для вопросов
  const questionsContainer = el("div", { class: "questions-container" });
  let questionCounter = 0;

  // Функция для создания элемента вопроса
  function createQuestionElement(questionNum) {
    const questionCard = el("div", { class: "card question-card" });
    
    const qText = el("input", { type: "text", placeholder: "Введите текст вопроса" });
    const qType = el("select", {}, [
      el("option", { value: "SINGLE_CHOICE", text: "Один вариант ответа" }),
      el("option", { value: "MULTIPLE_CHOICE", text: "Несколько вариантов" }),
      el("option", { value: "TEXT", text: "Текстовый ответ" }),
    ]);
    qType.value = "SINGLE_CHOICE";

    const optionsContainer = el("div", { class: "options-container" });
    const optionsList = el("div", { class: "options-list" });
    
    // Создаем два начальных варианта ответа
    function createOptionInput(index, placeholder) {
      const optDiv = el("div", { class: "option-row" });
      const optInput = el("input", { type: "text", placeholder, class: "option-input" });
      const removeOptBtn = el("button", { 
        type: "button",
        class: "btn btn-small btn-danger", 
        text: "×",
        onclick: () => {
          if (optionsList.children.length > 2) {
            optDiv.remove();
          }
        }
      });
      optDiv.append(optInput, removeOptBtn);
      return { div: optDiv, input: optInput };
    }

    const opt1 = createOptionInput(1, "Вариант ответа 1");
    const opt2 = createOptionInput(2, "Вариант ответа 2");
    optionsList.append(opt1.div, opt2.div);

    const addOptionBtn = el("button", { 
      type: "button",
      class: "btn btn-small", 
      text: "+ Добавить вариант",
      onclick: () => {
        const newOpt = createOptionInput(optionsList.children.length + 1, `Вариант ответа ${optionsList.children.length + 1}`);
        optionsList.append(newOpt.div);
      }
    });

    // Функция для обновления видимости вариантов ответов
    const updateOptionsVisibility = () => {
      if (qType.value === "TEXT") {
        optionsContainer.style.display = "none";
      } else {
        optionsContainer.style.display = "block";
      }
    };
    
    qType.addEventListener("change", updateOptionsVisibility);
    updateOptionsVisibility();

    const removeQuestionBtn = el("button", { 
      type: "button",
      class: "btn btn-small btn-danger", 
      text: "Удалить вопрос",
      onclick: () => {
        if (questionsContainer.children.length > 1) {
          questionCard.remove();
        } else {
          errEl.textContent = "Должен быть хотя бы один вопрос";
        }
      }
    });

    optionsContainer.append(
      el("div", { class: "field" }, [ 
        el("label", { text: "Варианты ответов" }), 
        optionsList,
        el("div", { style: "margin-top: 8px" }, [ addOptionBtn ])
      ])
    );

    questionCard.append(
      el("div", { class: "row", style: "align-items: center; margin-bottom: 12px" }, [
        el("h3", { style: "margin: 0; flex: 1", text: `Вопрос ${questionNum}` }),
        removeQuestionBtn
      ]),
      el("div", { class: "row" }, [
        el("div", { class: "col" }, [
          el("div", { class: "field" }, [ 
            el("label", { text: "Текст вопроса *" }), 
            qText 
          ]),
          el("div", { class: "field" }, [ 
            el("label", { text: "Тип вопроса" }), 
            qType 
          ]),
        ]),
        el("div", { class: "col" }, [
          optionsContainer
        ]),
      ])
    );

    return {
      card: questionCard,
      getData: () => {
        const options = [];
        if (qType.value !== "TEXT") {
          optionsList.querySelectorAll(".option-input").forEach((input, idx) => {
            if (input.value.trim()) {
              options.push({ position: idx + 1, text: input.value.trim() });
            }
          });
        }
        return {
          position: questionNum,
          type: qType.value,
          text: qText.value.trim(),
          required: true,
          options
        };
      }
    };
  }

  // Добавляем первый вопрос
  const firstQuestion = createQuestionElement(1);
  questionsContainer.append(firstQuestion.card);

  // Кнопка добавления вопроса
  const addQuestionBtn = el("button", { 
    type: "button",
    class: "btn", 
    text: "+ Добавить вопрос",
    onclick: () => {
      questionCounter++;
      const newQuestion = createQuestionElement(questionsContainer.children.length + 1);
      questionsContainer.append(newQuestion.card);
    }
  });

  const saveBtn = el("button", { class: "btn btn-primary", text: "Создать опрос", onclick: async () => {
    errEl.textContent = "";
    
    // Валидация
    if (!title.value.trim()) {
      errEl.textContent = "Введите название опроса";
      return;
    }

    const questions = [];
    const questionCards = questionsContainer.querySelectorAll(".question-card");
    
    if (questionCards.length === 0) {
      errEl.textContent = "Добавьте хотя бы один вопрос";
      return;
    }

    questionCards.forEach((card, idx) => {
      const qText = card.querySelector("input[type='text']");
      const qType = card.querySelector("select");
      const options = card.querySelectorAll(".option-input");
      
      if (!qText.value.trim()) {
        errEl.textContent = `Заполните текст вопроса ${idx + 1}`;
        return;
      }

      if (qType.value !== "TEXT") {
        const filledOptions = Array.from(options).filter(opt => opt.value.trim());
        if (filledOptions.length < 2) {
          errEl.textContent = `Вопрос ${idx + 1}: добавьте минимум 2 варианта ответа`;
          return;
        }
      }

      const questionData = {
        position: idx + 1,
        type: qType.value,
        text: qText.value.trim(),
        required: true,
        options: qType.value === "TEXT" ? [] : Array.from(options)
          .filter(opt => opt.value.trim())
          .map((opt, optIdx) => ({ position: optIdx + 1, text: opt.value.trim() }))
      };
      questions.push(questionData);
    });

    if (errEl.textContent) {
      return;
    }
    
    try {
      const payload = {
        title: title.value.trim(),
        description: desc.value.trim(),
        anonymous: anonymous.value === "true",
        singleSubmission: single.value === "true",
        startsAt: null,
        endsAt: null,
        questions
      };
      await API.surveys.create(payload);
      setHash("/"); 
      await safeRender();
    } catch (e) { 
      errEl.textContent = e.message; 
    }
  }});

  app.append(
    el("div", { class: "card" }, [
      el("div", { class: "row" }, [
        el("h2", { text: "Создать опрос" }),
        el("div", { style: "flex:1" }),
        el("button", { class: "btn", text: "Назад", onclick: () => setHash("/") })
      ]),
      errEl,
      el("div", { class: "divider" }),
      el("div", { class: "row" }, [
        el("div", { class: "col" }, [
          el("div", { class: "field" }, [ 
            el("label", { text: "Название опроса *" }), 
            title 
          ]),
          el("div", { class: "field" }, [ 
            el("label", { text: "Описание" }), 
            desc 
          ]),
        ]),
        el("div", { class: "col" }, [
          el("div", { class: "field" }, [ 
            el("label", { text: "Режим анонимности" }), 
            anonymous 
          ]),
          el("div", { class: "field" }, [ 
            el("label", { text: "Ограничение прохождений" }), 
            single 
          ]),
        ]),
      ]),
      el("div", { class: "divider" }),
      el("div", { class: "row", style: "align-items: center; margin-bottom: 12px" }, [
        el("h3", { style: "margin: 0; flex: 1", text: "Вопросы" }),
        addQuestionBtn
      ]),
      questionsContainer,
      el("div", { class: "divider" }),
      el("div", { class: "row" }, [
        el("div", { style: "flex:1" }),
        saveBtn
      ]),
    ])
  );
}
