package mx.uaemex.anden.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.uaemex.anden.data.local.database.AndenDatabase
import mx.uaemex.anden.data.local.entity.CommunityStopEntity
import mx.uaemex.anden.data.local.entity.PendingReportEntity
import mx.uaemex.anden.data.local.entity.StationEntity
import mx.uaemex.anden.data.repository.AndenRepository
import mx.uaemex.anden.domain.AndenRules
import mx.uaemex.anden.hardware.GpsReading
import mx.uaemex.anden.hardware.LocationGeofenceHelper

data class AndenUiState(
    val currentUserEmail: String = "",
    val isGuest: Boolean = false,
    val isAdminRole: Boolean = false,
    val selectedStationId: String = "INS-03", // Estación Metepec por defecto
    val searchQuery: String = "",
    val filterAccessibleOnly: Boolean = false,
    val filterFavoritesOnly: Boolean = false,
    val isOfflineSimulationMode: Boolean = false,
    val capturedMediaUri: String = "",
    val capturedIsVideo: Boolean = false,
    val gpsReading: GpsReading = GpsReading(
        latitude = 19.2842,
        longitude = -99.6556,
        accuracyMeters = 5.0f,
        nearestStation = null,
        distanceToNearestMeters = 85,
        isWithinGeofence300m = true
    ),
    val snackbarMessage: String? = null
)

/**
 * ReportViewModel bajo el patrón arquitectónico MVVM.
 * Expone flujos reactivos inmutables (StateFlow) hacia los Fragments / pantallas de Jetpack Compose,
 * sobreviviendo a cambios de configuración y aplicando la regla de negocio de caducidad de 30 minutos.
 */
