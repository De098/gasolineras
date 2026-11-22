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
    actualizado: "Actualizado", mapa: "Mapa", lista: "Lista", orden: "Orden:", precio: "Precio", distancia: "Distancia",
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
    cerrada: "Cerrada", h24: "24 h", favorita: "Favorita", anadir_fav: "Añadir a favoritas", quitar_fav: "Quitar de favoritas", favoritas: "Favoritas", cerca: "Cerca de ti",
    sin_gas: "No hay gasolineras en este radio con ese combustible.",
    toca_gas: "Toca una gasolinera del mapa para verla aquí.",
    sin_permiso: "Hace falta el permiso de ubicación para buscar gasolineras cerca de ti.",
    sin_ubic: "No se ha podido obtener tu ubicación. Activa el GPS o la ubicación del móvil.",
    error: "Ha habido un error.",
    reintentar: "Reintentar", ajustes_app: "Ajustes de la app",
  },
  en: {
    app: "Cheap Fuel",
    actualizado: "Updated", mapa: "Map", lista: "List", orden: "Sort by:", precio: "Price", distancia: "Distance",
    solo_abiertas: "Open now",
    c0: "95", c1: "98", c2: "Diesel", c3: "Diesel+", c4: "LPG",
    radio: n => `Within ${n} km`,
    n_gas: n => n === 1 ? "1 station" : `${n} stations`,
    buscando: "searching…", cargando: "Loading…",
    pie: (fecha, sinRed) => `Source: Spanish government · ${fecha}${sinRed ? " (offline: showing saved prices)" : ""}`,
    precio_fmt: p => "€" + p.toFixed(3),
    num3: p => p.toFixed(3),
    km_fmt: k => k.toFixed(1) + " km",
    cerrada: "Closed", h24: "24 h", favorita: "Favourite", anadir_fav: "Add to favourites", quitar_fav: "Remove from favourites", favoritas: "Favourites", cerca: "Near you",
    sin_gas: "No petrol stations within this radius sell that fuel.",
    toca_gas: "Tap a station on the map to see its details here.",
    sin_permiso: "Location access is needed to find petrol stations near you.",
    sin_ubic: "Couldn't find your location. Turn on GPS or location services on your phone.",
    error: "Something went wrong.",
    reintentar: "Try again", ajustes_app: "App settings",
  },
  // francés (France): espacio fino antes de «:» y del €, coma decimal
  fr: {
    app: "Carburant pas cher",
    actualizado: "Mis à jour", mapa: "Carte", lista: "Liste", orden: "Trier par :", precio: "Prix", distancia: "Distance",
    solo_abiertas: "Ouvertes maintenant",
    c0: "95", c1: "98", c2: "Gazole", c3: "Gazole+", c4: "GPL",
    radio: n => `Rayon : ${n} km`,
    n_gas: n => n === 1 ? "1 station" : `${n} stations`,
    buscando: "recherche…", cargando: "Chargement…",
    pie: (fecha, sinRed) => `Source : gouvernement espagnol · ${fecha}${sinRed ? " (hors ligne : derniers prix enregistrés)" : ""}`,
    precio_fmt: p => p.toFixed(3).replace(".", ",") + " €",
    num3: p => p.toFixed(3).replace(".", ","),
    km_fmt: k => k.toFixed(1).replace(".", ",") + " km",
    cerrada: "Fermée", h24: "24h/24", favorita: "Favori", anadir_fav: "Ajouter aux favoris", quitar_fav: "Retirer des favoris", favoritas: "Favoris", cerca: "Autour de vous",
    sin_gas: "Aucune station ne vend ce carburant dans ce rayon.",
    toca_gas: "Touchez une station sur la carte pour afficher ses détails ici.",
    sin_permiso: "L’accès à votre position est nécessaire pour trouver les stations autour de vous.",
    sin_ubic: "Impossible de trouver votre position. Activez le GPS ou la localisation sur votre téléphone.",
    error: "Un problème est survenu.",
    reintentar: "Réessayer", ajustes_app: "Paramètres de l’appli",
  },
  // catalán
  ca: {
    app: "Benzineres",
    actualizado: "Actualitzat", mapa: "Mapa", lista: "Llista", orden: "Ordena per:", precio: "Preu", distancia: "Distància",
    solo_abiertas: "Només obertes ara",
    c0: "95", c1: "98", c2: "Dièsel", c3: "Dièsel+", c4: "GLP",
    radio: n => `Radi: ${n} km`,
    n_gas: n => n === 1 ? "1 benzinera" : `${n} benzineres`,
    buscando: "cercant…", cargando: "Carregant…",
    pie: (fecha, sinRed) => `Font: Ministeri per a la Transició Ecològica · ${fecha}${sinRed ? " (sense connexió: preus desats)" : ""}`,
    precio_fmt: p => p.toFixed(3).replace(".", ",") + " €",
    num3: p => p.toFixed(3).replace(".", ","),
    km_fmt: k => k.toFixed(1).replace(".", ",") + " km",
    cerrada: "Tancada", h24: "24 h", favorita: "Preferida", anadir_fav: "Afegeix a preferides", quitar_fav: "Treu de preferides", favoritas: "Preferides", cerca: "A prop teu",
    sin_gas: "No hi ha cap benzinera en aquest radi amb aquest carburant.",
    toca_gas: "Toca una benzinera del mapa per veure-la aquí.",
    sin_permiso: "Cal el permís d’ubicació per buscar benzineres a prop teu.",
    sin_ubic: "No s’ha pogut saber on ets. Activa el GPS o la ubicació del mòbil.",
    error: "Hi ha hagut un error.",
    reintentar: "Torna-ho a provar", ajustes_app: "Configuració de l’app",
  },
  // alemán (de tú, como la mayoría de apps libres)
  de: {
    app: "Günstig tanken",
    actualizado: "Stand", mapa: "Karte", lista: "Liste", orden: "Sortieren:", precio: "Preis", distancia: "Entfernung",
    solo_abiertas: "Nur geöffnete",
    c0: "Super", c1: "Super Plus", c2: "Diesel", c3: "Diesel+", c4: "LPG",
    radio: n => `Umkreis: ${n} km`,
    n_gas: n => n === 1 ? "1 Tankstelle" : `${n} Tankstellen`,
    buscando: "suche…", cargando: "Wird geladen…",
    pie: (fecha, sinRed) => `Quelle: spanisches Ministerium · ${fecha}${sinRed ? " (offline: gespeicherte Preise)" : ""}`,
    precio_fmt: p => p.toFixed(3).replace(".", ",") + " €",
    num3: p => p.toFixed(3).replace(".", ","),
    km_fmt: k => k.toFixed(1).replace(".", ",") + " km",
    cerrada: "Geschlossen", h24: "24 h", favorita: "Favorit", anadir_fav: "Zu Favoriten", quitar_fav: "Aus Favoriten entfernen", favoritas: "Favoriten", cerca: "In deiner Nähe",
    sin_gas: "In diesem Umkreis gibt es keine Tankstelle mit dieser Kraftstoffsorte.",
    toca_gas: "Tippe auf eine Tankstelle auf der Karte, um sie hier zu sehen.",
    sin_permiso: "Für Tankstellen in deiner Nähe braucht die App Zugriff auf deinen Standort.",
    sin_ubic: "Dein Standort wurde nicht gefunden. Schalte GPS bzw. die Standortermittlung ein.",
    error: "Da ist etwas schiefgelaufen.",
    reintentar: "Nochmal versuchen", ajustes_app: "App-Einstellungen",
  },
  // italiano
  it: {
    app: "Pieno Conveniente",
    actualizado: "Aggiornato", mapa: "Mappa", lista: "Elenco", orden: "Ordina per:", precio: "Prezzo", distancia: "Distanza",
    solo_abiertas: "Solo aperti ora",
    c0: "95", c1: "98", c2: "Gasolio", c3: "Gasolio+", c4: "GPL",
    radio: n => `Raggio: ${n} km`,
    n_gas: n => n === 1 ? "1 distributore" : `${n} distributori`,
    buscando: "ricerca…", cargando: "Caricamento…",
    pie: (fecha, sinRed) => `Fonte: governo spagnolo · ${fecha}${sinRed ? " (offline: prezzi salvati)" : ""}`,
    precio_fmt: p => p.toFixed(3).replace(".", ",") + " €",
    num3: p => p.toFixed(3).replace(".", ","),
    km_fmt: k => k.toFixed(1).replace(".", ",") + " km",
    cerrada: "Chiuso", h24: "H24", favorita: "Preferito", anadir_fav: "Aggiungi ai preferiti", quitar_fav: "Togli dai preferiti", favoritas: "Preferiti", cerca: "Vicino a te",
    sin_gas: "Nessun distributore in questo raggio vende questo carburante.",
    toca_gas: "Tocca un distributore sulla mappa per vederlo qui.",
    sin_permiso: "Serve l’accesso alla posizione per trovare i distributori vicino a te.",
    sin_ubic: "Non riesco a trovare la tua posizione. Attiva il GPS o la localizzazione del telefono.",
    error: "Qualcosa è andato storto.",
    reintentar: "Riprova", ajustes_app: "Impostazioni dell’app",
  },
  // portugués (de Portugal)
  pt: {
    app: "Combustível Barato",
    actualizado: "Atualizado", mapa: "Mapa", lista: "Lista", orden: "Ordenar por:", precio: "Preço", distancia: "Distância",
    solo_abiertas: "Só abertos agora",
    c0: "95", c1: "98", c2: "Gasóleo", c3: "Gasóleo+", c4: "GPL",
    radio: n => `Raio: ${n} km`,
    n_gas: n => n === 1 ? "1 posto" : `${n} postos`,
    buscando: "a procurar…", cargando: "A carregar…",
    pie: (fecha, sinRed) => `Fonte: Governo de Espanha · ${fecha}${sinRed ? " (sem ligação: preços guardados)" : ""}`,
    precio_fmt: p => p.toFixed(3).replace(".", ",") + " €",
    num3: p => p.toFixed(3).replace(".", ","),
    km_fmt: k => k.toFixed(1).replace(".", ",") + " km",
    cerrada: "Fechado", h24: "24 h", favorita: "Favorito", anadir_fav: "Adicionar aos favoritos", quitar_fav: "Remover dos favoritos", favoritas: "Favoritos", cerca: "Perto de si",
    sin_gas: "Não há postos neste raio com este combustível.",
    toca_gas: "Toque num posto no mapa para o ver aqui.",
    sin_permiso: "É preciso acesso à localização para encontrar postos perto de si.",
    sin_ubic: "Não foi possível obter a sua localização. Ligue o GPS ou a localização do telemóvel.",
    error: "Algo correu mal.",
    reintentar: "Tentar de novo", ajustes_app: "Definições da app",
  },
};

/** Idioma de la página: "es", "en", "fr", "ca", "de", "it" o "pt". */
const IDIOMA = (() => {
  const SIMILARES = ["es", "gl", "eu"];   // gallego y euskera: castellano
  const lista = (navigator.languages && navigator.languages.length ? navigator.languages : [navigator.language || "en"]);
  for (const l of lista) {
    const base = String(l || "").toLowerCase().split(/[-_]/)[0];
    if (SIMILARES.includes(base)) return "es";
    if (base === "en") return "en";
    if (base === "fr") return "fr";
    if (["ca", "de", "it", "pt"].includes(base)) return base;
  }
  return "en";
})();
document.documentElement.lang = IDIOMA === "en" ? "en-GB" : IDIOMA;

/** Texto de la clave en el idioma elegido (si falta: en otro idioma, el inglés; si no, el castellano; si no, la propia clave). */
function tr(clave, ...args) {
  let v = TEXTOS[IDIOMA][clave];
  if (v === undefined && IDIOMA !== "es") v = TEXTOS.en[clave];
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
