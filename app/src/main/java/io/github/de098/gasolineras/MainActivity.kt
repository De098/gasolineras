package io.github.de098.gasolineras

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.view.WindowInsets
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.window.OnBackInvokedDispatcher
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import kotlin.concurrent.thread

/**
 * Gasolineras. La interfaz es una página web empaquetada (assets/web/gasolineras.html) en un WebView. La página se
 * sirve desde un host inventado (nada sale por él); lo único que se pide de internet son las teselas del mapa de
 * OpenStreetMap. La página habla con la app por el puente «Gas» y las respuestas vuelven con window.alNativo({tipo: …}).
 */
class MainActivity : Activity() {

    companion object {
        private const val HOST = "gasolineras.local"
        private const val PERMISO = 1
        private const val PREFS = "gasolineras"
    }

    private lateinit var web: WebView
    private var pidiendo = false                  // el diálogo de permiso está en pantalla
    private var ultima: Triple<Int, Int, Boolean>? = null   // (combustible, radio, forzar) de la última carga

    private inner class Puente {
        /** Calcula y devuelve (con alNativo) las gasolineras del radio. */
        @JavascriptInterface
        fun cargar(comb: Int, radio: Int, forzar: Boolean) {
            ultima = Triple(comb, radio, forzar)
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt("comb", comb).putInt("radio", radio).apply()
            runOnUiThread { arrancar() }
        }

        /** Vuelve a pedir el permiso de ubicación. */
        @JavascriptInterface
        fun reintentar() = runOnUiThread { pedirPermiso() }

        /** Ajustes de la app (si el permiso se denegó para siempre). */
        @JavascriptInterface
        fun ajustes() = runOnUiThread {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        }

        /** Abre la gasolinera en la app de mapas elegida. */
        @JavascriptInterface
        fun abrirMapa(lat: Double, lon: Double, nombre: String) = runOnUiThread {
            try { startActivity(Mapas.intent(this@MainActivity, lat, lon, nombre)) } catch (_: Exception) {}
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(estado: Bundle?) {
        super.onCreate(estado)
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.fondo))
        }
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true   // localStorage: radio, combustible, favoritas…
            settings.setSupportZoom(false)
            settings.textZoom = Math.round(resources.configuration.fontScale * 100).coerceIn(80, 200)
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            setBackgroundColor(getColor(R.color.fondo))
            webViewClient = Cliente()
            addJavascriptInterface(Puente(), "Gas")
        }
        raiz.addView(web, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        if (Build.VERSION.SDK_INT >= 30) {
            // de borde a borde, dejando hueco para las barras del sistema y el teclado
            window.setDecorFitsSystemWindows(false)
            raiz.setOnApplyWindowInsetsListener { v, ins ->
                val s = ins.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
                v.setPadding(s.left, s.top, s.right, s.bottom)
                WindowInsets.CONSUMED
            }
        } else raiz.fitsSystemWindows = true   // Android 10: el sistema deja el hueco
        setContentView(raiz)
        if (Build.VERSION.SDK_INT >= 33)
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) { atras() }
        web.loadUrl("https://$HOST/")
        if (!Ubicacion.tienePermiso(this)) pedirPermiso()   // al abrir la app
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = atras()

    private fun atras() {
        web.evaluateJavascript("window.atras ? atras() : false") { r -> if (r != "true") moveTaskToBack(true) }
    }

    private fun pedirPermiso() {
        pidiendo = true
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISO)
    }

    override fun onRequestPermissionsResult(code: Int, permisos: Array<out String>, res: IntArray) {
        super.onRequestPermissionsResult(code, permisos, res)
        if (code != PERMISO) return
        pidiendo = false
        if (res.any { it == PackageManager.PERMISSION_GRANTED }) arrancar()
        else js(JSONObject().put("tipo", "sinPermiso"))
    }

    private fun js(o: JSONObject) {
        val t = o.toString().replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")
        runOnUiThread { web.evaluateJavascript("window.alNativo($t)", null) }
    }

    private fun arrancar() {
        val (comb, radio, forzar) = ultima ?: return
        if (pidiendo) return   // cuando conteste el diálogo se repite solo
        if (!Ubicacion.tienePermiso(this)) { js(JSONObject().put("tipo", "sinPermiso")); return }
        js(JSONObject().put("tipo", "cargando"))
        Ubicacion.obtener(this) { loc ->
            if (loc == null) {
                js(JSONObject().put("tipo", "sinUbicacion"))
                return@obtener
            }
            Ubicacion.guardar(this, loc.latitude, loc.longitude)
            thread {
                try {
                    val r = Datos.obtener(this, forzar)
                    val cerca = Datos.cercanas(r.lista, loc.latitude, loc.longitude, comb, radio.toDouble())
                    val ahora = LocalDateTime.now()
                    val a = JSONArray()
                    for (g in cerca) {
                        val p = Horario.parsear(g.e.horario)
                        a.put(JSONObject().put("id", g.e.id).put("r", g.e.rotulo).put("d", g.e.dir).put("l", g.e.loc)
                            .put("km", g.km).put("lat", g.e.lat).put("lon", g.e.lon).put("p", g.precio)
                            .put("ab", Horario.estado(p, ahora)).put("h24", Horario.es24h(p)))
                    }
                    js(JSONObject().put("tipo", "datos").put("lat", loc.latitude).put("lon", loc.longitude)
                        .put("radio", radio).put("fecha", Datos.fechaCorta(r.fecha)).put("sinRed", r.sinRed).put("lista", a))
                    Widgets.actualizarTodos(this)
                } catch (e: ErrorUsuario) {
                    js(JSONObject().put("tipo", "error").put("mensaje", e.message))
                } catch (e: Exception) {
                    js(JSONObject().put("tipo", "error").put("mensaje", "No se han podido leer los precios."))
                }
            }
        }
    }

    private fun mime(p: String) = when (p.substringAfterLast('.', "")) {
        "html" -> "text/html"; "js" -> "text/javascript"; "css" -> "text/css"; "svg" -> "image/svg+xml"
        "png" -> "image/png"; "woff2" -> "font/woff2"; else -> "application/octet-stream"
    }

    private inner class Cliente : WebViewClient() {
        override fun shouldInterceptRequest(v: WebView, r: WebResourceRequest): WebResourceResponse? {
            val host = r.url.host
            if (host == "tile.openstreetmap.org") return null   // únicas peticiones a internet: teselas del mapa
            if (host != HOST) return WebResourceResponse("text/plain", "utf-8", 404, "Bloqueado", emptyMap(), ByteArrayInputStream(ByteArray(0)))
            var path = r.url.path ?: "/"
            if (path == "/") path = "/gasolineras.html"
            if (path.contains("..")) return WebResourceResponse("text/plain", "utf-8", 404, "No", emptyMap(), ByteArrayInputStream(ByteArray(0)))
            return try {
                WebResourceResponse(mime(path), "utf-8", assets.open("web$path"))
            } catch (_: Exception) {
                WebResourceResponse("text/plain", "utf-8", 404, "No encontrado", emptyMap(), ByteArrayInputStream(ByteArray(0)))
            }
        }

        override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean = r.url.host != HOST
    }
}
