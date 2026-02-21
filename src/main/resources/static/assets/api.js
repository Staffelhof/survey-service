/* API client */
const API = {
  async req(method, path, body, extraHeaders = {}) {
    const res = await fetch(path, {
      method,
      headers: {
        "Content-Type": "application/json",
        ...extraHeaders,
      },
      credentials: "include", // session cookie
      body: body ? JSON.stringify(body) : undefined,
    });
    let data = null;
    const ct = res.headers.get("content-type") || "";
    if (ct.includes("application/json")) data = await res.json();
    else data = await res.text().catch(() => null);
    if (!res.ok) {
      let msg = (data && data.error) ? data.error : "";
      if (!msg) {
        // Переводим HTTP статусы на русский
        const statusMessages = {
          400: "Неверный запрос",
          401: "Требуется авторизация",
          403: "Доступ запрещен",
          404: "Не найдено",
          500: "Ошибка сервера"
        };
        msg = statusMessages[res.status] || `Ошибка ${res.status}`;
      }
      throw new Error(msg);
    }
    return data;
  },
  auth: {
    register: (email, password, displayName) => API.req("POST", "/api/auth/register", { email, password, displayName }),
    login: (email, password) => API.req("POST", "/api/auth/login", { email, password }),
    me: () => API.req("GET", "/api/auth/me"),
    logout: () => API.req("POST", "/api/auth/logout"),
  },
  surveys: {
    list: () => API.req("GET", "/api/surveys"),
    create: (payload) => API.req("POST", "/api/surveys", payload),
    get: (publicId) => API.req("GET", `/api/surveys/${publicId}`),
    update: (publicId, payload) => API.req("PUT", `/api/surveys/${publicId}`, payload),
    publish: (publicId) => API.req("POST", `/api/surveys/${publicId}/publish`),
    setActive: (publicId, value) => API.req("POST", `/api/surveys/${publicId}/active?value=${value}`),
    del: (publicId) => API.req("DELETE", `/api/surveys/${publicId}`),
  },
  public: {
    getSurvey: (publicId) => API.req("GET", `/api/public/surveys/${publicId}`),
    submit: (publicId, respondentToken, payload) =>
      API.req("POST", `/api/public/surveys/${publicId}/submit`, payload, { "X-Respondent-Token": respondentToken }),
  },
  results: {
    get: (publicId) => API.req("GET", `/api/results/${publicId}`),
    listSubmissions: (publicId) => API.req("GET", `/api/results/${publicId}/submissions`),
    getSubmission: (publicId, submissionPublicId) => API.req("GET", `/api/results/${publicId}/submissions/${submissionPublicId}`),
  },
  admin: {
    listUsers: () => API.req("GET", "/api/admin/users"),
    updateUserRoles: (userId, roles) => API.req("PUT", `/api/admin/users/${userId}/roles`, { roles }),
    updateUserEnabled: (userId, enabled) => API.req("PUT", `/api/admin/users/${userId}/enabled`, { enabled }),
    deleteUser: (userId) => API.req("DELETE", `/api/admin/users/${userId}`),
  }
};
