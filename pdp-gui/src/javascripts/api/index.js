const apiPath = "/pdp/api";

function apiUrl(path) {
  return apiPath + path;
}

function validateResponse(res) {
  if (!res.ok) {
    const error = new Error(res.statusText);
    error.response = res;
    throw error;
  }

  return res;
}

export function parseJson(res) {
  return res.json();
}

function validFetch(path, options) {
  const headers = {
    "Accept": "application/json",
    "Content-Type": "application/json"
  };

  const fetchOptions = _.merge({}, { headers }, options);

  return fetch(apiUrl(path), fetchOptions)
  .then(validateResponse);
}

export function fetchJson(path, options = {}) {
  return validFetch(path, options)
  .then(parseJson);
}

export function getUserData() {
  return fetchJson("/internal/users/me");
}
