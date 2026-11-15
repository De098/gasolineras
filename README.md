# Gasolineras

Enseña las gasolineras más baratas en un radio ajustable (1-50 km) alrededor de tu ubicación, en lista o en mapa.
Android 10 o superior. Licencia GPL-3.0. Hermana de [Cartera](https://github.com/De098/cartera).

[![Descargar APK](https://img.shields.io/github/v/release/De098/gasolineras?label=Descargar%20APK&logo=android&style=for-the-badge)](https://github.com/De098/gasolineras/releases/latest)
[![Obtainium](https://img.shields.io/badge/Obtainium-actualizaciones%20autom%C3%A1ticas-5a4fcf?style=for-the-badge)](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/De098/gasolineras)

Descarga el `.apk` de la última versión (en **Assets**) y ábrelo en el móvil. Con [Obtainium](https://github.com/ImranR98/Obtainium)
se instala y se actualiza sola: pulsa el segundo botón desde el móvil o añade `https://github.com/De098/gasolineras`.

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

## Cómo está hecha

Kotlin sin dependencias (solo la plataforma Android): nada de AndroidX ni Google Play Services. La interfaz es una página
web empaquetada en `app/src/main/assets/web` (`gasolineras.html`, con Leaflet para el mapa) que se enseña en un `WebView`
servido desde un host inventado; la página habla con la app por el puente `Gas` y las respuestas vuelven con
`window.alNativo(…)`.

| Archivo | Qué hace |
|---|---|
| `Datos.kt` | descarga los precios del Ministerio leyéndolos en flujo, guarda solo los campos usados en `estaciones.json` y calcula las gasolineras del radio |
| `Ubicacion.kt` | ubicación con `LocationManager` (GPS o red), reutilizando la reciente si tiene menos de 2 minutos |
| `MainActivity.kt` | el `WebView`, el puente con la página, el permiso de ubicación y abrir una gasolinera en la app de mapas |
| `Widgets.kt` | el widget «Las 3 más baratas» y el selector de app de mapas |

Permisos: `INTERNET` (precios del Ministerio y teselas del mapa), `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`
(solo con la app abierta; con la aproximada también funciona). **No** tiene ubicación en segundo plano.

## Compilar

```
./gradlew assembleDebug      # APK de pruebas
./gradlew assembleRelease    # sin firmar (F-Droid firma con su clave)
```

Hace falta JDK 17 o superior y el SDK de Android 36.

## Licencias

Código: GPL-3.0 (`LICENSE`). Leaflet 1.9.4: BSD-2 (`app/src/main/assets/web/leaflet/LICENSE`). Tipografía Nunito:
SIL OFL 1.1 (`app/src/main/assets/web/nunito-LICENCIA.txt`). Mapa: © colaboradores de
[OpenStreetMap](https://www.openstreetmap.org/copyright) (ODbL). Precios: Ministerio para la Transición Ecológica y el
Reto Demográfico (datos abiertos).
