let base : string = import.meta.env.BASE_URL;
const baseHref = (document.getElementById('base-href-id'));
if (typeof(baseHref) != 'undefined' && baseHref != null) {
  const attr : Attr | null = baseHref.attributes.getNamedItem("href");
  if (attr !== null) {
    base = attr.value;
  }
}
export const PATHS = {
  CANCEL: base + 'view/cancel',
  COMPLETE: base + 'view/complete',
  ERROR: base + 'view/error',
};
