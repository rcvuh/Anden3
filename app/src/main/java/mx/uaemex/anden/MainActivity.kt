package mx.uaemex.anden

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.uaemex.anden.ui.screens.*
import mx.uaemex.anden.ui.theme.AndenTheme
import mx.uaemex.anden.ui.viewmodel.ReportViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndenTheme {
                val reportViewModel: ReportViewModel = viewModel()
                AndenNavigationHost(reportViewModel = reportViewModel)
            }
        }
    }
}

@Composable
fun AndenNavigationHost(reportViewModel: ReportViewModel) {
    val navController = rememberNavController()
    val uiState by reportViewModel.uiState.collectAsStateWithLifecycle()
    val stations by reportViewModel.filteredStations.collectAsStateWithLifecycle()
    val allStations by reportViewModel.stations.collectAsStateWithLifecycle()
    val allReports by reportViewModel.allReports.collectAsStateWithLifecycle()
    val communityStops by reportViewModel.communityStops.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            reportViewModel.clearSnackbar()
        }
    }

    val currentStation = allStations.find { it.stationId == uiState.selectedStationId }
        ?: allStations.firstOrNull()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = "LoginFragment"
            ) {
                // 1. LoginFragment
                composable("LoginFragment") {
                    LoginFragmentScreen(
                        onLoginCitizen = { email, pwd ->
                            reportViewModel.loginAsCitizen(email, pwd) {
                                navController.navigate("MapHomeFragment") {
                                    popUpTo("LoginFragment") { inclusive = true }
                                }
                            }
                        },
                        onLoginGuest = {
                            reportViewModel.loginAsGuest {
                                navController.navigate("MapHomeFragment") {
                                    popUpTo("LoginFragment") { inclusive = true }
                                }
                            }
                        },
                        onNavigateAdminLogin = {
                            navController.navigate("AdminLoginFragment")
                        }
                    )
                }

                // 2. AdminLoginFragment
                composable("AdminLoginFragment") {
                    AdminLoginFragmentScreen(
                        onAdminAuthenticated = { email, pwd ->
                            reportViewModel.loginAsAdmin(email, pwd) {
                                navController.navigate("AdminDashboardFragment") {
                                    popUpTo("LoginFragment") { inclusive = true }
                                }
                            }
                        },
                        onBackToUserLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // 3. MapHomeFragment
                composable("MapHomeFragment") {
                    MapHomeFragmentScreen(
                        uiState = uiState,
                        stations = stations,
                        onSelectStation = { st -> reportViewModel.selectStation(st.stationId) },
                        onOpenStationDetail = { st ->
                            reportViewModel.selectStation(st.stationId)
                            navController.navigate("StationDetailFragment")
                        },
                        onOpenCreateReport = {
                            navController.navigate("CreateReportFragment")
                        },
                        onOpenAdminPanel = {
                            navController.navigate("AdminDashboardFragment")
                        },
                        onSearchChange = reportViewModel::updateSearchQuery,
                        onToggleAccessibleFilter = reportViewModel::toggleAccessibleFilter,
                        onToggleFavoritesFilter = reportViewModel::toggleFavoritesFilter,
                        onToggleOfflineMode = reportViewModel::toggleOfflineMode,
                        onRefreshGps = { reportViewModel.refreshGpsLocation() },
                        onLogout = {
                            navController.navigate("LoginFragment") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // 4. StationDetailFragment
                composable("StationDetailFragment") {
                    if (currentStation != null) {
                        val activeReports = reportViewModel.getActiveReportsForStation(
                            stationId = currentStation.stationId,
                            reports = allReports
                        )
                        StationDetailFragmentScreen(
                            station = currentStation,
                            activeReports = activeReports,
                            onToggleFavorite = { reportViewModel.toggleFavoriteStation(currentStation) },
                            onNavigateCreateReport = { navController.navigate("CreateReportFragment") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                // 5. CreateReportFragment
                composable("CreateReportFragment") {
                    if (currentStation != null) {
                        CreateReportFragmentScreen(
                            station = currentStation,
                            uiState = uiState,
                            onOpenCameraCapture = { navController.navigate("CameraCaptureFragment") },
                            onSimulateDistanceChange = { dist -> reportViewModel.refreshGpsLocation(dist) },
                            onToggleOfflineMode = reportViewModel::toggleOfflineMode,
                            onSubmitReport = { category, desc ->
                                reportViewModel.submitCitizenReport(category, desc) {
                                    navController.popBackStack()
                                }
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }
                }

                // 6. CameraCaptureFragment
                composable("CameraCaptureFragment") {
                    CameraCaptureFragmentScreen(
                        uiState = uiState,
                        onMediaCaptured = { uriStr, isVideo ->
                            reportViewModel.onMediaCaptured(uriStr, isVideo)
                            navController.navigate("MediaPreviewFragment")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // 7. MediaPreviewFragment
                composable("MediaPreviewFragment") {
                    MediaPreviewFragmentScreen(
                        uiState = uiState,
                        onRetryCapture = {
                            reportViewModel.clearCapturedMedia()
                            navController.popBackStack()
                        },
                        onConfirmEvidence = {
                            navController.popBackStack("CreateReportFragment", inclusive = false)
                        }
                    )
                }

                // 8. AdminDashboardFragment
                composable("AdminDashboardFragment") {
                    AdminDashboardFragmentScreen(
                        communityStops = communityStops,
                        reports = allReports,
                        onModerateStop = reportViewModel::moderateCommunityStop,
                        onModerateReport = reportViewModel::moderateReport,
                        onBackToMap = {
                            navController.navigate("MapHomeFragment")
                        }
                    )
                }
            }
        }
    }
}
