package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.NfcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintFormScreen(viewModel: NfcViewModel, onBack: () -> Unit) {
    val currentStep by viewModel.currentFormStep.collectAsState()
    val isUploading by viewModel.isUploadingEvidence.collectAsState()
    val isSubmitting by viewModel.isSubmittingComplaint.collectAsState()
    val appsScriptUrlConfigured by viewModel.appsScriptUrl.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form states
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Upah / Gaji") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dateOccurred by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var witnesses by remember { mutableStateOf("") }
    var showUrlSetting by remember { mutableStateOf(false) }

    // Evidence Photo state
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var base64Data by remember { mutableStateOf<String?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var compressionInfo by remember { mutableStateOf("") }

    val categories = listOf(
        "Upah / Gaji",
        "Kesehatan & Keselamatan (K3)",
        "Kekerasan / Penganiayaan",
        "Tindak Pidana Perdagangan Orang (TPPO)",
        "Ketidakadilan Kontrak",
        "Lainnya"
    )

    // Activity launcher for choosing photos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            // Compress and convert to base64
            val result = compressAndEncodeImage(context, uri)
            if (result != null) {
                base64Data = result.first
                fileName = result.second
                
                // Let's estimate file size to reassure developer
                val byteCount = android.util.Base64.decode(result.first, android.util.Base64.DEFAULT).size
                val kb = byteCount / 1024
                compressionInfo = "Kompresi berhasil: $kb KB (Cocok untuk batas gratis)"
            } else {
                compressionInfo = "Gagal memproses gambar"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Form Pengaduan Multi-Step", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showUrlSetting = !showUrlSetting }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Setting Apps Script",
                            tint = if (appsScriptUrlConfigured.isNotBlank()) TablerSuccess else TablerSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TablerDark
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TablerLight)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Stepper Progress Indicator
                StepperProgress(currentStep = currentStep)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // CONFIG WINDOW FOR APPS SCRIPT
                    if (showUrlSetting) {
                        AppsScriptConfigSection(
                            currentUrl = appsScriptUrlConfigured,
                            onSave = { viewModel.saveAppsScriptUrl(it) }
                        )
                    }

                    // STEP CONTROLLER
                    when (currentStep) {
                        1 -> {
                            // Step 1: Informasi Dasar Kasus
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Langkah 1: Jenis & Lokasi Kasus",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TablerDark
                                    )

                                    // Kategori Dropdown
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Kategori Masalah",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TablerDark
                                        )
                                        var expanded by remember { mutableStateOf(false) }
                                        ExposedDropdownMenuBox(
                                            expanded = expanded,
                                            onExpandedChange = { expanded = !expanded }
                                        ) {
                                            OutlinedTextField(
                                                value = category,
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor()
                                                    .testTag("complaint_category_dropdown"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = TablerBlue,
                                                    unfocusedBorderColor = TablerBorder
                                                )
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                categories.forEach { selectionOption ->
                                                    DropdownMenuItem(
                                                        text = { Text(selectionOption, fontSize = 14.sp) },
                                                        onClick = {
                                                            category = selectionOption
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Judul Pengaduan
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Judul Laporan Singkat",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TablerDark
                                        )
                                        OutlinedTextField(
                                            value = title,
                                            onValueChange = { title = it },
                                            placeholder = { Text("Contoh: Penahanan Gaji ABK Kapal Longline", fontSize = 14.sp) },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("complaint_title_input"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = TablerBlue,
                                                unfocusedBorderColor = TablerBorder
                                            )
                                        )
                                    }

                                    // Lokasi Kejadian
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Tempat / Lokasi Kejadian",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TablerDark
                                        )
                                        OutlinedTextField(
                                            value = location,
                                            onValueChange = { location = it },
                                            placeholder = { Text("Contoh: Pelabuhan Benoa / Kapal MV Samudra", fontSize = 14.sp) },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("complaint_location_input"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = TablerBlue,
                                                unfocusedBorderColor = TablerBorder
                                            )
                                        )
                                    }

                                    // Tanggal Kejadian
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Kapan Kejadian Berlangsung (Tanggal)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TablerDark
                                        )
                                        OutlinedTextField(
                                            value = dateOccurred,
                                            onValueChange = { dateOccurred = it },
                                            placeholder = { Text("Contoh: Akhir Juni 2026 atau 25-06-2026", fontSize = 14.sp) },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("complaint_date_input"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = TablerBlue,
                                                unfocusedBorderColor = TablerBorder
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Step 2: Kronologi, Saksi & Bukti Foto
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Langkah 2: Kronologi Kejadian & Bukti",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TablerDark
                                    )

                                    // Deskripsi Laporan
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Kronologi Lengkap Kejadian",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TablerDark
                                        )
                                        OutlinedTextField(
                                            value = description,
                                            onValueChange = { description = it },
                                            placeholder = { 
                                                Text(
                                                    "Tulis kronologi sejelas mungkin. Sertakan nama kapal, nama pemilik, kronologi tindak kekerasan atau penahanan gaji yang terjadi secara detail.", 
                                                    fontSize = 13.sp
                                                ) 
                                            },
                                            minLines = 4,
                                            maxLines = 8,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("complaint_description_input"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = TablerBlue,
                                                unfocusedBorderColor = TablerBorder
                                            )
                                        )
                                    }

                                    // Saksi-Saksi
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Saksi-Saksi (Jika Ada)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TablerDark
                                        )
                                        OutlinedTextField(
                                            value = witnesses,
                                            onValueChange = { witnesses = it },
                                            placeholder = { Text("Contoh: Sesama rekan ABK 2 orang (Budi & Toni)", fontSize = 14.sp) },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("complaint_witnesses_input"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = TablerBlue,
                                                unfocusedBorderColor = TablerBorder
                                            )
                                        )
                                    }

                                    // Upload Bukti (Integrasi Jembatan Google Drive)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Unggah Foto Bukti Pendukung (KTP/Foto Kejadian/Kontrak Kerja)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TablerDark
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(TablerLight)
                                                .clickable { photoPickerLauncher.launch("image/*") }
                                                .padding(20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (imageUri != null) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                                    contentDescription = "Upload Bukti",
                                                    tint = if (imageUri != null) TablerSuccess else TablerBlue,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Text(
                                                    text = if (fileName != null) "File Terpilih: $fileName" else "Pilih Foto dari Galeri",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = TablerDark
                                                )
                                                if (compressionInfo.isNotBlank()) {
                                                    Text(
                                                        text = compressionInfo,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TablerSuccess
                                                    )
                                                } else {
                                                    Text(
                                                        text = "Otomatis di-kompres di bawah 300KB untuk hemat kuota gratis.",
                                                        fontSize = 11.sp,
                                                        color = TablerSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Step 3: Kerahasiaan & Konfirmasi Akhir
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Langkah 3: Jaminan Keamanan & Kirim",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TablerDark
                                    )

                                    // Jaminan Kerahasiaan Info
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TablerBlue.copy(alpha = 0.08f))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VerifiedUser,
                                                contentDescription = "Info",
                                                tint = TablerBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "Data Anda Dijamin Rahasia",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = TablerBlue
                                                )
                                                Text(
                                                    text = "Sistem pengaduan National Fishers Center memprioritaskan perlindungan bagi korban TPPO dan kekerasan laut. Identitas Anda terenkripsi.",
                                                    fontSize = 11.sp,
                                                    color = TablerDark.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }

                                    // Switch Opsi Anonim
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(TablerLight)
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Kirim sebagai Anonim (Tanpa Nama)",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = TablerDark
                                            )
                                            Text(
                                                text = "Nama Anda akan disembunyikan dari pihak eksternal/rujukan.",
                                                fontSize = 11.sp,
                                                color = TablerSecondary
                                            )
                                        }
                                        Switch(
                                            checked = isAnonymous,
                                            onCheckedChange = { isAnonymous = it },
                                            modifier = Modifier.testTag("complaint_anonymous_switch")
                                        )
                                    }

                                    Divider(color = TablerBorder)

                                    // Ringkasan Laporan
                                    Text(
                                        text = "Ringkasan Laporan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TablerDark
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Judul Laporan:", fontSize = 12.sp, color = TablerSecondary)
                                        Text(title.ifBlank { "(Belum diisi)" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TablerDark)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Kategori:", fontSize = 12.sp, color = TablerSecondary)
                                        Text(category, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TablerDark)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Foto Bukti:", fontSize = 12.sp, color = TablerSecondary)
                                        Text(if (fileName != null) "Tersedia ($fileName)" else "Tidak ada", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (fileName != null) TablerSuccess else TablerSecondary)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Jenis Pengiriman:", fontSize = 12.sp, color = TablerSecondary)
                                        Text(if (isAnonymous) "Rahasia (Anonim)" else "Terbuka (Sesuai KTP)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isAnonymous) TablerBlue else TablerSuccess)
                                    }
                                }
                            }
                        }
                    }

                    // STEPPER BOTTOM NAVIGATION BUTTONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { viewModel.setFormStep(currentStep - 1) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, TablerBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Sebelumnya", modifier = Modifier.size(16.dp))
                                    Text("Sebelumnya", fontSize = 13.sp, color = TablerDark)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp)) // Placeholder
                        }

                        // Next or Submit Button
                        if (currentStep < 3) {
                            Button(
                                onClick = { 
                                    // Validasi sederhana langkah 1
                                    if (currentStep == 1 && title.isBlank()) {
                                        // Jangan izinkan lanjut jika kosong
                                    } else {
                                        viewModel.setFormStep(currentStep + 1)
                                    }
                                },
                                enabled = if (currentStep == 1) title.isNotBlank() && location.isNotBlank() else true,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TablerBlue)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Lanjut", fontSize = 13.sp, color = Color.White)
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Lanjut", modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            // Submit complaint button
                            Button(
                                onClick = {
                                    viewModel.submitComplaint(
                                        title = title,
                                        category = category,
                                        description = description,
                                        location = location,
                                        date = dateOccurred,
                                        isAnonymous = isAnonymous,
                                        witnesses = witnesses,
                                        evidenceBase64 = base64Data,
                                        fileName = fileName
                                    )
                                },
                                enabled = !isSubmitting,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TablerSuccess),
                                modifier = Modifier.testTag("submit_complaint_form_button")
                            ) {
                                if (isSubmitting) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                        Text(if (isUploading) "Mengunggah Bukti..." else "Kirim Laporan...", fontSize = 13.sp)
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Kirim", modifier = Modifier.size(16.dp))
                                        Text("Kirim Pengaduan Resmi", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun StepperProgress(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step 1
        StepItem(
            stepNum = 1,
            label = "Informasi",
            isActive = currentStep >= 1,
            isCompleted = currentStep > 1,
            modifier = Modifier.weight(1f)
        )
        
        Divider(color = if (currentStep > 1) TablerBlue else TablerBorder, modifier = Modifier.weight(0.3f), thickness = 2.dp)

        // Step 2
        StepItem(
            stepNum = 2,
            label = "Bukti",
            isActive = currentStep >= 2,
            isCompleted = currentStep > 2,
            modifier = Modifier.weight(1f)
        )

        Divider(color = if (currentStep > 2) TablerBlue else TablerBorder, modifier = Modifier.weight(0.3f), thickness = 2.dp)

        // Step 3
        StepItem(
            stepNum = 3,
            label = "Kirim",
            isActive = currentStep >= 3,
            isCompleted = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StepItem(
    stepNum: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isCompleted) TablerSuccess
                    else if (isActive) TablerBlue
                    else TablerBorder
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = "Selesai", tint = Color.White, modifier = Modifier.size(14.dp))
            } else {
                Text(
                    text = stepNum.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else TablerSecondary
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) TablerDark else TablerSecondary
        )
    }
}

