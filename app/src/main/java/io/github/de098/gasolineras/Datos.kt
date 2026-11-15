package io.github.de098.gasolineras

import android.content.Context
import android.util.JsonReader
import android.util.JsonToken
import java.io.InputStreamReader
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.util.zip.GZIPInputStream
import kotlin.math.*

/** Un fallo que se le puede contar a la persona tal cual. */
class ErrorUsuario(mensaje: String) : Exception(mensaje)

class Est(val id: String, val rotulo: String, val dir: String, val loc: String, val horario: String,
          val lat: Double, val lon: Double, val precios: DoubleArray)   // precios: 95, 98, diésel, diésel+, GLP (0 = no vende)

class Cerca(val e: Est, val km: Double, val precio: Double)

class Resultado(val lista: List<Est>, val fecha: String, val sinRed: Boolean)

/**
 * Datos del Ministerio. La caché es SIEMPRE un único archivo (filesDir/estaciones.json) con solo los campos usados:
 * cada descarga se escribe en un temporal y se renombra encima del anterior; si falla, se conserva el anterior.
 */
object Datos {
    private const val URL_API = "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/"
    private const val VIGENCIA = 30L * 60 * 1000   // 30 minutos
    val CLAVES = listOf("Precio Gasolina 95 E5", "Precio Gasolina 98 E5", "Precio Gasoleo A", "Precio Gasoleo Premium", "Precio Gases licuados del petróleo")
    /** Nombre largo del combustible (95, 98, diésel, diésel+, GLP) en el idioma del móvil. */
    fun nombre(i: Int): String = s(listOf(R.string.comb0, R.string.comb1, R.string.comb2, R.string.comb3, R.string.comb4)[i])

    // Context de la aplicación, guardado para poder traducir en este object (que no recibe Context en todas sus funciones)
    private var ctx: Context? = null
    fun iniciar(c: Context) { ctx = c.applicationContext }

    /** Texto de res/values (en el idioma del móvil) con sus huecos. */
    fun s(id: Int, vararg a: Any): String = ctx!!.getString(id, *a)

    /** Formato de números según el idioma elegido para los recursos (coma decimal en castellano, punto en inglés). */
    fun localeNumeros(): java.util.Locale = java.util.Locale.forLanguageTag(s(R.string.idioma_numeros))

    private var memoria: Pair<Long, Resultado>? = null   // (cuándo se descargó, datos)

    private fun archivo(c: Context): File { iniciar(c); return File(c.applicationContext.filesDir, "estaciones.json") }

    /** Datos guardados, sin tocar la red (para el widget). */
    @Synchronized
    fun guardados(c: Context): Resultado? = cargar(c)?.second

    @Synchronized
    private fun cargar(c: Context): Pair<Long, Resultado>? {
        memoria?.let { return it }
        val f = archivo(c)
        if (!f.exists()) return null
        return try {
            val j = JSONObject(f.readText())
            val a = j.getJSONArray("e")
            val l = ArrayList<Est>(a.length())
            for (i in 0 until a.length()) {
                val r = a.getJSONArray(i)
                l += Est(r.getString(0), r.getString(1), r.getString(2), r.getString(3), r.getString(4),
                    r.getDouble(5), r.getDouble(6), DoubleArray(5) { r.getDouble(7 + it) })
            }
            val p = Pair(j.getLong("t"), Resultado(l, j.optString("fecha"), false))
            memoria = p
            p
        } catch (_: Exception) { null }
    }

    /** Datos listos: la caché si tiene menos de 30 min (y no se fuerza); si no, los baja; sin red, la caché vieja. */
    @Synchronized
    fun obtener(c: Context, forzar: Boolean): Resultado {
        val viejo = cargar(c)
        if (!forzar && viejo != null && System.currentTimeMillis() - viejo.first < VIGENCIA) return viejo.second
        try {
            val nuevo = descargar(c)
            memoria = nuevo
            return nuevo.second
        } catch (e: Exception) {
            if (viejo != null) return Resultado(viejo.second.lista, viejo.second.fecha, true)
            throw ErrorUsuario(s(R.string.err_sin_datos))
        }
    }

    private fun num(s: String): Double = s.trim().replace(',', '.').toDoubleOrNull() ?: 0.0

