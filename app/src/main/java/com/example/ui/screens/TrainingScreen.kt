package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrainingMaterial
import com.example.ui.theme.*
import com.example.viewmodel.NfcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(viewModel: NfcViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val materials by viewModel.trainingMaterials.collectAsState()
    var selectedCategory by remember { mutableStateOf("Semua") }
    var activeReadMaterial by remember { mutableStateOf<TrainingMaterial?>(null) }

    val categories = listOf("Semua", "Keselamatan Laut", "Hak ABK & Regulasi", "Kesehatan & K3")

    val filteredMaterials = remember(materials, selectedCategory) {
        if (selectedCategory == "Semua") {
            materials
        } else {
            materials.filter { it.category == selectedCategory }
        }
    }

    if (currentUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_locked_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Lock icon container
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(TablerBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Icon",
                            tint = TablerBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Akses Pelatihan Mandiri Terbatas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TablerDark,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Modul edukasi dasar kelautan, hak-hak hukum ABK perikanan, serta panduan kesehatan kerja laut ini hanya tersedia secara eksklusif bagi anggota terdaftar National Fishers Center (NFC) Indonesia.",
                        fontSize = 13.sp,
                        color = TablerSecondary,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.setTab("Profil") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = "Masuk Icon",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Masuk / Daftar Sekarang",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Proses pendaftaran gratis dan hanya membutuhkan verifikasi nomor WhatsApp aktif Anda.",
                        fontSize = 11.sp,
                        color = TablerSecondary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = TablerBlue),
                        border = BorderStroke(1.dp, TablerBlue.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "School Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Modul Pelatihan Mandiri",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Text(
                                text = "Akses Materi Offline & Edukasi ABK",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "Unduh materi saat mendapat sinyal internet di darat agar tetap bisa dipelajari tanpa kuota selama berlayar di tengah samudera.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Category Filter Chips (Horizontal Slider)
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TablerBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = TablerDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFF94A3B8),
                                    selectedBorderColor = TablerBlue
                                )
                            )
                        }
                    }
                }

                // Info Offline
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "Offline Info",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Materi berlabel 'Tersedia Offline' dapat dibaca secara penuh tanpa koneksi internet kapan pun Anda berada.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // List of Training Materials
                if (filteredMaterials.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Kosong",
                                tint = TablerSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Tidak ada materi pelatihan di kategori ini.",
                                fontSize = 14.sp,
                                color = TablerSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredMaterials, key = { it.id }) { material ->
                        TrainingMaterialCard(
                            material = material,
                            onDownload = { viewModel.downloadTrainingMaterial(material) },
                            onOpen = { activeReadMaterial = material },
                            onDelete = { viewModel.deleteDownloadedMaterial(material.id) },
                            onToggleCompletion = { viewModel.toggleMaterialCompletion(material.id, !material.isCompleted) }
                        )
                    }
                }
            }

            // Expanded Reader overlay screen
            AnimatedVisibility(
                visible = activeReadMaterial != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeReadMaterial?.let { material ->
                    OfflineMaterialReader(
                        material = material,
                        onBack = { activeReadMaterial = null },
                        onToggleCompletion = {
                            viewModel.toggleMaterialCompletion(material.id, !material.isCompleted)
                            // Perbarui data lokal pembaca aktif agar UI tersinkronisasi langsung
                            activeReadMaterial = material.copy(isCompleted = !material.isCompleted)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TrainingMaterialCard(
    material: TrainingMaterial,
    onDownload: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onToggleCompletion: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("material_card_${material.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TablerBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category & Type badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = material.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TablerBlue
                )

                // Type badge with corresponding color
                val badgeColor = when (material.type) {
                    "PDF" -> Color(0xFFE53935)
                    "VIDEO" -> Color(0xFF1E88E5)
                    else -> Color(0xFF43A047)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, badgeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = material.type,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Title & Description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = material.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TablerDark
                )

                Text(
                    text = material.description,
                    fontSize = 12.sp,
                    color = TablerSecondary,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Divider(color = TablerBorder.copy(alpha = 0.5f))

            // Progress download and status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Size and status info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (material.isDownloaded) Icons.Default.CheckCircle else Icons.Default.InsertDriveFile,
                        contentDescription = "Status file",
                        tint = if (material.isDownloaded) Color(0xFF43A047) else TablerSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (material.isDownloaded) "Tersedia Offline" else "Online (${material.fileSize})",
                        fontSize = 11.sp,
                        fontWeight = if (material.isDownloaded) FontWeight.Bold else FontWeight.Medium,
                        color = if (material.isDownloaded) Color(0xFF2E7D32) else TablerSecondary
                    )
                }

                // Completion status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Checkbox(
                        checked = material.isCompleted,
                        onCheckedChange = { onToggleCompletion() },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("completion_check_${material.id}"),
                        colors = CheckboxDefaults.colors(checkedColor = TablerBlue)
                    )
                    Text(
                        text = "Selesai",
                        fontSize = 11.sp,
                        fontWeight = if (material.isCompleted) FontWeight.Bold else FontWeight.Medium,
                        color = if (material.isCompleted) TablerBlue else TablerSecondary,
                        modifier = Modifier.clickable { onToggleCompletion() }
                    )
                }
            }

            // Downloading status bar
            if (material.downloadProgress > 0 && material.downloadProgress < 100) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mengunduh modul...", fontSize = 10.sp, color = TablerBlue)
                        Text("${material.downloadProgress}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TablerBlue)
                    }
                    LinearProgressIndicator(
                        progress = { material.downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = TablerBlue,
                        trackColor = TablerBorder
                    )
                }
            }

            // Main Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Read / Open Button (Available offline or online, but if offline it is guaranteed to work!)
                Button(
                    onClick = { onOpen() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_material_${material.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Buka",
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Buka Materi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Download or delete offline buttons
                if (material.isDownloaded) {
                    // Delete offline download button to save space
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier
                            .testTag("delete_material_${material.id}")
                            .background(Color(0xFFFEEBEE), RoundedCornerShape(6.dp))
                            .border(BorderStroke(1.dp, Color(0xFFFFCDD2)), RoundedCornerShape(6.dp))
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Unduhan",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    if (material.downloadProgress == 0) {
                        // Download for offline access button
                        Button(
                            onClick = { onDownload() },
                            modifier = Modifier
                                .testTag("download_material_${material.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, TablerBorder),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Unduh",
                                    tint = TablerDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Simpan Offline", fontSize = 12.sp, color = TablerDark)
                            }
                        }
                    } else {
                        // Disabled downloading button
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Mengunduh...", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineMaterialReader(
    material: TrainingMaterial,
    onBack: () -> Unit,
    onToggleCompletion: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = material.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TablerDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Edukasi ${material.type} • Offline Reader",
                            fontSize = 11.sp,
                            color = TablerSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali ke daftar",
                            tint = TablerDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TablerLight)
                .padding(innerPadding)
        ) {
            // Main content block
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, TablerBorder), RoundedCornerShape(8.dp))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Document warning or meta banner
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFECB3))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Keamanan",
                                    tint = Color(0xFFFF8F00),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Dokumen ini telah diunduh dan tersertifikasi resmi oleh National Fishers Center (NFC) Indonesia.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8D6E63)
                                )
                            }
                        }
                    }

                    // Content title and text rendering
                    item {
                        Text(
                            text = material.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TablerDark
                        )
                    }

                    item {
                        Text(
                            text = material.textContent ?: "Tidak ada teks konten tertulis yang tersedia untuk dokumen ini.",
                            fontSize = 14.sp,
                            color = TablerDark.copy(alpha = 0.85f),
                            lineHeight = 22.sp
                        )
                    }

                    // Bottom safety disclaimer
                    item {
                        Divider(color = TablerBorder)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Catatan Penting: Jika Anda berada dalam situasi darurat di laut, segera gunakan tombol darurat pengaduan pada aplikasi atau hubungi pihak berwenang melalui radio kapal.",
                            fontSize = 11.sp,
                            color = TablerSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Reader bottom control panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Toggle completion
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { onToggleCompletion() }
                    ) {
                        Checkbox(
                            checked = material.isCompleted,
                            onCheckedChange = { onToggleCompletion() },
                            colors = CheckboxDefaults.colors(checkedColor = TablerBlue),
                            modifier = Modifier.testTag("reader_completion_check")
                        )
                        Column {
                            Text(
                                text = "Selesai Mempelajari",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TablerDark
                            )
                            Text(
                                text = if (material.isCompleted) "Tandai belum selesai" else "Tandai sebagai selesai",
                                fontSize = 11.sp,
                                color = TablerSecondary
                            )
                        }
                    }

                    // Close reader button
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Tutup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
