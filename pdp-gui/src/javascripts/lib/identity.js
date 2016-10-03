export let currentIdentity = null;

export function changeIdentity(id) {
  currentIdentity = id;
}

export function clearIdentity() {
  currentIdentity = null;
}
