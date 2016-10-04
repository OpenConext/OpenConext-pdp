import { currentIdentity } from "../lib/identity";
import spinner from "../lib/spin";

const apiPath = "/pdp/api";
let csrfToken = null;

function apiUrl(path) {
  return apiPath + path;
}

function validateResponse(res) {
  spinner.stop();

  if (!res.ok) {
    const error = new Error(res.statusText);
    error.response = res;
    throw error;
  }

  csrfToken = res.headers.get('x-csrf-token');

  return res;
}

export function parseJson(res) {
  return res.json();
}

function validFetch(path, options) {
  const contentHeaders = {
    "Accept": "application/json",
    "Content-Type": "application/json",
    "X-CSRF-TOKEN": csrfToken
  };

  let identityHeaders = {};
  if (currentIdentity) {
    identityHeaders = {
      "X-IDP-ENTITY-ID": currentIdentity.idpEntityId,
      "X-UNSPECIFIED-NAME-ID": currentIdentity.unspecifiedNameId,
      "X-DISPLAY-NAME": currentIdentity.displayName,
      "X-IMPERSONATE": true
    };
  }

  const fetchOptions = _.merge({}, { headers: { ...contentHeaders, ...identityHeaders } }, options, {
    credentials: "same-origin"
  });

  spinner.start();
  return fetch(apiUrl(path), fetchOptions)
  .catch(err => {
    spinner.stop();
    throw err;
  })
  .then(validateResponse);
}

export function fetchJson(path, options = {}) {
  return validFetch(path, options)
  .then(parseJson);
}

function postJson(path, body, options = {}) {
  return validFetch(path, Object.assign({}, { method: "post", body: JSON.stringify(body) }, options));
}

function putJson(path, body, options = {}) {
  return validFetch(path, Object.assign({}, { method: "put", body: JSON.stringify(body) }, options));
}

export function fetchDelete(path) {
  return validFetch(path, { method: "delete" });
}

export function getUserData() {
  return fetchJson("/internal/users/me");
}

export function getIdentityProviders() {
  return fetchJson("/internal/identityProviders");
}

export function getScopedIdentityProviders() {
  return fetchJson("/internal/identityProviders/scoped");
}

export function getServiceProviders() {
  return fetchJson("/internal/serviceProviders");
}

export function getAllowedAttributes() {
  return fetchJson("/internal/attributes");
}

export function getPolicies() {
  return fetchJson("/internal/policies");
}

export function deletePolicy(policyId) {
  return fetchDelete(`/internal/policies/${ policyId }`);
}

export function getRevisions(policyId) {
  return fetchJson(`/internal/revisions/${policyId}`);
}

export function getNewPolicy() {
  return fetchJson(`/internal/default-policy`);
}

export function getPolicy(policyId) {
  return fetchJson(`/internal/policies/${policyId}`);
}

export function createPolicy(policy) {
  return postJson("/internal/policies", policy, {
    headers: {
      "Content-Type": "application/json"
    }
  });
}

export function updatePolicy(policy) {
  return putJson("/internal/policies", policy, {
    headers: {
      "Content-Type": "application/json"
    }
  });
}

export function getViolations() {
  return fetchJson(`/internal/violations`);
}