    /** Estación a partir de los campos leídos; null si no tiene coordenadas o no vende ninguno de los 5 combustibles. */
    private fun estacion(o: Map<String, String>): Est? {
        val lat = num(o["Latitud"] ?: ""); val lon = num(o["Longitud (WGS84)"] ?: "")
        if (lat == 0.0 && lon == 0.0) return null
        val pr = DoubleArray(5) { num(o[CLAVES[it]] ?: "") }
        if (pr.all { it <= 0.0 }) return null
        val loc = (o["Localidad"] ?: "").ifBlank { o["Municipio"] ?: "" }
        return Est(o["IDEESS"] ?: "", (o["Rótulo"] ?: "").trim(), (o["Dirección"] ?: "").trim(), loc.trim(),
            (o["Horario"] ?: "").trim(), lat, lon, pr)
    }

    /** Campos del Ministerio que se guardan; el resto se salta sin leerlo. */
    private val USADOS = setOf("IDEESS", "Rótulo", "Dirección", "Localidad", "Municipio", "Horario", "Latitud", "Longitud (WGS84)") + CLAVES

    /** Valor como texto (el Ministerio da todo como cadena, pero por si viene null o número). */
    private fun cadena(jr: JsonReader): String =
        if (jr.peek() == JsonToken.NULL) { jr.nextNull(); "" } else if (jr.peek() == JsonToken.STRING || jr.peek() == JsonToken.NUMBER) jr.nextString() else { jr.skipValue(); "" }

    private fun descargar(c: Context): Pair<Long, Resultado> {
        val destino = archivo(c)
        val tmp = File(destino.parentFile, "estaciones.json.tmp")
        tmp.delete()   // resto de una descarga a medias
        val con = URL(URL_API).openConnection() as HttpURLConnection
        try {
            con.connectTimeout = 15000
            con.readTimeout = 60000
            con.setRequestProperty("Accept-Encoding", "gzip")
            con.setRequestProperty("Accept", "application/json")
            if (con.responseCode != 200) throw ErrorUsuario(s(R.string.err_ministerio, con.responseCode))
            var flujo = con.inputStream
            if ("gzip".equals(con.contentEncoding, true)) flujo = GZIPInputStream(flujo)
            // se lee en flujo (son unos 12 MB): el JSON original nunca está entero en memoria ni en disco;
            // a disco va únicamente la versión compactada
            val buf = java.io.BufferedInputStream(flujo)
            buf.mark(3)   // saltar el BOM de UTF-8 si lo trae
            if (!(buf.read() == 0xEF && buf.read() == 0xBB && buf.read() == 0xBF)) buf.reset()
            val salida = JSONArray()
            val lista = ArrayList<Est>(12000)
            var fecha = ""
            val jr = JsonReader(InputStreamReader(buf, Charsets.UTF_8))
            try {
                jr.beginObject()
                while (jr.hasNext()) {
                    when (jr.nextName()) {
                        "Fecha" -> fecha = cadena(jr)
                        "ListaEESSPrecio" -> {
                            jr.beginArray()
                            while (jr.hasNext()) {
                                val o = HashMap<String, String>()
                                jr.beginObject()
                                while (jr.hasNext()) {
                                    val k = jr.nextName()
                                    if (k in USADOS) o[k] = cadena(jr) else jr.skipValue()
                                }
                                jr.endObject()
                                val e = estacion(o) ?: continue
                                lista += e
                                val r = JSONArray().put(e.id).put(e.rotulo).put(e.dir).put(e.loc).put(e.horario).put(e.lat).put(e.lon)
                                for (p in e.precios) r.put(p)
                                salida.put(r)
                            }
                            jr.endArray()
                        }
                        else -> jr.skipValue()
                    }
                }
                jr.endObject()
            } finally { jr.close() }
            if (lista.isEmpty()) throw ErrorUsuario(s(R.string.err_vacia))
            val ahora = System.currentTimeMillis()
            tmp.writeText(JSONObject().put("t", ahora).put("fecha", fecha).put("e", salida).toString())
            if (!tmp.renameTo(destino)) { destino.delete(); tmp.renameTo(destino) }
            return Pair(ahora, Resultado(lista, fecha, false))
        } finally {
            con.disconnect()
            tmp.delete()
        }
    }

