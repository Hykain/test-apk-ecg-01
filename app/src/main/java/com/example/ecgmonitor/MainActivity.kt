package com.example.ecgmonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ecgmonitor.ble.BleEcgClient
import com.example.ecgmonitor.data.EcgDatabase
import com.example.ecgmonitor.data.EcgSession
import com.example.ecgmonitor.ui.components.BleDeviceSheet
import com.example.ecgmonitor.ui.screens.ChartsScreen
import com.example.ecgmonitor.ui.screens.HomeScreen
import com.example.ecgmonitor.ui.screens.SettingsScreen
import com.example.ecgmonitor.ui.screens.TrendsScreen
import com.example.ecgmonitor.ui.theme.EcgTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val database by lazy { EcgDatabase.create(this) }
    private val vm by viewModels<EcgViewModel> {
        EcgViewModel.Factory(this, BleEcgClient(this), database.sessionDao())
    }

    private val permissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) vm.scanDevices()
    }

    override fun onCreate(state: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(state)
        setContent {
            EcgApp(vm, ::scan)
        }
        scan()
    }

    private fun scan() {
        val wanted = if (Build.VERSION.SDK_INT >= 31) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (wanted.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            permissions.launch(wanted)
        } else {
            vm.scanDevices()
        }
    }
}

