export function capitalize(string: string): string {
  if (string.length == 0) {
    return string;
  }

  return string.charAt(0).toUpperCase() + string.substring(1);
}

export function redashUuid(uuid: string): string {
  return uuid.replace(
    /^([0-9a-f]{8})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{12})$/i,
    "$1-$2-$3-$4-$5",
  );
}
