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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
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
                    val pos = String.format("%.5f, %.5f", lat, lng)
                    locationText = pos
                    AppPreferences.setLocation(pos)
                    val grid = GridCalculator.latLngToGrid(lat, lng)
                    gridText = grid
                    AppPreferences.setGridSquare(grid)
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
                                                val pos = String.format("%.5f, %.5f", lat, lng)
                                                locationText = pos
                                                AppPreferences.setLocation(pos)
                                                val grid = GridCalculator.latLngToGrid(lat, lng)
                                                gridText = grid
                                                AppPreferences.setGridSquare(grid)
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
                            onValueChange = { val v = it.uppercase(); gridText = v; AppPreferences.setGridSquare(v) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerif),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
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
                @OptIn(ExperimentalLayoutApi::class)
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Build,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(8.dp));Text("设备维护",style=MaterialTheme.typography.titleSmall) }
                        Spacer(Modifier.height(8.dp))
                        Text("天馈",style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            antennaList.forEach { item -> Surface(shape=MaterialTheme.shapes.small,color=MaterialTheme.colorScheme.surfaceVariant,modifier=Modifier.clickable{EquipmentManager.removeAntenna(item);refreshEquipment()}){Row(Modifier.padding(start=8.dp,end=6.dp,top=4.dp,bottom=4.dp),verticalAlignment=Alignment.CenterVertically){Text(item,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.width(4.dp));Icon(Icons.Default.Close,null,Modifier.size(10.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f))}} }
                            }
                        Spacer(Modifier.height(4.dp))
                        Surface(onClick={showAntennaAdd=true},modifier=Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.small,color=MaterialTheme.colorScheme.primary.copy(alpha=0.1f)){Row(Modifier.padding(horizontal=8.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Add,null,Modifier.size(12.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(4.dp));Text("添加",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)}}
                        Spacer(Modifier.height(12.dp))
                        Text("设备",style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        rigList.forEach { cat -> Column(Modifier.padding(bottom=8.dp)){Text(cat.brand,style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onSurface);Spacer(Modifier.height(4.dp));FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){cat.models.forEach{model->Surface(shape=MaterialTheme.shapes.extraSmall,color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.6f),modifier=Modifier.clickable{EquipmentManager.removeRigModel(cat.brand,model);refreshEquipment()}){Row(Modifier.padding(start=6.dp,end=6.dp,top=3.dp,bottom=3.dp),verticalAlignment=Alignment.CenterVertically){Text(model,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.width(3.dp));Icon(Icons.Default.Close,null,Modifier.size(9.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.4f))}}}}} }
                        Spacer(Modifier.height(4.dp))
                        Surface(onClick={showRigAdd=true},shape=MaterialTheme.shapes.small,color=MaterialTheme.colorScheme.primary.copy(alpha=0.1f),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(horizontal=8.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Add,null,Modifier.size(12.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(4.dp));Text("添加设备",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)}}
                    }
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
