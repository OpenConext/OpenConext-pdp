import { EventEmitter } from "events";

export const emitter = new EventEmitter();

let flash = null;
let timeout = null;

export function getFlash() {
  const tempFlash = flash;
  timeout = setTimeout(() => flash = null, 100);
  return tempFlash;
}

export function setFlash(message, type) {
  clearTimeout(timeout);
  flash = { message, type: type || "info" };
  emitter.emit("flash", flash);
}
