package com.hamlog.ui.screen

import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.hamlog.ui.theme.LocalWindowSizeClass
import com.hamlog.ui.theme.NotoSerif
import com.hamlog.AppPreferences
import com.hamlog.util.GridCalculator
import com.hamlog.util.EquipmentManager
import com.hamlog.R
import com.hamlog.viewmodel.SettingsViewModel
import androidx.core.content.ContextCompat
import java.time.ZoneId
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import android.widget.Toast
import com.hamlog.util.CloudlogSync

@Composable
fun <T> DragReorderableColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    onMove: (Int, Int) -> Unit,
    itemContent: @Composable (T, Int) -> Unit
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var draggedItemStartIndex by remember { mutableStateOf(0) }
    var dragOffset by remember { mutableStateOf(0f) }
    val itemHeight = 36.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    val targetIndex = if (draggedIndex != null) {
        (draggedItemStartIndex + (dragOffset / itemHeightPx).roundToInt()).coerceIn(0, items.lastIndex)
    } else -1

    Column(modifier) {
        items.forEachIndexed { index, item ->
            val isDragging = draggedIndex == index
            val isTargetSlot = !isDragging && draggedIndex != null && index == targetIndex
            Box(
                Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffset else 0f
                        alpha = if (isDragging) 0.9f else 1f
                        scaleX = if (isDragging) 1.03f else 1f
                        scaleY = if (isDragging) 1.03f else 1f
                    }
                    .pointerInput(item) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedIndex = index
                                draggedItemStartIndex = index
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                val target = (dragOffset / itemHeightPx).roundToInt()
                                val newIndex = (draggedItemStartIndex + target).coerceIn(0, items.lastIndex)
                                if (newIndex != draggedItemStartIndex) {
                                    onMove(draggedItemStartIndex, newIndex)
                                }
                                draggedIndex = null
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggedIndex = null
                                dragOffset = 0f
                            },
                            onDrag = { change, offset ->
                                change.consume()
                                dragOffset += offset.y
                            }
                        )
                    }
            ) {
                Column {
                    if (isTargetSlot && targetIndex < draggedItemStartIndex) {
                        // Show gap above target
                        Box(Modifier.fillMaxWidth().height(itemHeight).padding(horizontal=4.dp).graphicsLayer { alpha = 0.3f }) {
                            Surface(Modifier.fillMaxSize(), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary) {}
                        }
                    }
                    itemContent(item, index)
                    if (isTargetSlot && targetIndex > draggedItemStartIndex) {
                        // Show gap below target
                        Box(Modifier.fillMaxWidth().height(itemHeight).padding(horizontal=4.dp).graphicsLayer { alpha = 0.3f }) {
                            Surface(Modifier.fillMaxSize(), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary) {}
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val widthClass = LocalWindowSizeClass.current
    val hPadding = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 40.dp
        WindowWidthSizeClass.Medium -> 24.dp
        else -> 16.dp
    }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val userCallsign by AppPreferences.callsign.collectAsState()
    var callsignText by remember { mutableStateOf(userCallsign) }
    val userOpName by AppPreferences.opName.collectAsState()
    var opNameText by remember { mutableStateOf(userOpName) }
    val userEquipment by AppPreferences.equipment.collectAsState()
    var equipmentText by remember { mutableStateOf(userEquipment) }
    val userLocation by AppPreferences.location.collectAsState()
    var locationText by remember { mutableStateOf(userLocation) }
    val userGrid by AppPreferences.gridSquare.collectAsState()
    var gridText by remember { mutableStateOf(userGrid) }

    LaunchedEffect(userCallsign) { callsignText = userCallsign }
    LaunchedEffect(userOpName) { opNameText = userOpName }
    LaunchedEffect(userEquipment) { equipmentText = userEquipment }
    LaunchedEffect(userLocation) { locationText = userLocation }
    LaunchedEffect(userGrid) { gridText = userGrid }


    // Cloudlog state
    val cloudlogUrlPref by AppPreferences.cloudlogUrl.collectAsState()
    var cloudlogUrl by remember { mutableStateOf(cloudlogUrlPref) }
    LaunchedEffect(cloudlogUrlPref) { cloudlogUrl = cloudlogUrlPref }
    val cloudlogApiKeyPref by AppPreferences.cloudlogApiKey.collectAsState()
    var cloudlogApiKey by remember { mutableStateOf(cloudlogApiKeyPref) }
    LaunchedEffect(cloudlogApiKeyPref) { cloudlogApiKey = cloudlogApiKeyPref }
    var cloudlogKeyFocused by remember { mutableStateOf(false) }
    val stationProfileIdPref by AppPreferences.stationProfileId.collectAsState()
    var stationProfileId by remember { mutableStateOf(stationProfileIdPref) }
    LaunchedEffect(stationProfileIdPref) { stationProfileId = stationProfileIdPref }
    val autoUploadPref by AppPreferences.autoUploadEnabled.collectAsState()
    var autoUploadEnabled by remember { mutableStateOf(autoUploadPref) }
    LaunchedEffect(autoUploadPref) { autoUploadEnabled = autoUploadPref }
    var isSyncing by remember { mutableStateOf(false) }
    var isTestingConn by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<com.hamlog.util.SyncResult?>(null) }
    var showSyncResult by remember { mutableStateOf(false) }
    var syncProgress by remember { mutableStateOf(0 to 0) }

    LaunchedEffect(uiState.exportComplete, uiState.exportUri) {
        if (uiState.exportComplete && uiState.exportUri != null) {
            context.startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uiState.exportUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, "\u5bfc\u51fa ADIF \u65e5\u5fd7"))
            viewModel.resetExportState()
        }
    }

    val timezoneOptions = listOf(
        "UTC" to "UTC",
        "Asia/Shanghai" to "UTC+8 \u5317\u4eac",
        "Asia/Tokyo" to "UTC+9 \u4e1c\u4eac",
        "Asia/Singapore" to "UTC+8 \u65b0\u52a0\u5761",
        "Europe/London" to "UTC+0 \u4f26\u6566",
        "Europe/Berlin" to "UTC+1 \u67cf\u6797",
        "America/New_York" to "UTC-5 \u7ebd\u7ea6",
        "America/Los_Angeles" to "UTC-8 \u6d1b\u6749\u77f6"
    )
    var tzExpanded by remember { mutableStateOf(false) }
    var showAntennaAdd by remember { mutableStateOf(false) }
    var showRigAdd by remember { mutableStateOf(false) }
    var newAntennaText by remember { mutableStateOf("") }
    var newRigModelText by remember { mutableStateOf("") }
    var newRigBrandText by remember { mutableStateOf("") }
    val antennaList = remember { mutableStateListOf<String>().apply { addAll(EquipmentManager.getAntennas()) } }
    val rigList = remember { mutableStateListOf<EquipmentManager.EquipmentCategory>().apply { addAll(EquipmentManager.getRigs()) } }
    fun refreshEquipment() { antennaList.clear(); antennaList.addAll(EquipmentManager.getAntennas()); rigList.clear(); rigList.addAll(EquipmentManager.getRigs()) }

    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<com.hamlog.util.UpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    val updateScope = rememberCoroutineScope()
    val adifPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importAdif(it) }
    }

    val locationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? LocationManager
            try {
                val location = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    val grid = GridCalculator.latLngToGrid(lat, lng)
                    gridText = grid
                    AppPreferences.setGridSquare(grid)
                    val pos = try {
                        val geocoder = android.location.Geocoder(context)
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            listOfNotNull(addr.adminArea, addr.locality, addr.subLocality, addr.thoroughfare)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                        } else {
                            grid
                        }
                    } catch (_: Exception) {
                        grid
                    }
                    locationText = pos
                    AppPreferences.setLocation(pos)
                    
                }
            } catch (_: Exception) {}
        }
    }

    uiState.importResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportResult() },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("\u5bfc\u5165\u5b8c\u6210", fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "\u6210\u529f\u5bfc\u5165 ${result.imported} \u6761\uff0c\u8df3\u8fc7 ${result.skipped} \u6761",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissImportResult() }) {
                    Text("\u786e\u5b9a")
                }
            }
        )
    }

    if (showAntennaAdd) AlertDialog(onDismissRequest={showAntennaAdd=false},shape=MaterialTheme.shapes.large,containerColor=MaterialTheme.colorScheme.surface,title={Text("添加天馈",fontWeight=FontWeight.SemiBold)},text={OutlinedTextField(value=newAntennaText,onValueChange={newAntennaText=it},modifier=Modifier.fillMaxWidth(),singleLine=true,textStyle=MaterialTheme.typography.bodySmall,shape=RoundedCornerShape(8.dp))},confirmButton={TextButton(onClick={EquipmentManager.addAntenna(newAntennaText);refreshEquipment();newAntennaText="";showAntennaAdd=false}){Text("确定")}},dismissButton={TextButton(onClick={showAntennaAdd=false;newAntennaText=""}){Text("取消")}})
    if (showRigAdd) AlertDialog(onDismissRequest={showRigAdd=false},shape=MaterialTheme.shapes.large,containerColor=MaterialTheme.colorScheme.surface,title={Text("添加设备",fontWeight=FontWeight.SemiBold)},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(value=newRigBrandText,onValueChange={newRigBrandText=it},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("品牌")},textStyle=MaterialTheme.typography.bodySmall,shape=RoundedCornerShape(8.dp));OutlinedTextField(value=newRigModelText,onValueChange={newRigModelText=it},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("型号")},textStyle=MaterialTheme.typography.bodySmall,shape=RoundedCornerShape(8.dp))}},confirmButton={TextButton(onClick={val b=newRigBrandText.trim();val m=newRigModelText.trim();if(b.isNotBlank()){EquipmentManager.addRigBrand(b);if(m.isNotBlank())EquipmentManager.addRigModel(b,m)};refreshEquipment();newRigBrandText="";newRigModelText="";showRigAdd=false}){Text("确定")}},dismissButton={TextButton(onClick={showRigAdd=false;newRigBrandText="";newRigModelText=""}){Text("取消")}})

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "\u8bbe\u7f6e",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = hPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Callsign
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("\u547c\u53f7", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "\u8bbe\u7f6e\u540e\u4e3b\u9875\u663e\u793a\u300cXXX \u7684\u901a\u8054\u65e5\u5fd7\u300d",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = callsignText,
                            onValueChange = { val v = it.uppercase(); callsignText = v; AppPreferences.setCallsign(v.trim()) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerif, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            shape = MaterialTheme.shapes.small,
                            colors = hamFieldColors()
                        )
                    }
                }

                // Operator Info
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("\u59d3\u540d", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = opNameText,
                            onValueChange = { opNameText = it; AppPreferences.setOpName(it) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(8.dp),
                            colors = hamFieldColors()
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Computer, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("\u8bbe\u5907", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = equipmentText,
                            onValueChange = { equipmentText = it; AppPreferences.setEquipment(it) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(8.dp),
                            colors = hamFieldColors()
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("\u4f4d\u7f6e", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = locationText,
                            onValueChange = { locationText = it; AppPreferences.setLocation(it) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(8.dp),
                            colors = hamFieldColors(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? LocationManager
                                        try {
                                            val location = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                                ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                                if (location != null) {
                                                    val lat = location.latitude
                                                    val lng = location.longitude
                                                    val grid = GridCalculator.latLngToGrid(lat, lng)
                                                    gridText = grid
                                                    AppPreferences.setGridSquare(grid)
                                                    val pos = try {
                                                        val geocoder = android.location.Geocoder(context)
                                                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                                                        if (!addresses.isNullOrEmpty()) {
                                                            val addr = addresses[0]
                                                            listOfNotNull(addr.adminArea, addr.locality, addr.subLocality, addr.thoroughfare)
                                                                .filter { it.isNotBlank() }
                                                                .joinToString(" ")
                                                        } else {
                                                            grid
                                                        }
                                                    } catch (_: Exception) {
                                                        grid
                                                    }
                                                    locationText = pos
                                                    AppPreferences.setLocation(pos)
                                            }
                                        } catch (_: Exception) {}
                                    } else {
                                        locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }) {
                                    Icon(Icons.Default.MyLocation, "获取位置", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GridOn, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("\u7f51\u683c", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = gridText,
                            onValueChange = { gridText = it; AppPreferences.setGridSquare(it) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerif),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                            shape = RoundedCornerShape(8.dp),
                            colors = hamFieldColors()
                        )
                    }
                }

                // Timezone
                SettingsCard {
                    Column(Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text("\u65f6\u533a", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(10.dp))
                        ExposedDropdownMenuBox(expanded = tzExpanded, onExpandedChange = { tzExpanded = it }) {
                            OutlinedTextField(
                                value = timezoneOptions.find { it.first == uiState.selectedTimezone.id }?.second ?: "UTC",
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall,
                                shape = MaterialTheme.shapes.small,
                                colors = hamFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth().height(48.dp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tzExpanded) }
                            )
                            ExposedDropdownMenu(expanded = tzExpanded, onDismissRequest = { tzExpanded = false }) {
                                timezoneOptions.forEach { (id, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = { viewModel.setTimezone(ZoneId.of(id)); tzExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                // Equipment Maintenance
                SettingsCard {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Build,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(8.dp));Text("设备维护",style=MaterialTheme.typography.titleSmall) }
                        Spacer(Modifier.height(8.dp))
                        Text("天馈",style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        DragReorderableColumn(
                            items = antennaList.toList(),
                            onMove = { from, to -> EquipmentManager.moveAntenna(from, to); refreshEquipment() }
                        ) { item, _ ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                        EquipmentManager.removeAntenna(item)
                                        refreshEquipment()
                                        true
                                    } else false
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {},
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = true
                            ) {
                                Row(Modifier.fillMaxWidth().height(36.dp).padding(horizontal=4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(item, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f).padding(horizontal=8.dp))
                                    Icon(Icons.Default.DragHandle, "拖拽", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.4f))
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Surface(onClick={showAntennaAdd=true},modifier=Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.small,color=MaterialTheme.colorScheme.primary.copy(alpha=0.1f)){Row(Modifier.padding(horizontal=8.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Add,null,Modifier.size(12.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(4.dp));Text("添加天馈",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)}}
                        Spacer(Modifier.height(12.dp))
                        Text("设备",style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        DragReorderableColumn(
                            items = rigList.toList(),
                            onMove = { from, to ->
                                EquipmentManager.moveRigBrand(from, to)
                                refreshEquipment()
                            }
                        ) { cat, _ ->
                            val brandDismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                        EquipmentManager.removeRigBrand(cat.brand)
                                        refreshEquipment()
                                        true
                                    } else false
                                }
                            )
                            SwipeToDismissBox(
                                state = brandDismissState,
                                backgroundContent = {},
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = true
                            ) {
                                Column(Modifier.padding(bottom=6.dp)) {
                                Row(Modifier.fillMaxWidth().height(28.dp).padding(horizontal=4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(cat.brand, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f).padding(horizontal=4.dp))
                                    Icon(Icons.Default.DragHandle, "拖拽", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.4f))
                                }
                                DragReorderableColumn(
                                    items = cat.models,
                                    onMove = { from, to ->
                                        val newList = rigList.toList().toMutableList()
                                        val idx = newList.indexOfFirst { it.brand == cat.brand }
                                        if (idx >= 0) {
                                            val newModels = cat.models.toMutableList()
                                            val m = newModels.removeAt(from)
                                            newModels.add(to, m)
                                            newList[idx] = cat.copy(models = newModels)
                                            EquipmentManager.setRigs(newList)
                                            refreshEquipment()
                                        }
                                    }
                                ) { model, _ ->
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = {
                                            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                                EquipmentManager.removeRigModel(cat.brand, model)
                                                refreshEquipment()
                                                true
                                            } else false
                                        }
                                    )
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        backgroundContent = {},
                                        enableDismissFromStartToEnd = true,
                                        enableDismissFromEndToStart = true
                                    ) {
                                        Row(Modifier.fillMaxWidth().padding(start=8.dp).height(32.dp).padding(horizontal=4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(model, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f).padding(horizontal=4.dp))
                                            Icon(Icons.Default.DragHandle, "拖拽", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.4f))
                                        }
                                    }
                                }
                            }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Surface(onClick={showRigAdd=true},shape=MaterialTheme.shapes.small,color=MaterialTheme.colorScheme.primary.copy(alpha=0.1f),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(horizontal=8.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Add,null,Modifier.size(12.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(4.dp));Text("添加设备",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)}}
                    }
                }

                Spacer(Modifier.height(8.dp))
                // Cloudlog Sync
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cloud, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Cloudlog 同步设置", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("API 地址", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = cloudlogUrl,
                            onValueChange = { cloudlogUrl = it; AppPreferences.setCloudlogUrl(it) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(8.dp),
                            colors = hamFieldColors()
                        )
                        Spacer(Modifier.height(6.dp))
                        val maskTransformation = remember {
                            object : VisualTransformation {
                                override fun filter(text: AnnotatedString): TransformedText {
                                    val raw = text.text
                                    if (raw.length <= 10) return TransformedText(text, OffsetMapping.Identity)
                                    val maskCount = minOf(7, raw.length - 4)
                                    val prefixLen = (raw.length - maskCount) / 2
                                    val masked = raw.substring(0, prefixLen) + "*".repeat(maskCount) + raw.substring(prefixLen + maskCount)
                                    return TransformedText(AnnotatedString(masked), OffsetMapping.Identity)
                                }
                            }
                        }
                        Text("API Key", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = cloudlogApiKey,
                            onValueChange = { cloudlogApiKey = it; AppPreferences.setCloudlogApiKey(it) },
                            modifier = Modifier.fillMaxWidth().height(48.dp).onFocusChanged { cloudlogKeyFocused = it.isFocused },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(8.dp),
                            colors = hamFieldColors(),
                            visualTransformation = if (cloudlogKeyFocused) VisualTransformation.None else maskTransformation
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("台站 ID", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = stationProfileId,
                            onValueChange = { stationProfileId = it; AppPreferences.setStationProfileId(it) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(8.dp),
                            colors = hamFieldColors()
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("保存后自动上传", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Switch(
                                checked = autoUploadEnabled,
                                onCheckedChange = { autoUploadEnabled = it; AppPreferences.setAutoUploadEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    isTestingConn = true
                                    updateScope.launch {
                                        val res = com.hamlog.util.CloudlogSync.testConnection(cloudlogUrl, cloudlogApiKey)
                                        isTestingConn = false
                                        val msg = if (res.ok) "连接成功" else "连接失败: "
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                enabled = cloudlogUrl.isNotBlank() && !isTestingConn && !isSyncing,
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isTestingConn) {
                                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(if (isTestingConn) "测试中..." else "测试连接", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    isSyncing = true; syncResult = null; syncProgress = 0 to 0
                                    updateScope.launch {
                                        val contacts = viewModel.getAllContactsForSync()
                                        syncResult = com.hamlog.util.CloudlogSync.syncContacts(
                                            cloudlogUrl, cloudlogApiKey, contacts,
                                            callsign = callsignText, gridSquare = gridText, stationProfileId = stationProfileId
                                        ) { done, total -> syncProgress = done to total }
                                        isSyncing = false; showSyncResult = true
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                enabled = cloudlogUrl.isNotBlank() && cloudlogApiKey.isNotBlank() && !isSyncing && !isTestingConn && uiState.totalContacts > 0,
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(6.dp))
                                    Text("/", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text("同步日志", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }
                // Sync Result Dialog
                if (showSyncResult && syncResult != null) {
                    val r = syncResult!!
                    AlertDialog(
                        onDismissRequest = { showSyncResult = false },
                        shape = MaterialTheme.shapes.large,
                        containerColor = MaterialTheme.colorScheme.surface,
                        title = { Text("同步完成", fontWeight = FontWeight.SemiBold) },
                        text = {
                            Column {
                                Text("成功: ${r.success}, 失败: ${r.failed}", style = MaterialTheme.typography.bodyMedium)
                                if (r.lastResponse.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text("服务器响应:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(r.lastResponse, style = MaterialTheme.typography.bodySmall, maxLines = 4, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (r.errors.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("错误详情:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                    Text(r.errors.take(5).joinToString("\n"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, maxLines = 6)
                                }
                            }
                        },
                        confirmButton = { TextButton(onClick = { showSyncResult = false }) { Text("关闭") } }
                    )
                }

                // Stats
                SettingsCard {
                    Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("\u901a\u8054\u603b\u6570", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${uiState.totalContacts} \u6761",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = NotoSerif),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Export
                Button(
                    onClick = { viewModel.exportAdif() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !uiState.isExporting && uiState.totalContacts > 0,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.FileDownload, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (uiState.isExporting) "\u5bfc\u51fa\u4e2d..." else "\u5bfc\u51fa ADIF \u65e5\u5fd7", fontWeight = FontWeight.Medium)
                }

                // Import
                OutlinedButton(
                    onClick = { adifPicker.launch(arrayOf("text/plain", "application/octet-stream", "*/*")) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !uiState.isImporting,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (uiState.isImporting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.FileUpload, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (uiState.isImporting) "\u5bfc\u5165\u4e2d..." else "\u5bfc\u5165 ADIF \u65e5\u5fd7", fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(4.dp))

                // Check Update
                OutlinedButton(
                    onClick = {
                        isCheckingUpdate = true
                        updateScope.launch {
                            val info = com.hamlog.util.UpdateChecker.checkForUpdate(context)
                            updateInfo = info
                            isCheckingUpdate = false
                            showUpdateDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    enabled = !isCheckingUpdate,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isCheckingUpdate) "检查中..." else "检查更新", fontSize = 13.sp)
                }

                Spacer(Modifier.height(4.dp))
                // Footer
                val uriHandler = LocalUriHandler.current
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Designed by BI9BRH",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_github),
                        contentDescription = "GitHub",
                        modifier = Modifier.size(14.dp).clickable {
                            uriHandler.openUri("https://github.com/walker6253/ham-logs")
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { content() }
}

@Composable
private fun hamFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
