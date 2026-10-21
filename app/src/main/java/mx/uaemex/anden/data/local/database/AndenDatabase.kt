package mx.uaemex.anden.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.uaemex.anden.data.local.dao.AndenDao
import mx.uaemex.anden.data.local.entity.CommunityStopEntity
import mx.uaemex.anden.data.local.entity.PendingReportEntity
import mx.uaemex.anden.data.local.entity.StationEntity

@Database(
    entities = [
        StationEntity::class,
        PendingReportEntity::class,
        CommunityStopEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AndenDatabase : RoomDatabase() {

    abstract fun andenDao(): AndenDao

    companion object {
        @Volatile
        private var INSTANCE: AndenDatabase? = null

        fun getInstance(context: Context): AndenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AndenDatabase::class.java,
                    "anden_movilidad_toluca.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                CoroutineScope(Dispatchers.IO).launch {
                    seedInitialDataIfNeeded(instance.andenDao())
                }
                instance
            }
        }

        private suspend fun seedInitialDataIfNeeded(dao: AndenDao) {
            if (dao.getStationsCount() > 0) return

            val initialStations = listOf(
                StationEntity(
                    stationId = "INS-01",
                    name = "Zinacantepec",
                    systemLine = "Tren El Insurgente",
                    municipality = "Zinacantepec, Méx.",
                    latitude = 19.2869,
                    longitude = -99.7014,
                    scheduleText = "Horario: 06:00 AM - 11:00 PM",
                    elevatorStatus = "Operando",
                    rampStatus = "Accesibles",
                    serviceStatus = "Servicio Regular (Sin retrasos)",
                    feederConnections = "Línea 6 Mexibús (Zinacantepec - Lerma);Ramal Villa Cuauhtémoc;Corredor Av. Las Torres",
                    isFavorite = true
                ),
                StationEntity(
                    stationId = "INS-02",
                    name = "Toluca Centro",
                    systemLine = "Tren El Insurgente",
                    municipality = "Toluca de Lerdo, Méx.",
                    latitude = 19.2709,
                    longitude = -99.6562,
                    scheduleText = "Horario: 06:00 AM - 11:00 PM",
                    elevatorStatus = "Operando",
                    rampStatus = "Accesibles",
                    serviceStatus = "Servicio Regular (Sin retrasos)",
                    feederConnections = "Terminal de Autobuses Toluca;Corredor Circuito Tollocan;Ramal Tenango del Valle;Ramal Zolotepec",
                    isFavorite = true
                ),
                StationEntity(
                    stationId = "INS-03",
                    name = "Metepec",
                    systemLine = "Tren El Insurgente",
                    municipality = "Metepec, Méx.",
                    latitude = 19.2842,
                    longitude = -99.6556,
                    scheduleText = "Horario: 06:00 AM - 11:00 PM",
                    elevatorStatus = "Operando",
                    rampStatus = "Accesibles",
                    serviceStatus = "Servicio Regular (Sin retrasos)",
                    feederConnections = "Estación Metepec - San Bartolomé Tlatelulco;Circuito Tollocan;Línea 6 Mexibús Conexión Pino Suárez",
                    isFavorite = true
                ),
                StationEntity(
                    stationId = "INS-04",
                    name = "Lerma",
                    systemLine = "Tren El Insurgente",
                    municipality = "Lerma de Villada, Méx.",
                    latitude = 19.2825,
                    longitude = -99.5125,
                    scheduleText = "Horario: 06:00 AM - 11:00 PM",
                    elevatorStatus = "En mantenimiento",
                    rampStatus = "Accesibles",
                    serviceStatus = "Afluencia moderada en andén",
                    feederConnections = "Línea 6 Mexibús (Terminal Lerma);Corredor Industrial Lerma;Ruta Amomolulco - Ocoyoacac"
                ),
                StationEntity(
                    stationId = "INS-05",
                    name = "Santa Fe",
                    systemLine = "Tren El Insurgente",
                    municipality = "Cuajimalpa / Álvaro Obregón, CDMX",
                    latitude = 19.3614,
                    longitude = -99.2736,
                    scheduleText = "Horario: 06:00 AM - 11:00 PM",
                    elevatorStatus = "Operando",
                    rampStatus = "Accesibles",
                    serviceStatus = "Servicio Regular (Sin retrasos)",
                    feederConnections = "RTP Santa Fe - Auditorio;RTP Santa Fe - Metro Balderas;Corredor Centro Comercial Santa Fe"
                ),
                StationEntity(
                    stationId = "INS-06",
                    name = "Vasco de Quiroga",
                    systemLine = "Tren El Insurgente",
                    municipality = "Álvaro Obregón, CDMX",
                    latitude = 19.3812,
                    longitude = -99.2345,
                    scheduleText = "Horario: 06:00 AM - 11:00 PM",
                    elevatorStatus = "Operando",
                    rampStatus = "Accesibles",
                    serviceStatus = "Servicio Regular (Sin retrasos)",
                    feederConnections = "Cablebús Línea 3 (Los Pinos / Constituyentes);Rutas locales Pueblo Santa Fe"
                ),
                StationEntity(
                    stationId = "INS-07",
                    name = "Observatorio",
                    systemLine = "Tren El Insurgente",
                    municipality = "Álvaro Obregón, CDMX",
                    latitude = 19.3982,
                    longitude = -99.2003,
                    scheduleText = "Horario: 06:00 AM - 11:00 PM",
                    elevatorStatus = "Operando",
                    rampStatus = "Accesibles",
                    serviceStatus = "Enlace multimodal CETRAM Observatorio",
                    feederConnections = "Metro CDMX Línea 1;Terminal de Autobuses Poniente;CETRAM Observatorio"
                )
            )
            dao.insertStations(initialStations)

            val now = System.currentTimeMillis()
            val initialReports = listOf(
                PendingReportEntity(
                    stationId = "INS-03",
                    stationName = "Metepec",
                    incidentType = "Aglomeración",
                    description = "Afluencia regular en acceso norte, flujo continuo en torniquetes.",
                    mediaUri = "content://anden/evidencia_metepec_andenes.jpg",
                    isVideo = false,
                    latitude = 19.2842,
                    longitude = -99.6556,
                    accuracyMeters = 4.8f,
                    distanceToStationMeters = 85,
                    authorHandle = "@UsuarioAnonimo",
                    timestampMillis = now - (10 * 60 * 1000L), // Hace 10 min
                    syncStatus = "SINCRONIZADO_FIRESTORE",
                    moderationStatus = "APROBADO"
                ),
                PendingReportEntity(
                    stationId = "INS-03",
                    stationName = "Metepec",
                    incidentType = "Elevador funcionando",
                    description = "Elevador oriente hacia el vestíbulo operando sin contratiempos.",
                    mediaUri = "content://anden/evidencia_elevador_metepec.jpg",
                    isVideo = false,
                    latitude = 19.2843,
                    longitude = -99.6555,
                    accuracyMeters = 5.0f,
                    distanceToStationMeters = 62,
                    authorHandle = "@VecinoMetepec",
                    timestampMillis = now - (22 * 60 * 1000L), // Hace 22 min
                    syncStatus = "SINCRONIZADO_FIRESTORE",
                    moderationStatus = "APROBADO"
                ),
                PendingReportEntity(
                    stationId = "INS-03",
                    stationName = "Metepec",
                    incidentType = "Retraso de tren",
                    description = "Espera de 5 min en andén dirección Observatorio.",
                    mediaUri = "",
                    isVideo = false,
                    latitude = 19.2841,
                    longitude = -99.6557,
                    accuracyMeters = 6.2f,
                    distanceToStationMeters = 90,
                    authorHandle = "@EstudianteUAEMex",
                    timestampMillis = now - (12 * 60 * 1000L), // Hace 12 min
                    syncStatus = "SINCRONIZADO_FIRESTORE",
                    moderationStatus = "APROBADO"
                ),
                PendingReportEntity(
                    stationId = "INS-02",
                    stationName = "Toluca Centro",
                    incidentType = "Aglomeración",
                    description = "Fila moderada en conexión con camiones hacia Tenango y CU.",
                    mediaUri = "content://anden/evidencia_toluca_centro.jpg",
                    isVideo = false,
                    latitude = 19.2709,
                    longitude = -99.6562,
                    accuracyMeters = 5.1f,
                    distanceToStationMeters = 110,
                    authorHandle = "@PasajeroToluca",
                    timestampMillis = now - (15 * 60 * 1000L),
                    syncStatus = "SINCRONIZADO_FIRESTORE",
                    moderationStatus = "PENDIENTE"
                )
            )
            dao.insertReports(initialReports)

            val initialStops = listOf(
                CommunityStopEntity(
                    title = "Parada: Cruce Tollocan y Pilares",
                    corridorReference = "Cerca de estación Metepec / Toluca Centro",
                    nearestStationName = "Metepec",
                    authorHandle = "@VecinoToluca",
                    timeAgoText = "Hace 2 horas",
                    latitude = 19.2865,
                    longitude = -99.6312,
                    moderationStatus = "PENDIENTE"
                ),
                CommunityStopEntity(
                    title = "Parada: Paseo de los Matlatzincas",
                    corridorReference = "Ruta local hacia Zinacantepec Centro",
                    nearestStationName = "Zinacantepec",
                    authorHandle = "@Usuario99",
                    timeAgoText = "Hace 4 horas",
                    latitude = 19.2951,
                    longitude = -99.6621,
                    moderationStatus = "PENDIENTE"
                ),
                CommunityStopEntity(
                    title = "Parada: Av. Las Torres esq. Pino Suárez",
                    corridorReference = "Conexión alimentadora hacia San Bartolomé Tlatelulco",
                    nearestStationName = "Metepec",
                    authorHandle = "@ComunidadPotro",
                    timeAgoText = "Hace 1 hora",
                    latitude = 19.2778,
                    longitude = -99.6489,
                    moderationStatus = "PENDIENTE"
                )
            )
            dao.insertCommunityStops(initialStops)
        }
    }
}
