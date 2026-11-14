package io.github.de098.gasolineras

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicBoolean

/** Ubicación con LocationManager (sin Google Play Services). Hay que llamarlo desde cualquier hilo; contesta en el principal. */
object Ubicacion {
    private const val ESPERA = 20000L

    fun tienePermiso(c: Context) =
        c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun proveedores(c: Context, lm: LocationManager): List<String> {
        val fino = c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        // primero la red (rápida); el GPS solo con permiso preciso
        return listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { (it != LocationManager.GPS_PROVIDER || fino) && try { lm.isProviderEnabled(it) } catch (_: Exception) { false } }
    }

    /** La última conocida más reciente de todos los proveedores. */
    @Suppress("MissingPermission")
    private fun ultima(lm: LocationManager): Location? = try {
        lm.getProviders(true).mapNotNull { try { lm.getLastKnownLocation(it) } catch (_: Exception) { null } }.maxByOrNull { it.time }
    } catch (_: Exception) { null }

    @Suppress("MissingPermission")
    fun obtener(c: Context, alTerminar: (Location?) -> Unit) {
        val lm = c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provs = if (tienePermiso(c)) proveedores(c, lm) else emptyList()
        val h = Handler(Looper.getMainLooper())
        val hecho = AtomicBoolean(false)
        fun fin(l: Location?) { if (hecho.compareAndSet(false, true)) alTerminar(l ?: ultima(lm)) }
        if (provs.isEmpty()) { h.post { fin(null) }; return }
        h.postDelayed({ fin(null) }, ESPERA)
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                lm.getCurrentLocation(provs[0], null, c.mainExecutor) { l -> fin(l) }
            } else {
                lm.requestSingleUpdate(provs[0], object : android.location.LocationListener {
                    override fun onLocationChanged(l: Location) { fin(l) }
                    override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {}
                    override fun onProviderEnabled(p: String) {}
                    override fun onProviderDisabled(p: String) {}
                }, Looper.getMainLooper())
            }
        } catch (_: Exception) { fin(null) }
    }

    /** Última ubicación guardada por la app (para el widget). */
    fun guardada(c: Context): Pair<Double, Double>? {
        val p = c.getSharedPreferences("gasolineras", Context.MODE_PRIVATE)
        val la = p.getString("lat", null)?.toDoubleOrNull() ?: return null
        val lo = p.getString("lon", null)?.toDoubleOrNull() ?: return null
        return Pair(la, lo)
    }

    fun guardar(c: Context, lat: Double, lon: Double) {
        c.getSharedPreferences("gasolineras", Context.MODE_PRIVATE).edit().putString("lat", lat.toString()).putString("lon", lon.toString()).apply()
    }
}
