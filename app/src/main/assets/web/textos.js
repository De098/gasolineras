/* Gasolineras: los textos de la página, en castellano (es) e inglés británico (en).
   Idioma: según el del móvil (el WebView sigue al sistema). Castellano si es es, ca, gl o eu (cualquier región);
   inglés en cualquier otro caso. Como Android al elegir res/values-*, se recorre la lista de idiomas del móvil y se
   toma el primero que sea de esos o inglés.
   Uso: tr("clave", ...huecos). Un texto es una cadena con {0}, {1}… o una función (para plurales y números). */
"use strict";

const TEXTOS = {
  es: {
    app: "Gasolineras",
    // fijos del HTML
    actualizar: "Actualizar", mapa: "Mapa", lista: "Lista", orden: "Orden:", precio: "Precio", distancia: "Distancia",
    solo_abiertas: "Solo abiertas ahora",
    // combustibles (botones cortos)
    c0: "95", c1: "98", c2: "Diésel", c3: "Diésel+", c4: "GLP",
    // dinámicos
    radio: n => `Radio: ${n} km`,
    n_gas: n => n === 1 ? "1 gasolinera" : `${n} gasolineras`,
    buscando: "buscando…", cargando: "Cargando…",
    pie: (fecha, sinRed) => `Ministerio · ${fecha}${sinRed ? " (sin red: datos guardados)" : ""}`,
    precio_fmt: p => p.toFixed(3).replace(".", ",") + " €",
    num3: p => p.toFixed(3).replace(".", ","),
    km_fmt: k => k.toFixed(1).replace(".", ",") + " km",
    cerrada: "Cerrada", h24: "24 h", favorita: "Favorita", favoritas: "Favoritas", cerca: "Cerca de ti",
    sin_gas: "No hay gasolineras en este radio con ese combustible.",
    toca_gas: "Toca una gasolinera del mapa para verla aquí.",
    sin_permiso: "Hace falta el permiso de ubicación para buscar gasolineras cerca de ti.",
    sin_ubic: "No se ha podido obtener tu ubicación. Activa el GPS o la ubicación del móvil.",
    error: "Ha habido un error.",
    reintentar: "Reintentar", ajustes_app: "Ajustes de la app",
  },
  en: {
    app: "Cheap Fuel",
    actualizar: "Refresh", mapa: "Map", lista: "List", orden: "Sort by:", precio: "Price", distancia: "Distance",
    solo_abiertas: "Open now",
    c0: "95", c1: "98", c2: "Diesel", c3: "Diesel+", c4: "LPG",
    radio: n => `Within ${n} km`,
    n_gas: n => n === 1 ? "1 station" : `${n} stations`,
    buscando: "searching…", cargando: "Loading…",
    pie: (fecha, sinRed) => `Source: Spanish government · ${fecha}${sinRed ? " (offline: showing saved prices)" : ""}`,
    precio_fmt: p => "€" + p.toFixed(3),
    num3: p => p.toFixed(3),
    km_fmt: k => k.toFixed(1) + " km",
    cerrada: "Closed", h24: "24 h", favorita: "Favourite", favoritas: "Favourites", cerca: "Near you",
    sin_gas: "No petrol stations within this radius sell that fuel.",
    toca_gas: "Tap a station on the map to see its details here.",
    sin_permiso: "Location access is needed to find petrol stations near you.",
    sin_ubic: "Couldn't find your location. Turn on GPS or location services on your phone.",
    error: "Something went wrong.",
    reintentar: "Try again", ajustes_app: "App settings",
  },
};

/** Idioma de la página: "es" o "en". */
const IDIOMA = (() => {
  const SIMILARES = ["es", "ca", "gl", "eu"];
  const lista = (navigator.languages && navigator.languages.length ? navigator.languages : [navigator.language || "en"]);
  for (const l of lista) {
    const base = String(l || "").toLowerCase().split(/[-_]/)[0];
    if (SIMILARES.includes(base)) return "es";
    if (base === "en") return "en";
  }
  return "en";
})();
document.documentElement.lang = IDIOMA === "es" ? "es" : "en-GB";

/** Texto de la clave en el idioma elegido (si falta, el castellano; si no, la propia clave). */
function tr(clave, ...args) {
  let v = TEXTOS[IDIOMA][clave];
  if (v === undefined) v = TEXTOS.es[clave];
  if (v === undefined) return clave;
  if (typeof v === "function") return v(...args);
  return v.replace(/\{(\d+)\}/g, (_, i) => args[i] === undefined ? "" : args[i]);
}

/** Rellena los textos fijos del HTML: data-t (contenido), data-t-placeholder, data-t-title, data-t-aria, data-t-alt. */
function traducirPagina() {
  for (const e of document.querySelectorAll("[data-t]")) e.textContent = tr(e.dataset.t);
  for (const e of document.querySelectorAll("[data-t-placeholder]")) e.placeholder = tr(e.dataset.tPlaceholder);
  for (const e of document.querySelectorAll("[data-t-title]")) e.title = tr(e.dataset.tTitle);
  for (const e of document.querySelectorAll("[data-t-aria]")) e.setAttribute("aria-label", tr(e.dataset.tAria));
  for (const e of document.querySelectorAll("[data-t-alt]")) e.alt = tr(e.dataset.tAlt);
}
