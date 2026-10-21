package mx.uaemex.anden.domain

import mx.uaemex.anden.data.local.entity.PendingReportEntity
import mx.uaemex.anden.data.local.entity.StationEntity
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Reglas de negocio de Andén en Kotlin puro (sin dependencias de Android),
 * para poder verificarlas con pruebas unitarias JUnit en la JVM.
 */
object AndenRules {

    /** Un reporte deja de mostrarse 30 minutos después de emitirse. */
    const val REPORT_EXPIRATION_MILLIS = 30 * 60 * 1000L

    /** Radio de la geocerca: sólo se puede reportar a 300 m o menos de la estación. */
    const val MAX_GEOFENCE_METERS = 300

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun isWithinGeofence(distanceMeters: Int): Boolean =
        distanceMeters in 0..MAX_GEOFENCE_METERS

    /** Vigente si no fue descartado y tiene menos de 30 minutos de antigüedad. */
    fun isReportActive(report: PendingReportEntity, nowMillis: Long): Boolean =
        report.moderationStatus != "DESCARTADO" &&
            report.timestampMillis <= nowMillis &&
            nowMillis - report.timestampMillis < REPORT_EXPIRATION_MILLIS

    fun activeReportsForStation(
        stationId: String,
        reports: List<PendingReportEntity>,
        nowMillis: Long,
    ): List<PendingReportEntity> =
        reports.filter { it.stationId == stationId && isReportActive(it, nowMillis) }

    /** Búsqueda por texto + filtro de accesibilidad (elevador operando) + favoritos. */
    fun filterStations(
        stations: List<StationEntity>,
        query: String,
        accessibleOnly: Boolean,
        favoritesOnly: Boolean,
    ): List<StationEntity> = stations.filter { st ->
        val matchesQuery = query.isBlank() ||
            st.name.contains(query, ignoreCase = true) ||
            st.municipality.contains(query, ignoreCase = true) ||
            st.feederConnections.contains(query, ignoreCase = true)
        val matchesA11y = !accessibleOnly || st.elevatorStatus.contains("Operando", ignoreCase = true)
        val matchesFav = !favoritesOnly || st.isFavorite
        matchesQuery && matchesA11y && matchesFav
    }

    /** Distancia de Haversine en metros (referencia para validar Location.distanceBetween). */
    fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return (EARTH_RADIUS_METERS * 2 * atan2(sqrt(a), sqrt(1 - a))).roundToInt()
    }

    /** Estación más cercana a un punto, con su distancia en metros. */
    fun nearestStation(lat: Double, lon: Double, stations: List<StationEntity>): Pair<StationEntity, Int>? =
        stations.map { it to haversineMeters(lat, lon, it.latitude, it.longitude) }.minByOrNull { it.second }
}
