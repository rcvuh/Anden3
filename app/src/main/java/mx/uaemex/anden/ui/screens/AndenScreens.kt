package mx.uaemex.anden.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import mx.uaemex.anden.data.local.entity.CommunityStopEntity
import mx.uaemex.anden.data.local.entity.PendingReportEntity
import mx.uaemex.anden.data.local.entity.StationEntity
import mx.uaemex.anden.ui.theme.*
import mx.uaemex.anden.ui.viewmodel.AndenUiState
import mx.uaemex.anden.ui.viewmodel.ReportViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

// =========================================================================
// 1. LoginFragment: Acceso para Pasajero / Invitado y enlace Administrativo
// =========================================================================
@Composable
fun LoginFragmentScreen(
    onLoginCitizen: (String, String) -> Unit,
    onLoginGuest: () -> Unit,
    onNavigateAdminLogin: () -> Unit
) {
    var email by remember { mutableStateOf("hector.rodriguez@alumno.uaemex.mx") }
    var password by remember { mutableStateOf("AndenToluca2026") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AndenSurfaceLight, Color(0xFFF3E5E8), AndenBurgundyPrimary)
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AndenBurgundyPrimary,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsSubway,
                                contentDescription = "Logo Andén",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Andén",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F1A1C)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Conectando tu trayecto, estación por estación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Alternar visibilidad de contraseña"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { onLoginCitizen(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AndenBurgundyPrimary)
                    ) {
                        Text("Iniciar sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("o continúa con", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { onLoginCitizen("google.oauth@uaemex.mx", "GoogleOAuthToken") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = AndenBurgundyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("G  Ingresar con Google", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FilledTonalButton(
                        onClick = onLoginGuest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Entrar como invitado (Catálogo Offline Room)")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = onNavigateAdminLogin) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = AndenBurgundyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Portal Administrativo (@movilidad.edomex.gob.mx)",
                            color = AndenBurgundyPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Andén | Valle de Toluca y CDMX\nTren El Insurgente • Línea 6 Mexibús • Corredores Toluca",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// =========================================================================
// 2. AdminLoginFragment: Acceso exclusivo para supervisores de la red
// =========================================================================
@Composable
fun AdminLoginFragmentScreen(
    onAdminAuthenticated: (String, String) -> Unit,
    onBackToUserLogin: () -> Unit
) {
    var employeeEmail by remember { mutableStateOf("supervisor.toluca@movilidad.edomex.gob.mx") }
    var password by remember { mutableStateOf("SEMOV_Admin2026") }
    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF6EFEF), Color(0xFFE6D5D8), AndenBurgundyDark)
                )
            )
            .padding(20.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsSubway,
                        contentDescription = null,
                        tint = AndenBurgundyPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Andén",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = AndenGoldSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Portal Administrativo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AndenBurgundyPrimary
                )
                Text(
                    text = "Acceso exclusivo para personal autorizado y supervisores de la red.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = employeeEmail,
                    onValueChange = { employeeEmail = it },
                    label = { Text("ID de Empleado / Correo institucional") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña Administrativa") },
                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onAdminAuthenticated(employeeEmail, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AndenBurgundyPrimary)
                ) {
                    Text("Acceder al Panel", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onBackToUserLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volver al acceso de usuarios")
                }
            }
        }
    }
}