@Composable
fun AppsScriptConfigSection(currentUrl: String, onSave: (String) -> Unit) {
    var urlInput by remember { mutableStateOf(currentUrl) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, TablerBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Apps Script Integration", tint = TablerBlue)
                Text(
                    "Konfigurasi Jembatan Google Drive",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TablerDark
                )
            }

            Text(
                text = "Supabase DB Anda tetap aman dari kehabisan kuota storage. Pengaduan ini melampirkan file yang akan langsung dikirim ke Google Drive via Google Apps Script.",
                fontSize = 12.sp,
                color = TablerSecondary
            )

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                placeholder = { Text("https://script.google.com/macros/s/.../exec", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TablerBlue,
                    unfocusedBorderColor = TablerBorder
                )
            )

            Button(
                onClick = { onSave(urlInput) },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TablerBlue)
            ) {
                Text("Simpan URL Jembatan", fontSize = 12.sp)
            }

            Divider(color = TablerBorder)

            Text(
                "Kode Apps Script untuk Google Drive Anda (Salin ke Editor Apps Script Anda):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TablerDark
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = """
function doPost(e) {
  try {
    var data = JSON.parse(e.postData.contents);
    var fileBytes = Utilities.base64Decode(data.file);
    var blob = Utilities.newBlob(fileBytes, 'image/jpeg', data.filename);
    var file = DriveApp.createFile(blob);
    file.setSharing(DriveApp.Access.ANYONE, DriveApp.Permission.VIEW);
    return ContentService.createTextOutput(JSON.stringify({
      "status": "success",
      "url": file.getUrl()
    })).setMimeType(ContentService.MimeType.JSON);
  } catch (error) {
    return ContentService.createTextOutput(JSON.stringify({
      "status": "error",
      "message": error.toString()
    })).setMimeType(ContentService.MimeType.JSON);
  }
}
                        """.trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}

