# Gasolineras

Enseña las gasolineras más baratas en un radio ajustable (1-50 km) alrededor de tu ubicación, en lista o en mapa.
Android 10 o superior. Licencia GPL-3.0. Hermana de [Cartera](https://github.com/De098/cartera).

- **Combustibles**: Gasolina 95 y 98, Diésel, Diésel+ y GLP. Orden por precio o distancia, favoritas arriba,
  filtro «Solo abiertas ahora» (interpreta el horario del Ministerio) y etiqueta «24 h».
- **Mapa** con Leaflet 1.9.4 (BSD-2, en `assets/web/leaflet/`) y teselas de OpenStreetMap.
- **Widget** «Las 3 más baratas»; el botón ⟳ del widget descarga en segundo plano. Sin trabajos periódicos.
- Al pulsar una gasolinera se abre la app de mapas que elijas.

## Datos
Precios oficiales de la API REST del Ministerio (`sedeaplicaciones.minetur.gob.es`, Precios de Carburantes). Se guardan
compactados en un único archivo de la app y se reutilizan 30 minutos; sin red se usa lo último guardado y se indica su fecha.

## Privacidad
Sin cuenta, sin anuncios, sin rastreo y sin Google Play Services. La ubicación se usa solo en el móvil para calcular
distancias y nunca se envía. Las únicas peticiones de red son los precios del Ministerio y las teselas del mapa.
No tiene permisos de segundo plano.
