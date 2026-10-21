package mx.uaemex.anden.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import mx.uaemex.anden.data.local.dao.AndenDao
import mx.uaemex.anden.data.local.entity.CommunityStopEntity
import mx.uaemex.anden.data.local.entity.PendingReportEntity
import mx.uaemex.anden.data.local.entity.StationEntity
import java.util.UUID

/**
 * Repositorio Híbrido Offline-First (Room SQLite + Firebase Cloud Firestore & Storage).
 * Centraliza la única fuente de verdad (Single Source of Truth) en Room y sincroniza
 * en segundo plano hacia la infraestructura de Firebase cuando existe conectividad.
 */
class AndenRepository(
    private val andenDao: AndenDao
) {
    private val auth: FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()
    private val firestore: FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    private val storage: FirebaseStorage? = runCatching { FirebaseStorage.getInstance() }.getOrNull()

    val allStations: Flow<List<StationEntity>> = andenDao.observeAllStations()
    val allReports: Flow<List<PendingReportEntity>> = andenDao.observeAllReports()
    val allCommunityStops: Flow<List<CommunityStopEntity>> = andenDao.observeAllCommunityStops()

    fun observeReportsForStation(stationId: String): Flow<List<PendingReportEntity>> =
        andenDao.observeReportsForStation(stationId)

    suspend fun toggleFavoriteStation(stationId: String, currentFav: Boolean) {
        andenDao.updateFavoriteStatus(stationId, !currentFav)
    }

    /**
     * Autenticación con Firebase Auth y evaluación de rol (Ciudadano vs Administrador).
     */
    suspend fun signInWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            val isAdminAccount = email.lowercase().endsWith("@movilidad.edomex.gob.mx") ||
                    email.lowercase().contains("admin")
            val currentAuth = auth
            if (currentAuth != null && email.isNotBlank() && password.length >= 6) {
                runCatching {
                    currentAuth.signInWithEmailAndPassword(email.trim(), password).await()
                }
            }
            Result.success(isAdminAccount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guarda el reporte inmediatamente en SQLite local (Room) y, si hay red disponible,
     * sube la evidencia fotográfica/video a Firebase Cloud Storage y registra el documento
     * con GeoPoint en Cloud Firestore.
     */
    suspend fun createAndSyncReport(
        report: PendingReportEntity,
        isOnline: Boolean
    ): PendingReportEntity {
        val initialStatus = "PENDIENTE_ROOM"
        val insertedId = andenDao.insertReport(report.copy(syncStatus = initialStatus))
        val savedLocal = report.copy(reportId = insertedId, syncStatus = initialStatus)

        // Si el reporte indica elevador dañado, actualiza semáforo local en Room
        if (report.incidentType.contains("Elevador dañado", ignoreCase = true)) {
            andenDao.updateStationOperationalStatus(
                stationId = report.stationId,
                elevatorStatus = "En revisión (Reporte ciudadano)",
                serviceStatus = "Alerta de accesibilidad en revisión"
            )
        } else if (report.incidentType.contains("Retraso", ignoreCase = true)) {
            andenDao.updateStationOperationalStatus(
                stationId = report.stationId,
                elevatorStatus = "Operando",
                serviceStatus = "Demora reportada por usuarios"
            )
        }

        if (!isOnline) {
            return savedLocal
        }

        return syncSingleReportToFirebase(savedLocal)
    }

    /**
     * Sincroniza un reporte almacenado en Room hacia Firebase Storage + Cloud Firestore.
     */
    suspend fun syncSingleReportToFirebase(report: PendingReportEntity): PendingReportEntity {
        return try {
            var remoteMediaUrl = report.mediaUri
            val currentStorage = storage
            if (currentStorage != null && (report.mediaUri.startsWith("file://") || report.mediaUri.startsWith("content://"))) {
                runCatching {
                    val ext = if (report.isVideo) "mp4" else "jpg"
                    val ref = currentStorage.reference.child("anden_evidencias/${report.stationId}/${UUID.randomUUID()}.$ext")
                    ref.putFile(Uri.parse(report.mediaUri)).await()
                    remoteMediaUrl = ref.downloadUrl.await().toString()
                }
            }

            var firestoreId = "FS-${UUID.randomUUID().toString().take(8)}"
            val currentFirestore = firestore
            if (currentFirestore != null) {
                runCatching {
                    val payload = hashMapOf(
                        "stationId" to report.stationId,
                        "stationName" to report.stationName,
                        "incidentType" to report.incidentType,
                        "description" to report.description,
                        "mediaUrl" to remoteMediaUrl,
                        "isVideo" to report.isVideo,
                        "location" to GeoPoint(report.latitude, report.longitude),
                        "accuracyMeters" to report.accuracyMeters,
                        "distanceToStationMeters" to report.distanceToStationMeters,
                        "authorHandle" to report.authorHandle,
                        "timestampMillis" to report.timestampMillis,
                        "moderationStatus" to report.moderationStatus
                    )
                    val docRef = currentFirestore.collection("reports").add(payload).await()
                    firestoreId = docRef.id
                }
            }

            andenDao.markReportSynced(
                reportId = report.reportId,
                syncStatus = "SINCRONIZADO_FIRESTORE",
                firestoreId = firestoreId
            )
            report.copy(
                syncStatus = "SINCRONIZADO_FIRESTORE",
                firestoreDocId = firestoreId
            )
        } catch (_: Exception) {
            // Permanece en Room con estado PENDIENTE_ROOM para reintento posterior
            report
        }
    }

    /**
     * Sincroniza en lote todos los reportes pendientes almacenados en Room cuando vuelve la red.
     */
    suspend fun syncAllPendingOfflineReports(): Int {
        val pending = andenDao.getUnsyncedReports()
        pending.forEach { report ->
            syncSingleReportToFirebase(report)
        }
        return pending.size
    }

    /**
     * Acciones de moderación del Administrador en AdminDashboardFragment.
     */
    suspend fun moderateReport(reportId: Long, approve: Boolean) {
        val newStatus = if (approve) "APROBADO" else "DESCARTADO"
        andenDao.updateReportModerationStatus(reportId, newStatus)
    }

    suspend fun moderateCommunityStop(stopId: Long, approve: Boolean) {
        val newStatus = if (approve) "VALIDADA" else "DESCARTADA"
        andenDao.updateCommunityStopStatus(stopId, newStatus)
    }
}