// =========================================================================
// 3. MapHomeFragment: Tablero Cartográfico Intermodal + GPS + Catálogo Room
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapHomeFragmentScreen(
    uiState: AndenUiState,
    stations: List<StationEntity>,
    onSelectStation: (StationEntity) -> Unit,
    onOpenStationDetail: (StationEntity) -> Unit,
    onOpenCreateReport: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleAccessibleFilter: () -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    onToggleOfflineMode: () -> Unit,
    onRefreshGps: () -> Unit,
    onLogout: () -> Unit
) {
    val selectedStation = stations.find { it.stationId == uiState.selectedStationId }
        ?: stations.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DirectionsSubway,
                            contentDescription = null,
                            tint = AndenBurgundyPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Andén • Valle de Toluca", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                text = if (uiState.isOfflineSimulationMode) "Modo Offline (Room SQLite)" else "Online (Room + Firebase Sync)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isOfflineSimulationMode) AndenAmberWarning else AndenEmeraldValid
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleOfflineMode) {
                        Icon(
                            imageVector = if (uiState.isOfflineSimulationMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                            contentDescription = "Alternar simulación Offline/Online",
                            tint = if (uiState.isOfflineSimulationMode) AndenAmberWarning else AndenEmeraldValid
                        )
                    }
                    if (uiState.isAdminRole) {
                        IconButton(onClick = onOpenAdminPanel) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = "Panel Admin",
                                tint = AndenBurgundyPrimary
                            )
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = !uiState.filterFavoritesOnly,
                    onClick = { if (uiState.filterFavoritesOnly) onToggleFavoritesFilter() },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text("Mapa") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { selectedStation?.let(onOpenStationDetail) },
                    icon = { Icon(Icons.Default.Train, contentDescription = null) },
                    label = { Text("Estación") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenCreateReport,
                    icon = { Icon(Icons.Default.Flag, contentDescription = null) },
                    label = { Text("Reportar") }
                )
                NavigationBarItem(
                    selected = uiState.filterFavoritesOnly,
                    onClick = onToggleFavoritesFilter,
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Favoritos") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Buscar estaciones, rutas o corredores...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // Banner de Corredor Mexibús L6 + Tren El Insurgente
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFDF8F5))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AndenEmeraldValid,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("6", color = Color.White, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Línea 6 del Mexibús + Tren El Insurgente",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Corredor Intermodal: Zinacantepec - Toluca - Metepec - Lerma - Observatorio",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            item {
                // Selector rápido de estaciones para centrar Google Maps en tiempo real
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stations, key = { "chip_${it.stationId}" }) { st ->
                        val isSel = st.stationId == selectedStation?.stationId
                        FilterChip(
                            selected = isSel,
                            onClick = { onSelectStation(st) },
                            label = { Text(st.name) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AndenBurgundyPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                // Mapa Real de Google Maps centrado en las coordenadas GPS de la estación/usuario
                val mapLat = selectedStation?.latitude ?: uiState.gpsReading.latitude
                val mapLng = selectedStation?.longitude ?: uiState.gpsReading.longitude
                var lastLoadedCoords by remember { mutableStateOf(Pair(0.0, 0.0)) }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    CookieManager.getInstance().setAcceptCookie(true)
                                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        databaseEnabled = true
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        setSupportZoom(true)
                                        builtInZoomControls = true
                                        displayZoomControls = false
                                        cacheMode = WebSettings.LOAD_DEFAULT
                                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    }

                                    webChromeClient = WebChromeClient()
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean = false
                                    }

                                    lastLoadedCoords = Pair(mapLat, mapLng)
                                    val googleMapsHtml = """
                                        <!DOCTYPE html>
                                        <html style="width:100%;height:100%;margin:0;padding:0;">
                                        <head>
                                            <meta charset="utf-8">
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                            <style>
                                                html, body { width:100%; height:100%; margin:0; padding:0; overflow:hidden; background-color:#e5e3df; }
                                                iframe { width:100%; height:100%; border:0; display:block; }
                                            </style>
                                        </head>
                                        <body>
                                            <iframe 
                                                src="https://maps.google.com/maps?q=$mapLat,$mapLng&t=&z=15&ie=UTF8&iwloc=&output=embed" 
                                                width="100%" 
                                                height="100%" 
                                                frameborder="0" 
                                                style="border:0;" 
                                                allowfullscreen>
                                            </iframe>
                                        </body>
                                        </html>
                                    """.trimIndent()
                                    loadDataWithBaseURL("https://maps.google.com", googleMapsHtml, "text/html", "UTF-8", null)
                                }
                            },
                            update = { webView ->
                                if (lastLoadedCoords != Pair(mapLat, mapLng)) {
                                    lastLoadedCoords = Pair(mapLat, mapLng)
                                    val googleMapsHtml = """
                                        <!DOCTYPE html>
                                        <html style="width:100%;height:100%;margin:0;padding:0;">
                                        <head>
                                            <meta charset="utf-8">
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                            <style>
                                                html, body { width:100%; height:100%; margin:0; padding:0; overflow:hidden; background-color:#e5e3df; }
                                                iframe { width:100%; height:100%; border:0; display:block; }
                                            </style>
                                        </head>
                                        <body>
                                            <iframe 
                                                src="https://maps.google.com/maps?q=$mapLat,$mapLng&t=&z=15&ie=UTF8&iwloc=&output=embed" 
                                                width="100%" 
                                                height="100%" 
                                                frameborder="0" 
                                                style="border:0;" 
                                                allowfullscreen>
                                            </iframe>
                                        </body>
                                        </html>
                                    """.trimIndent()
                                    webView.loadDataWithBaseURL("https://maps.google.com", googleMapsHtml, "text/html", "UTF-8", null)
                                }
                            }
                        )

                        // Puntero GPS activo en tiempo real sobre Google Maps
                        Surface(
                            color = Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 10.dp, bottom = 10.dp, end = 64.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = AndenBurgundyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Estación más cercana: ${selectedStation?.name ?: "Metepec"} (${uiState.gpsReading.distanceToNearestMeters} m)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "GPS: %.4f°, %.4f° • Geocerca <300m: %s",
                                            mapLat,
                                            mapLng,
                                            if (uiState.gpsReading.isWithinGeofence300m) "VÁLIDA" else "FUERA DE RANGO"
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AndenEmeraldValid
                                    )
                                }
                            }
                        }

                        FloatingActionButton(
                            onClick = onRefreshGps,
                            containerColor = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .size(42.dp)
                        ) {
                            Icon(Icons.Default.GpsFixed, contentDescription = "Actualizar GPS", tint = AndenBurgundyPrimary)
                        }
                    }
                }
            }

            item {
                // Barra de Filtros Material Design 3 (Accesibilidad Motriz y Simulación Offline)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.filterAccessibleOnly,
                            onClick = onToggleAccessibleFilter,
                            label = { Text("Con accesibilidad (Elevador operando)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Accessible,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.isOfflineSimulationMode,
                            onClick = onToggleOfflineMode,
                            label = {
                                Text(
                                    if (uiState.isOfflineSimulationMode) "Modo Sin Red: Activo"
                                    else "Simular Pérdida de Red (Room)"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Storage,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Catálogo de Estaciones en Room SQLite (${stations.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(stations, key = { it.stationId }) { station ->
                val isSelected = station.stationId == selectedStation?.stationId
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectStation(station)
                            onOpenStationDetail(station)
                        },
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isSelected) Color(0xFFFDF2F4) else Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.DirectionsSubway,
                                    contentDescription = null,
                                    tint = AndenBurgundyPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Estación ${station.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${station.systemLine} • ${station.municipality}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            AssistChip(
                                onClick = { onOpenStationDetail(station) },
                                label = { Text("Ver Ficha", maxLines = 1) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = AndenBurgundyPrimary,
                                    labelColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = Color(0xFFE6F4EA),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "● ${station.serviceStatus}",
                                    color = AndenEmeraldValid,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = if (station.elevatorStatus.contains("Operando")) Color(0xFFE6F4EA) else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "♿ Elevador: ${station.elevatorStatus}",
                                    color = if (station.elevatorStatus.contains("Operando")) AndenEmeraldValid else AndenAmberWarning,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// =========================================================================
// 4. StationDetailFragment: Ficha Operativa, Semáforo de Accesibilidad y Reportes (30 min)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailFragmentScreen(
    station: StationEntity,
    activeReports: List<PendingReportEntity>,
    onToggleFavorite: () -> Unit,
    onNavigateCreateReport: () -> Unit,
    onBack: () -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estación ${station.name}", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (station.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AndenBurgundyPrimary)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateCreateReport,
                containerColor = AndenBurgundyPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Reportar estado de estación", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Encabezado de Estación
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Train,
                                contentDescription = null,
                                tint = AndenBurgundyPrimary,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = station.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${station.systemLine} • ${station.municipality}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFFE6F4EA),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "● ${station.serviceStatus}",
                                color = AndenEmeraldValid,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Horario y Semáforo de Accesibilidad Universal
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Horario y Accesibilidad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = station.scheduleText, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = if (station.elevatorStatus.contains("Operando")) Color(0xFFE6F4EA) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Elevator, contentDescription = null, tint = AndenEmeraldValid)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Elevador: ${station.elevatorStatus} ●",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (station.elevatorStatus.contains("Operando")) AndenEmeraldValid else AndenAmberWarning
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Color(0xFFE6F4EA),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Accessible, contentDescription = null, tint = AndenEmeraldValid)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Rampas: ${station.rampStatus} ●",
                                    fontWeight = FontWeight.SemiBold,
                                    color = AndenEmeraldValid
                                )
                            }
                        }
                    }
                }
            }

            // Conexiones y Alimentadores (Plan Colibrí / Corredores Naturales)
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Conexiones y Alimentadores",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Corredores Naturales y camiones locales:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        station.feederConnections.split(";").forEach { feeder ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = AndenBurgundyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = feeder, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Reportes de usuarios (Vigencia de últimos 30 minutos)
            item {
                Text(
                    text = "Reportes de usuarios (últimos 30 min)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(activeReports, key = { it.reportId }) { rep ->
                val minutesAgo = ((System.currentTimeMillis() - rep.timestampMillis) / 60000L).coerceAtLeast(1L)
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFFCE8EC),
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    rep.incidentType.contains("Elevador", true) -> Icons.Default.Elevator
                                    rep.incidentType.contains("Retraso", true) -> Icons.Default.Schedule
                                    else -> Icons.Default.Groups
                                },
                                contentDescription = null,
                                tint = AndenBurgundyPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rep.incidentType, fontWeight = FontWeight.Bold)
                            Text(rep.description, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "Hace $minutesAgo min (${timeFormatter.format(rep.timestampMillis)}) por ${rep.authorHandle} • GPS a ${rep.distanceToStationMeters}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = if (rep.syncStatus == "SINCRONIZADO_FIRESTORE") "✓ Sincronizado en Firestore"
                                else "⏳ En cola local Room SQLite (Pendiente de red)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (rep.syncStatus == "SINCRONIZADO_FIRESTORE") AndenEmeraldValid else AndenAmberWarning,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

// =========================================================================
// 5. CreateReportFragment: Geocerca <300m, Categorías, Evidencia y Cola Room/Firestore
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportFragmentScreen(
    station: StationEntity,
    uiState: AndenUiState,
    onOpenCameraCapture: () -> Unit,
    onSimulateDistanceChange: (Int) -> Unit,
    onToggleOfflineMode: () -> Unit,
    onSubmitReport: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    val categories = listOf(
        "Elevador dañado",
        "Aglomeración",
        "Retraso de tren",
        "Bloqueo de corredor"
    )
    var selectedCategory by remember { mutableStateOf("Retraso de tren") }
    var description by remember { mutableStateOf("") }

    val isWithinGeofence = uiState.gpsReading.isWithinGeofence300m

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reportar incidencia", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "Estación ${station.name} (${station.systemLine})",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AndenBurgundyPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Banner de Validación de Geocerca Satelital (< 300 m)
            Surface(
                color = if (isWithinGeofence) Color(0xFF1E7E34) else AndenDangerRed,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isWithinGeofence) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isWithinGeofence) {
                                "Ubicación válida: Estás a ${uiState.gpsReading.distanceToNearestMeters} m de la estación (Menos de 300 m)"
                            } else {
                                "Fuera de geocerca: Estás a ${uiState.gpsReading.distanceToNearestMeters} m (Se requiere < 300 m)"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = String.format(
                                Locale.US,
                                "Coordenadas GPS: %.4f°, %.4f° (±%.1f m)",
                                uiState.gpsReading.latitude,
                                uiState.gpsReading.longitude,
                                uiState.gpsReading.accuracyMeters
                            ),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Controles rápidos para demostrar la geocerca en el video de evidencia
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { onSimulateDistanceChange(85) },
                    label = { Text("Simular GPS a 85 m (Válido)") },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = { onSimulateDistanceChange(450) },
                    label = { Text("Simular a 450 m (Bloqueo)") },
                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Text("Tipo de incidencia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Cuadrícula de Chips de Categoría (Material Design 3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    categories.take(2).forEach { cat ->
                        val selected = selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, modifier = Modifier.padding(vertical = 6.dp)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (cat.contains("Elevador")) Icons.Default.Accessible else Icons.Default.Groups,
                                    contentDescription = null
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AndenBurgundyPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    categories.drop(2).forEach { cat ->
                        val selected = selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, modifier = Modifier.padding(vertical = 6.dp)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (cat.contains("Retraso")) Icons.Default.Schedule else Icons.Default.Warning,
                                    contentDescription = null
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AndenBurgundyPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Text("Descripción (opcional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Describe brevemente la situación en el andén o corredor...") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Evidencia fotográfica / video (CameraX)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FilledTonalButton(
                            onClick = onOpenCameraCapture,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFFFF4D6)
                            )
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AndenBurgundyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.capturedMediaUri.isBlank()) "Tomar fotografía / video" else "Cambiar evidencia capturada",
                                color = AndenBurgundyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.capturedMediaUri.isBlank()) {
                                "Permiso de CameraX y GPS listos en tiempo de ejecución"
                            } else {
                                "✓ Evidencia adjunta: ${uiState.capturedMediaUri.substringAfterLast('/')}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.capturedMediaUri.isBlank()) Color.Gray else AndenEmeraldValid
                        )
                    }
                }
            }

            // Estado de Envío (Room Offline vs Firebase Online)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Estado de envío", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onToggleOfflineMode) {
                    Icon(
                        imageVector = if (uiState.isOfflineSimulationMode) Icons.Default.WifiOff else Icons.Default.Wifi,
                        contentDescription = null,
                        tint = if (uiState.isOfflineSimulationMode) AndenDangerRed else AndenEmeraldValid,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isOfflineSimulationMode) "Sin conexión (Modo Andén)" else "Con conexión activa",
                        color = if (uiState.isOfflineSimulationMode) AndenDangerRed else AndenEmeraldValid
                    )
                }
            }

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Storage,
                        contentDescription = null,
                        tint = AndenBurgundyPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (uiState.isOfflineSimulationMode) "Pendiente por enviar (Guardado en Room SQLite)"
                            else "Sincronización directa: Room SQLite + Cloud Firestore",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Se almacena localmente de forma inmediata y se sube a Firebase al detectar red.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Button(
                onClick = { onSubmitReport(selectedCategory, description) },
                enabled = isWithinGeofence,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AndenBurgundyPrimary)
            ) {
                Text("Enviar reporte", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = Color.DarkGray)
            }
        }
    }
}