// Client-side image compression helper to maintain free tier quota
fun compressAndEncodeImage(context: Context, uri: Uri): Pair<String, String>? {
    return try {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Maksimal resolusi 1024px agar ukuran file tetap efisien (~150KB)
        val maxDimension = 1024
        val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val (newWidth, newHeight) = if (originalBitmap.width > originalBitmap.height) {
            if (originalBitmap.width > maxDimension) {
                Pair(maxDimension, (maxDimension / ratio).toInt())
            } else {
                Pair(originalBitmap.width, originalBitmap.height)
            }
        } else {
            if (originalBitmap.height > maxDimension) {
                Pair((maxDimension * ratio).toInt(), maxDimension)
            } else {
                Pair(originalBitmap.width, originalBitmap.height)
            }
        }

        val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        val outputStream = java.io.ByteArrayOutputStream()
        
        // Kompresi JPEG kualitas 75-80% -> hemat bandwidth & hemat Google Drive space
        resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 78, outputStream)
        val imageBytes = outputStream.toByteArray()
        val base64Encoded = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)

        val cursor = contentResolver.query(uri, null, null, null, null)
        var displayName = "bukti_foto_${System.currentTimeMillis()}.jpg"
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    displayName = it.getString(index)
                }
            }
        }

        Pair(base64Encoded, displayName)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