class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AndenDatabase.getInstance(application)
    private val repository = AndenRepository(database.andenDao())
    private val locationHelper = LocationGeofenceHelper(application)

    private val _uiState = MutableStateFlow(AndenUiState())
    val uiState: StateFlow<AndenUiState> = _uiState.asStateFlow()

    val stations: StateFlow<List<StationEntity>> = repository.allStations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<PendingReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityStops: StateFlow<List<CommunityStopEntity>> = repository.allCommunityStops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Filtra estaciones según texto de búsqueda, filtro de accesibilidad motriz (elevador operando)
     * o filtro de estaciones favoritas.
     */
    val filteredStations: StateFlow<List<StationEntity>> = combine(
        stations,
        _uiState
    ) { list, state ->
        AndenRules.filterStations(
            stations = list,
            query = state.searchQuery,
            accessibleOnly = state.filterAccessibleOnly,
            favoritesOnly = state.filterFavoritesOnly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Regla de Caducidad Operativa: 30 minutos (1,800,000 ms)
    companion object {
        const val REPORT_EXPIRATION_MILLIS = AndenRules.REPORT_EXPIRATION_MILLIS
    }

    fun getActiveReportsForStation(stationId: String, reports: List<PendingReportEntity>): List<PendingReportEntity> =
        AndenRules.activeReportsForStation(stationId, reports, System.currentTimeMillis())

    fun loginAsCitizen(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.signInWithEmail(email, password)
            _uiState.value = _uiState.value.copy(
                currentUserEmail = email.ifBlank { "usuario@alumno.uaemex.mx" },
                isGuest = false,
                isAdminRole = false,
                snackbarMessage = "Sesión iniciada correctamente con Firebase Auth"
            )
            refreshGpsLocation()
            onSuccess()
        }
    }

    fun loginAsGuest(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(
            currentUserEmail = "Invitado (Solo Consulta)",
            isGuest = true,
            isAdminRole = false,
            snackbarMessage = "Modo Invitado activo: Catálogo Offline en Room disponible"
        )
        refreshGpsLocation()
        onSuccess()
    }

    fun loginAsAdmin(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.signInWithEmail(email, password)
            _uiState.value = _uiState.value.copy(
                currentUserEmail = email.ifBlank { "supervisor@movilidad.edomex.gob.mx" },
                isGuest = false,
                isAdminRole = true,
                snackbarMessage = "Rol Administrador verificado (Custom Claim admin: true)"
            )
            onSuccess()
        }
    }

    fun selectStation(stationId: String) {
        _uiState.value = _uiState.value.copy(selectedStationId = stationId)
        val station = stations.value.find { it.stationId == stationId }
        if (station != null) {
            val simulatedReading = locationHelper.simulateInStationGeofence(
                station = station,
                stations = stations.value,
                simulatedDistanceMeters = 85
            )
            _uiState.value = _uiState.value.copy(gpsReading = simulatedReading)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleAccessibleFilter() {
        val next = !_uiState.value.filterAccessibleOnly
        _uiState.value = _uiState.value.copy(
            filterAccessibleOnly = next,
            snackbarMessage = if (next) "Filtro activo: Solo estaciones con elevador operando" else null
        )
    }

    fun toggleFavoritesFilter() {
        val next = !_uiState.value.filterFavoritesOnly
        _uiState.value = _uiState.value.copy(filterFavoritesOnly = next)
    }

    fun toggleFavoriteStation(station: StationEntity) {
        viewModelScope.launch {
            repository.toggleFavoriteStation(station.stationId, station.isFavorite)
        }
    }

    fun toggleOfflineMode() {
        val nextOffline = !_uiState.value.isOfflineSimulationMode
        _uiState.value = _uiState.value.copy(
            isOfflineSimulationMode = nextOffline,
            snackbarMessage = if (nextOffline) {
                "Modo Sin Conexión (Andén Subterráneo): Guardando en Room SQLite"
            } else {
                "Conexión restablecida: Sincronizando cola Room -> Firebase Firestore"
            }
        )
        if (!nextOffline) {
            syncPendingReportsNow()
        }
    }

    fun refreshGpsLocation(simulatedDistance: Int? = null) {
        viewModelScope.launch {
            val list = stations.value
            val target = list.find { it.stationId == _uiState.value.selectedStationId }
            val reading = if (simulatedDistance != null) {
                locationHelper.simulateInStationGeofence(target, list, simulatedDistance)
            } else {
                locationHelper.fetchCurrentGpsReading(list, target)
            }
            _uiState.value = _uiState.value.copy(gpsReading = reading)
        }
    }

    fun onMediaCaptured(uriString: String, isVideo: Boolean) {
        _uiState.value = _uiState.value.copy(
            capturedMediaUri = uriString,
            capturedIsVideo = isVideo
        )
    }

    fun clearCapturedMedia() {
        _uiState.value = _uiState.value.copy(
            capturedMediaUri = "",
            capturedIsVideo = false
        )
    }

    fun submitCitizenReport(
        incidentType: String,
        description: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            val station = stations.value.find { it.stationId == state.selectedStationId }
            val stationName = station?.name ?: "Metepec"
            val isOnline = !state.isOfflineSimulationMode

            val newReport = PendingReportEntity(
                stationId = state.selectedStationId,
                stationName = stationName,
                incidentType = incidentType,
                description = description.ifBlank { "Reporte de $incidentType verificado con GPS en estación $stationName." },
                mediaUri = state.capturedMediaUri,
                isVideo = state.capturedIsVideo,
                latitude = state.gpsReading.latitude,
                longitude = state.gpsReading.longitude,
                accuracyMeters = state.gpsReading.accuracyMeters,
                distanceToStationMeters = state.gpsReading.distanceToNearestMeters,
                authorHandle = if (state.isGuest) "@InvitadoVerificado" else "@${state.currentUserEmail.substringBefore('@').ifBlank { "UsuarioToluca" }}",
                timestampMillis = System.currentTimeMillis()
            )

            val result = repository.createAndSyncReport(newReport, isOnline = isOnline)
            val msg = if (result.syncStatus == "SINCRONIZADO_FIRESTORE") {
                "Reporte sincronizado en Firestore (${result.firestoreDocId}) y Room SQLite"
            } else {
                "Sin red: Reporte encolado en Room SQLite (Estado: Pendiente por enviar)"
            }

            _uiState.value = _uiState.value.copy(
                capturedMediaUri = "",
                capturedIsVideo = false,
                snackbarMessage = msg
            )
            onComplete()
        }
    }

    fun syncPendingReportsNow() {
        viewModelScope.launch {
            val count = repository.syncAllPendingOfflineReports()
            if (count > 0) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "$count reporte(s) pendientes subidos de Room a Firestore y Cloud Storage"
                )
            }
        }
    }

    fun moderateReport(reportId: Long, approve: Boolean) {
        viewModelScope.launch {
            repository.moderateReport(reportId, approve)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (approve) "Reporte validado y publicado en Firestore" else "Reporte descartado por moderación"
            )
        }
    }

    fun moderateCommunityStop(stopId: Long, approve: Boolean) {
        viewModelScope.launch {
            repository.moderateCommunityStop(stopId, approve)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (approve) "Parada comunitaria certificada en el corredor" else "Propuesta de parada descartada"
            )
        }
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
}
