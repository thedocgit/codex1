import crypto from "node:crypto";

export function newId(prefix) {
  return `${prefix}_${crypto.randomUUID()}`;
}

export function assertNonEmptyString(value, field) {
  if (typeof value !== "string" || value.trim().length === 0) {
    throw new TypeError(`${field} must be a non-empty string`);
  }
  return value.trim();
}

export function freezeContract(value) {
  return Object.freeze(structuredClone(value));
}

export function nowIso() {
  return new Date().toISOString();
}
