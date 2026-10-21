package mx.uaemex.anden.domain

import mx.uaemex.anden.data.local.entity.PendingReportEntity
import mx.uaemex.anden.data.local.entity.StationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AndenRulesTest {

    private val now = 1_800_000_000_000L
    private fun minutes(m: Long) = m * 60_000L

    private fun station(id: String, name: String, lat: Double, lon: Double, elevator: String = "Operando", fav: Boolean = false) =
        StationEntity(
            stationId = id, name = name, systemLine = "Tren El Insurgente", municipality = name,
            latitude = lat, longitude = lon, scheduleText = "", elevatorStatus = elevator,
            rampStatus = "Accesibles", serviceStatus = "Servicio Regular",
            feederConnections = "Circuito Tollocan;Ruta Tenango", isFavorite = fav,
        )

    private val stations = listOf(
        station("INS-01", "Zinacantepec", 19.2869, -99.7014),
        station("INS-02", "Toluca Centro", 19.2709, -99.6562, fav = true),
        station("INS-04", "Lerma", 19.2825, -99.5125, elevator = "En mantenimiento"),
        station("INS-07", "Observatorio", 19.3982, -99.2003),
    )

    private fun report(station: String, ageMin: Long, moderation: String = "APROBADO") = PendingReportEntity(
        stationId = station, stationName = station, incidentType = "Aglomeración", description = "",
        mediaUri = "", latitude = 0.0, longitude = 0.0, timestampMillis = now - minutes(ageMin),
        moderationStatus = moderation,
    )

    // ---------- Geocerca de 300 m (RF-04) ----------
    @Test fun geocercaAceptaHasta300Metros() {
        assertTrue(AndenRules.isWithinGeofence(0))
        assertTrue(AndenRules.isWithinGeofence(85))
        assertTrue(AndenRules.isWithinGeofence(300))
    }

    @Test fun geocercaRechazaFueraDelRadio() {
        assertFalse(AndenRules.isWithinGeofence(301))
        assertFalse(AndenRules.isWithinGeofence(450))
        assertFalse(AndenRules.isWithinGeofence(-1))
    }

    // ---------- Caducidad de 30 minutos (RF-03) ----------
    @Test fun reporteVigenteAntesDeTreintaMinutos() {
        assertTrue(AndenRules.isReportActive(report("INS-03", 0), now))
        assertTrue(AndenRules.isReportActive(report("INS-03", 29), now))
    }

    @Test fun reporteCaducaALosTreintaMinutos() {
        assertFalse(AndenRules.isReportActive(report("INS-03", 30), now))
        assertFalse(AndenRules.isReportActive(report("INS-03", 45), now))
    }

    @Test fun reporteDescartadoPorModeracionNoSeMuestra() {
        assertFalse(AndenRules.isReportActive(report("INS-03", 5, moderation = "DESCARTADO"), now))
        assertTrue(AndenRules.isReportActive(report("INS-03", 5, moderation = "PENDIENTE"), now))
    }

    @Test fun reportesActivosSoloDeLaEstacionPedida() {
        val all = listOf(report("INS-03", 10), report("INS-03", 40), report("INS-02", 5))
        val active = AndenRules.activeReportsForStation("INS-03", all, now)
        assertEquals(1, active.size)
        assertEquals("INS-03", active.first().stationId)
    }

    // ---------- Búsqueda y filtros (RF-09, RF-10) ----------
    @Test fun busquedaPorNombreSinDistinguirMayusculas() {
        val r = AndenRules.filterStations(stations, "lerma", accessibleOnly = false, favoritesOnly = false)
        assertEquals(listOf("Lerma"), r.map { it.name })
    }

    @Test fun busquedaPorCorredorAlimentador() {
        val r = AndenRules.filterStations(stations, "tollocan", accessibleOnly = false, favoritesOnly = false)
        assertEquals(stations.size, r.size)
    }

    @Test fun filtroDeAccesibilidadExcluyeElevadorEnMantenimiento() {
        val r = AndenRules.filterStations(stations, "", accessibleOnly = true, favoritesOnly = false)
        assertFalse(r.any { it.name == "Lerma" })
        assertEquals(3, r.size)
    }

    @Test fun filtroDeFavoritos() {
        val r = AndenRules.filterStations(stations, "", accessibleOnly = false, favoritesOnly = true)
        assertEquals(listOf("Toluca Centro"), r.map { it.name })
    }

    // ---------- Distancia y estación más cercana (RF-04) ----------
    @Test fun haversineCeroEnElMismoPunto() {
        assertEquals(0, AndenRules.haversineMeters(19.2709, -99.6562, 19.2709, -99.6562))
    }

    @Test fun unaMilesimaDeGradoDeLatitudSonUnos111Metros() {
        assertEquals(111, AndenRules.haversineMeters(19.2709, -99.6562, 19.2719, -99.6562))
    }

    @Test fun estacionMasCercanaDesdeUnPuntoEnLerma() {
        val (st, d) = AndenRules.nearestStation(19.2830, -99.5130, stations)!!
        assertEquals("Lerma", st.name)
        assertTrue("d = $d", AndenRules.isWithinGeofence(d))
    }

    @Test fun sinEstacionesNoHayMasCercana() {
        assertNull(AndenRules.nearestStation(19.0, -99.0, emptyList()))
    }
}
