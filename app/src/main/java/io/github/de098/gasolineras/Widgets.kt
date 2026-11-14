package io.github.de098.gasolineras

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import kotlin.concurrent.thread

/** Abrir una gasolinera en la app de mapas que se elija (o en el navegador si no hay ninguna). */
object Mapas {
    fun intent(c: Context, lat: Double, lon: Double, nombre: String): Intent {
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(nombre)})"))
        val hay = geo.resolveActivity(c.packageManager) != null
        val i = if (hay) Intent.createChooser(geo, "Abrir con")
        else Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/?mlat=$lat&mlon=$lon#map=17/$lat/$lon"))
        return i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

/** Widget «Las 3 más baratas», con la última ubicación y los datos guardados por la app. */
object Widgets {
    const val ACCION_REFRESCAR = "io.github.de098.gasolineras.REFRESCAR"

    fun actualizarTodos(c: Context) {
        val am = AppWidgetManager.getInstance(c)
        for (wid in am.getAppWidgetIds(ComponentName(c, WBaratas::class.java))) pintar(c, am, wid)
    }

    private fun pintar(c: Context, am: AppWidgetManager, wid: Int) {
        val v = RemoteViews(c.packageName, R.layout.w_baratas)
        val p = c.getSharedPreferences("gasolineras", Context.MODE_PRIVATE)
        val comb = p.getInt("comb", 0).coerceIn(0, 4)
        v.setTextViewText(R.id.titulo, "Las 3 más baratas · " + Datos.NOMBRES[comb])
        val abrirApp = PendingIntent.getActivity(c, 9200 + wid,
            Intent(c, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        v.setOnClickPendingIntent(R.id.titulo, abrirApp)
        v.setOnClickPendingIntent(R.id.vacio, abrirApp)
        v.setOnClickPendingIntent(R.id.refrescar, PendingIntent.getBroadcast(c, 9400 + wid,
            Intent(c, WBaratas::class.java).setAction(ACCION_REFRESCAR),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))

        val pos = Ubicacion.guardada(c)
        val datos = Datos.guardados(c)
        val top = if (pos != null && datos != null)
            Datos.cercanas(datos.lista, pos.first, pos.second, comb, p.getInt("radio", 12).toDouble()).take(3) else emptyList()
        // cada fila es su propio RemoteViews (los ids internos se repiten)
        v.removeAllViews(R.id.raiz_filas)
        for ((i, g) in top.withIndex()) {
            val f = RemoteViews(c.packageName, R.layout.w_barata_fila)
            f.setTextViewText(R.id.t, g.e.rotulo)
            f.setTextViewText(R.id.s, (g.e.loc + " · " + String.format(java.util.Locale("es", "ES"), "%.1f km", g.km)).trim(' ', '·'))
            f.setTextViewText(R.id.p, String.format(java.util.Locale("es", "ES"), "%.3f €", g.precio))
            f.setOnClickPendingIntent(R.id.fila, PendingIntent.getActivity(c, 100 + i + wid * 10,
                Mapas.intent(c, g.e.lat, g.e.lon, g.e.rotulo), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            v.addView(R.id.raiz_filas, f)
        }
        v.setViewVisibility(R.id.vacio, if (top.isEmpty()) View.VISIBLE else View.GONE)
        v.setTextViewText(R.id.vacio, if (pos == null || datos == null) "Abre la app para ver las gasolineras cercanas" else "Ninguna gasolinera en el radio")
        am.updateAppWidget(wid, v)
    }
}

class WBaratas : AppWidgetProvider() {
    override fun onUpdate(c: Context, am: AppWidgetManager, ids: IntArray) = Widgets.actualizarTodos(c)

    override fun onReceive(c: Context, i: Intent) {
        if (i.action == Widgets.ACCION_REFRESCAR) {
            // descarga en segundo plano (sin trabajos periódicos): solo cuando se pulsa ⟳
            val pend = goAsync()
            thread {
                try { Datos.obtener(c, true) } catch (_: Exception) {}
                try { Widgets.actualizarTodos(c) } finally { pend.finish() }
            }
        } else super.onReceive(c, i)
    }
}