private data class NavigationItem(
    val titleEn: String,
    val titleVi: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EcgApp(vm: EcgViewModel, scan: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Waveform & Telemetry States
    val raw by vm.rawWaveform.collectAsState()
    val espFiltered by vm.espFilteredWaveform.collectAsState()
    val androidFiltered by vm.androidFilteredWaveform.collectAsState()

    val status by vm.status.collectAsState()
    val isConnected by vm.isConnected.collectAsState()
    val isScanning by vm.isScanning.collectAsState()
    val assessment by vm.assessment.collectAsState()
    val sessions by vm.sessions.collectAsState()
    val devices by vm.devices.collectAsState()
    val selected by vm.selectedDevice.collectAsState()
    val samples by vm.sampleCount.collectAsState()

    val spo2 by vm.spo2.collectAsState()
    val ppgBpm by vm.ppgBpm.collectAsState()
    val activeAiClassName by vm.activeAiClassName.collectAsState()
    val activeAiClassPercentages by vm.activeAiClassPercentages.collectAsState()
    val isPeak by vm.isPeak.collectAsState()
    val gifAsset by vm.currentGifAsset.collectAsState()

    // Dual AI Source and TFLite Model State
    val aiSource by vm.aiSource.collectAsState()
    val availableModels by vm.availableModels.collectAsState()
    val selectedModel by vm.selectedModel.collectAsState()
    val isDemoMode by vm.isDemoMode.collectAsState()
    val onDeviceConfidence by vm.onDeviceConfidence.collectAsState()
    val onDeviceInferenceTimeMs by vm.onDeviceInferenceTimeMs.collectAsState()

    // Navigation & Appearance State
    var page by rememberSaveable { mutableIntStateOf(0) }
    val systemInDark = isSystemInDarkTheme()
    var dark by rememberSaveable { mutableStateOf(systemInDark) }
    var english by rememberSaveable { mutableStateOf(false) }
    var showConnectSheet by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // State for SAF CSV Export
    var pendingExportSession by remember { mutableStateOf<EcgSession?>(null) }
    var isExportingCurrentSession by remember { mutableStateOf(false) }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        if (isExportingCurrentSession) {
                            vm.exportCurrentSessionToCsv(stream)
                        } else {
                            pendingExportSession?.let { s -> vm.exportSessionToCsv(s, stream) }
                        }
                    }
                    snackbarHostState.showSnackbar(
                        if (english) "CSV exported successfully!" else "Đã xuất tệp CSV thành công!"
                    )
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(
                        if (english) "Failed to export CSV: ${e.localizedMessage}" else "Lỗi khi xuất CSV: ${e.localizedMessage}"
                    )
                } finally {
                    pendingExportSession = null
                    isExportingCurrentSession = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.connectionEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Material 3 Navigation Destinations
    val navItems = listOf(
        NavigationItem(
            titleEn = "Home",
            titleVi = "Trang chủ",
            selectedIcon = Icons.Filled.MonitorHeart,
            unselectedIcon = Icons.Outlined.MonitorHeart
        ),
        NavigationItem(
            titleEn = "ECG Charts",
            titleVi = "Đồ thị ECG",
            selectedIcon = Icons.Filled.ShowChart,
            unselectedIcon = Icons.Outlined.ShowChart
        ),
        NavigationItem(
            titleEn = "Trends",
            titleVi = "Xu hướng",
            selectedIcon = Icons.Filled.TrendingUp,
            unselectedIcon = Icons.Outlined.TrendingUp
        ),
        NavigationItem(
            titleEn = "Settings",
            titleVi = "Cài đặt",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    // Pulsing heart animation for peak heartbeat indicator in TopAppBar
    val heartScale by animateFloatAsState(
        targetValue = if (isPeak) 1.25f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "topBarHeartScale"
    )

    EcgTheme(
        darkTheme = dark,
        dynamicColor = true // Material You dynamic colors on Android 12+
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (english) "ECG Companion" else "ECG Sinh Hiệu AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (isPeak) {
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Heartbeat",
                                    tint = EcgTheme.colors.peakMarker,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .scale(heartScale)
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick Demo Toggle Chip
                        FilterChip(
                            selected = isDemoMode,
                            onClick = vm::toggleDemoMode,
                            label = {
                                Text(
                                    text = if (isDemoMode) (if (english) "Demo: ON" else "Demo: BẬT")
                                    else (if (english) "Demo" else "Chạy thử"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        )

                        // Bluetooth Status Quick Action Button
                        FilledTonalButton(
                            onClick = {
                                scan()
                                showConnectSheet = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isConnected || isDemoMode)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.errorContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .defaultMinSize(minHeight = 36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isConnected || isDemoMode)
                                            EcgTheme.colors.rhythmNormal
                                        else
                                            MaterialTheme.colorScheme.error,
                                        CircleShape
                                    )
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isDemoMode) (if (english) "Demo" else "Mô phỏng")
                                else if (isConnected) (selected?.name ?: if (english) "Connected" else "Đã kết nối")
                                else (if (english) "Connect" else "Kết nối"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected || isDemoMode)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp
                ) {
                    navItems.forEachIndexed { index, item ->
                        val selectedItem = page == index
                        NavigationBarItem(
                            selected = selectedItem,
                            onClick = { page = index },
                            icon = {
                                Icon(
                                    imageVector = if (selectedItem) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = if (english) item.titleEn else item.titleVi
                                )
                            },
                            label = {
                                Text(
                                    text = if (english) item.titleEn else item.titleVi,
                                    fontWeight = if (selectedItem) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (page) {
                    0 -> HomeScreen(
                        assessment = assessment,
                        spo2 = spo2,
                        ppgBpm = ppgBpm,
                        activeAiClassName = activeAiClassName,
                        activeAiClassPercentages = activeAiClassPercentages,
                        aiSource = aiSource,
                        onSelectAiSource = vm::setAiSource,
                        availableModels = availableModels,
                        selectedModel = selectedModel,
                        onSelectModel = vm::selectModel,
                        onDeviceConfidence = onDeviceConfidence,
                        onDeviceInferenceTimeMs = onDeviceInferenceTimeMs,
                        samples = samples,
                        gifAsset = gifAsset,
                        isPeak = isPeak,
                        isConnected = isConnected || isDemoMode,
                        isDemoMode = isDemoMode,
                        onToggleDemo = vm::toggleDemoMode,
                        onOpenConnect = {
                            scan()
                            showConnectSheet = true
                        },
                        english = english
                    )
                    1 -> ChartsScreen(
                        raw = raw,
                        espFiltered = espFiltered,
                        androidFiltered = androidFiltered,
                        status = if (isDemoMode) (if (english) "125Hz Simulated ECG Stream" else "Chế độ mô phỏng Demo ECG 125Hz") else status,
                        assessment = assessment,
                        spo2 = spo2,
                        ppgBpm = ppgBpm,
                        aiClassName = activeAiClassName,
                        selected = selected,
                        isConnected = isConnected || isDemoMode,
                        samples = samples,
                        onOpenConnect = {
                            scan()
                            showConnectSheet = true
                        },
                        disconnect = vm::disconnect,
                        save = vm::saveCurrentMeasurement,
                        onExportCurrentCsv = {
                            isExportingCurrentSession = true
                            pendingExportSession = null
                            exportCsvLauncher.launch(vm.generateCsvFilename())
                        },
                        english = english
                    )
                    2 -> TrendsScreen(
                        sessions = sessions,
                        onExportSessionCsv = { session ->
                            isExportingCurrentSession = false
                            pendingExportSession = session
                            exportCsvLauncher.launch(vm.generateCsvFilename(session.startedAt))
                        },
                        english = english
                    )
                    else -> SettingsScreen(
                        dark = dark,
                        onDark = { dark = it },
                        english = english,
                        onEnglish = { english = it },
                        isDemoMode = isDemoMode,
                        onToggleDemo = vm::toggleDemoMode,
                        selectedModel = selectedModel,
                        availableModels = availableModels,
                        onSelectModel = vm::selectModel
                    )
                }
            }

            if (showConnectSheet) {
                BleDeviceSheet(
                    devices = devices,
                    selected = selected,
                    isConnected = isConnected,
                    isScanning = isScanning,
                    status = status,
                    onDismiss = { showConnectSheet = false },
                    onScan = scan,
                    onConnect = { dev ->
                        vm.connectTo(dev)
                        showConnectSheet = false
                    },
                    onDisconnect = vm::disconnect,
                    english = english
                )
            }
        }
    }
}