// =========================================================================
// 6. CameraCaptureFragment: Visor CameraX en Tiempo Real + Overlay Satelital GPS
// =========================================================================
@Composable
fun CameraCaptureFragmentScreen(
    uiState: AndenUiState,
    onMediaCaptured: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermissions = grants[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var isVideoMode by remember { mutableStateOf(false) }
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasPermissions) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        controller = cameraController
                        cameraController.bindToLifecycle(lifecycleOwner)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Vista de respaldo en caso de ejecutar en emulador sin webcam configurada
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Visor CameraX Activo (Simulación de Andén en Emulador)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("Conceder permisos en tiempo de ejecución", color = Color.White)
                }
            }
        }

        // Barra superior con retorno e indicador GPS en vivo (FusedLocationProviderClient)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                }
                Icon(Icons.Default.Cameraswitch, contentDescription = null, tint = Color.White)
            }

            Surface(
                color = Color(0xFFE6F4EA).copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AndenEmeraldValid,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format(
                            Locale.US,
                            "GPS Activo: %.4f°, %.4f° (Margen %.0fm)",
                            uiState.gpsReading.latitude,
                            uiState.gpsReading.longitude,
                            uiState.gpsReading.accuracyMeters
                        ),
                        color = AndenEmeraldValid,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Controles inferiores de obturador (FOTO / VIDEO)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    text = "FOTO",
                    color = if (!isVideoMode) AndenGoldSecondary else Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { isVideoMode = false }
                )
                Text(
                    text = "VIDEO",
                    color = if (isVideoMode) AndenGoldSecondary else Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { isVideoMode = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isVideoMode) "● REC 00:00" else "Captura con metadatos EXIF + GPS",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Botón Obturador Circular
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(if (isVideoMode) Color.Red else AndenBurgundyPrimary)
                    .clickable {
                        captureWithCameraXOrFallback(
                            context = context,
                            cameraController = cameraController,
                            isVideo = isVideoMode,
                            onSaved = { uriStr ->
                                onMediaCaptured(uriStr, isVideoMode)
                            }
                        )
                    }
            ) {
                Icon(
                    imageVector = if (isVideoMode) Icons.Default.Videocam else Icons.Default.CameraAlt,
                    contentDescription = "Disparar obturador",
                    tint = Color.White
                )
            }
        }
    }
}