    /** «15/11/2026 0:41:12» → «15/11 a las 0:41» (castellano) o «15 Nov at 00:41» (inglés). */
    fun fechaCorta(f: String): String {
        val m = Regex("(\\d+)/(\\d+)/\\d+\\s+(\\d+):(\\d+)").find(f) ?: return f
        val (d, mes, h, mi) = m.destructured
        val nombreMes = ctx!!.resources.getStringArray(R.array.meses_cortos)[(mes.toInt() - 1).coerceIn(0, 11)]
        return s(R.string.fmt_fecha, d.toInt(), mes.toInt(), h.toInt(), mi, nombreMes)
    }

    fun distancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1); val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(a))
    }

    /** Las del radio que venden ese combustible, por precio (y a igual precio, la más cercana). */
    fun cercanas(lista: List<Est>, lat: Double, lon: Double, comb: Int, radioKm: Double): List<Cerca> {
        val dLat = radioKm / 111.0   // caja previa barata antes del haversine
        return lista.asSequence()
            .filter { it.precios[comb] > 0 && abs(it.lat - lat) <= dLat }
            .map { Cerca(it, distancia(lat, lon, it.lat, it.lon), it.precios[comb]) }
            .filter { it.km <= radioKm }
            .sortedWith(compareBy<Cerca> { it.precio }.thenBy { it.km })
            .toList()
    }
}

/** Interpreta el campo «Horario» del Ministerio: «L-D: 24H», «L-V: 06:00-22:00; S: 07:00-15:00»… */
object Horario {
    private const val DIAS = "LMXJVSD"

    /** Intervalos (minutos desde las 0:00; el final puede pasar de 1440 si cruza la medianoche) de cada día; null si no se entiende. */
    fun parsear(h: String): Array<MutableList<IntArray>>? {
        val s = h.trim().uppercase()
        if (s.isEmpty()) return null
        val res = Array(7) { mutableListOf<IntArray>() }
        val rango = Regex("(\\d{1,2}):(\\d{2})\\s*-\\s*(\\d{1,2}):(\\d{2})")
        for (seg in s.split(';')) {
            val t = seg.trim()
            if (t.isEmpty()) continue
            val i = t.indexOf(':')
            if (i < 0) return null
            val ds = mutableSetOf<Int>()
            for (g in t.substring(0, i).split(',')) {
                val p = g.trim().split('-').map { it.trim() }
                if (p.any { it.length != 1 || DIAS.indexOf(it) < 0 } || p.size > 2) return null
                val a = DIAS.indexOf(p[0])
                if (p.size == 1) ds += a else {
                    val b = DIAS.indexOf(p[1]); var d = a
                    while (true) { ds += d; if (d == b) break; d = (d + 1) % 7 }
                }
            }
            val horas = t.substring(i + 1).trim()
            val iv = mutableListOf<IntArray>()
            if (horas.replace(" ", "") == "24H") iv += intArrayOf(0, 1440) else {
                for (r in horas.split(Regex("\\s+Y\\s+|\\s*,\\s*"))) {
                    val m = rango.matchEntire(r.trim()) ?: return null
                    val (a, b, c, d) = m.destructured
                    val ini = a.toInt() * 60 + b.toInt()
                    var fin = c.toInt() * 60 + d.toInt()
                    if (fin <= ini) fin += 1440
                    iv += intArrayOf(ini, fin)
                }
            }
            for (d in ds) res[d].addAll(iv)
        }
        return res
    }

    /** Todos los días abierta las 24 h. */
    fun es24h(p: Array<MutableList<IntArray>>?) = p != null && p.all { d -> d.any { it[0] == 0 && it[1] >= 1440 } }

    /** 0 = no se sabe, 1 = abierta, 2 = cerrada. */
    fun estado(p: Array<MutableList<IntArray>>?, ahora: LocalDateTime): Int {
        if (p == null) return 0
        val hoy = ahora.dayOfWeek.value - 1
        val min = ahora.hour * 60 + ahora.minute
        if (p[hoy].any { min >= it[0] && min < it[1] }) return 1
        // lo que viene de ayer cruzando la medianoche
        if (p[(hoy + 6) % 7].any { min + 1440 >= it[0] && min + 1440 < it[1] }) return 1
        return 2
    }
}
