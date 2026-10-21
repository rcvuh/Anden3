package mx.uaemex.anden.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para el catálogo offline-first de estaciones y corredores
 * del Tren Interurbano "El Insurgente", Línea 6 del Mexibús y Corredores Naturales.
 */
@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val stationId: String,
    val name: String,
    val systemLine: String, // "Tren El Insurgente", "Mexibús Línea 6", "Corredor Natural"
    val municipality: String,
    val latitude: Double,
    val longitude: Double,
    val scheduleText: String,
    val elevatorStatus: String, // "Operando", "En mantenimiento", "Sin servicio"
    val rampStatus: String,     // "Accesibles", "En revisión"
    val serviceStatus: String,  // "Servicio Regular (Sin retrasos)", "Afluencia Alta", "Demora 10 min"
    val feederConnections: String, // Separadas por ';'
    val isFavorite: Boolean = false
)

/**
 * Entidad Room para reportes colaborativos ciudadanos con soporte Offline-First.
 * Si el dispositivo pierde cobertura en andenes subterráneos o zonas de sombra,
 * persiste en SQLite local con syncStatus = "PENDIENTE_ROOM" y se sincroniza
 * hacia Firebase Firestore y Cloud Storage al restablecer la red.
 */
@Entity(tableName = "pending_reports")
data class PendingReportEntity(
    @PrimaryKey(autoGenerate = true) val reportId: Long = 0,
    val firestoreDocId: String = "",
    val stationId: String,
    val stationName: String,
    val incidentType: String, // "Elevador dañado", "Aglomeración", "Retraso de tren", "Bloqueo de corredor"
    val description: String,
    val mediaUri: String,
    val isVideo: Boolean = false,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 5.0f,
    val distanceToStationMeters: Int = 85,
    val authorHandle: String = "@UsuarioAnonimo",
    val timestampMillis: Long = System.currentTimeMillis(),
    val syncStatus: String = "SINCRONIZADO_FIRESTORE", // "PENDIENTE_ROOM" | "SINCRONIZADO_FIRESTORE"
    val moderationStatus: String = "APROBADO"          // "PENDIENTE" | "APROBADO" | "DESCARTADO"
)

/**
 * Entidad Room para propuestas ciudadanas de paradas alimentadoras de última milla
 * sujetas a auditoría en el AdminDashboardFragment.
 */
@Entity(tableName = "community_stops")
data class CommunityStopEntity(
    @PrimaryKey(autoGenerate = true) val stopId: Long = 0,
    val title: String,
    val corridorReference: String,
    val nearestStationName: String,
    val authorHandle: String,
    val timeAgoText: String,
    val latitude: Double,
    val longitude: Double,
    val moderationStatus: String = "PENDIENTE" // "PENDIENTE" | "VALIDADA" | "DESCARTADA"
)