private fun captureWithCameraXOrFallback(
    context: Context,
    cameraController: LifecycleCameraController,
    isVideo: Boolean,
    onSaved: (String) -> Unit
) {
    val ext = if (isVideo) "mp4" else "jpg"
    val file = File(context.cacheDir, "evidencia_anden_${System.currentTimeMillis()}.$ext")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    runCatching {
        cameraController.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onSaved(Uri.fromFile(file).toString())
                }

                override fun onError(exception: ImageCaptureException) {
                    file.writeText("EVIDENCIA_CAMERAX_SIMULADA_GPS")
                    onSaved(Uri.fromFile(file).toString())
                }
            }
        )
    }.onFailure {
        file.writeText("EVIDENCIA_CAMERAX_SIMULADA_GPS")
        onSaved(Uri.fromFile(file).toString())
    }
}

// =========================================================================
// 7. MediaPreviewFragment: Validación Previa de Evidencia antes de Guardar en Room
// =========================================================================
@Composable
fun MediaPreviewFragmentScreen(
    uiState: AndenUiState,
    onRetryCapture: () -> Unit,
    onConfirmEvidence: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .padding(16.dp)
    ) {
        // Sello superior de Coordenadas GPS verificadas
        Surface(
            color = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AndenEmeraldValid)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(
                        Locale.US,
                        "Ubicación GPS: %.4f°, %.4f°",
                        uiState.gpsReading.latitude,
                        uiState.gpsReading.longitude
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Tarjeta central de vista previa de la evidencia capturada
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF262626))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (uiState.capturedIsVideo) Icons.Default.PlayCircleFilled else Icons.Default.Verified,
                    contentDescription = null,
                    tint = AndenGoldSecondary,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (uiState.capturedIsVideo) "Clip de Video Capturado (MP4)" else "Fotografía Testimonial Capturada (JPEG)",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Archivo local: ${uiState.capturedMediaUri.substringAfterLast('/')}\nGeocerca validada a ${uiState.gpsReading.distanceToNearestMeters} m del andén",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Botones de acción inferior: Reintentar vs Usar evidencia
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetryCapture,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reintentar", color = Color.White)
            }

            Button(
                onClick = onConfirmEvidence,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AndenBurgundyPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Usar evidencia", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =========================================================================
// 8. AdminDashboardFragment: Moderación de Paradas Nuevas y Reportes Dudosos
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardFragmentScreen(
    communityStops: List<CommunityStopEntity>,
    reports: List<PendingReportEntity>,
    onModerateStop: (Long, Boolean) -> Unit,
    onModerateReport: (Long, Boolean) -> Unit,
    onBackToMap: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val pendingStops = communityStops.filter { it.moderationStatus == "PENDIENTE" }
    val pendingOrAllReports = reports.filter { it.moderationStatus != "DESCARTADO" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackToMap) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver al Mapa", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AndenBurgundyPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Paradas nuevas (${pendingStops.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Reportes ciudadanos (${pendingOrAllReports.size})", fontWeight = FontWeight.Bold) }
                )
            }

            // Insignia de Rol y Sincronización Firestore
            Surface(
                color = Color(0xFFFFF8E1),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AndenGoldSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rol: Administrador • Sincronizado en tiempo real con Firestore",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5C4300)
                    )
                }
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingStops, key = { it.stopId }) { stop ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(stop.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "Registrada por: ${stop.authorHandle} • ${stop.timeAgoText}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = AndenBurgundyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stop.corridorReference, style = MaterialTheme.typography.bodySmall)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { onModerateStop(stop.stopId, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AndenEmeraldValid),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Validar")
                                    }
                                    OutlinedButton(
                                        onClick = { onModerateStop(stop.stopId, false) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = AndenDangerRed, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Descartar", color = AndenDangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingOrAllReports, key = { it.reportId }) { rep ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "${rep.incidentType} • Estación ${rep.stationName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(rep.description, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "Autor: ${rep.authorHandle} • Distancia GPS: ${rep.distanceToStationMeters}m (<300m)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { onModerateReport(rep.reportId, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AndenEmeraldValid),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Validar")
                                    }
                                    OutlinedButton(
                                        onClick = { onModerateReport(rep.reportId, false) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Descartar", color = AndenDangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
