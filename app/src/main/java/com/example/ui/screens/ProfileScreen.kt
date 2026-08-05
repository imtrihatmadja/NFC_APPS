package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Complaint
import com.example.ui.theme.*
import com.example.viewmodel.NfcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: NfcViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val userComplaints by viewModel.complaints.collectAsState()
    val showComplaintForm by viewModel.showComplaintForm.collectAsState()
    
    val scrollState = rememberScrollState()
    var selectedComplaintForTracking by remember { mutableStateOf<Complaint?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TablerLight)
    ) {
        user?.let { u ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Digital Member Card NFC Indonesia
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nfc_member_card"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, TablerBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(TablerBlue, Color(0xFF1D4ED8)) // Biru kelautan bergradasi
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Card Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBoat,
                                        contentDescription = "Ship",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "KARTU ANGGOTA NFC",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = "NFC Enabled",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Card Body Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Placeholder
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Portrait",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = u.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "WA: ${u.phone}",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

                            // Card Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ID ANGGOTA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = u.memberId,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "BERGABUNG",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = u.joinDate,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Keaktifan & Fitur Ringkasan Anggota
                Text(
                    text = "Layanan & Layanan Cepat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TablerDark
                )

                // Grid stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, TablerBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Aktif",
                                tint = TablerSuccess,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Status Keaktifan",
                                fontSize = 11.sp,
                                color = TablerSecondary
                            )
                            Text(
                                text = "Aktif Terverifikasi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TablerDark
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, TablerBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Aduan",
                                tint = TablerBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Aduan Saya",
                                fontSize = 11.sp,
                                color = TablerSecondary
                            )
                            Text(
                                text = if (userComplaints.isEmpty()) "Belum Ada" else "${userComplaints.size} Pengaduan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TablerDark
                            )
                        }
                    }
                }

                // REAL COMPLAINTS LIST (FASE 3)
                if (userComplaints.isNotEmpty()) {
                    Text(
                        text = "Daftar Pengaduan Saya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TablerDark
                    )

                    userComplaints.forEach { complaint ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedComplaintForTracking = complaint },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, TablerBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Category Tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (complaint.category) {
                                                    "Tindak Pidana Perdagangan Orang (TPPO)" -> TablerDanger.copy(alpha = 0.1f)
                                                    "Kekerasan / Penganiayaan" -> TablerWarning.copy(alpha = 0.15f)
                                                    else -> TablerBlue.copy(alpha = 0.1f)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = complaint.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (complaint.category) {
                                                "Tindak Pidana Perdagangan Orang (TPPO)" -> TablerDanger
                                                "Kekerasan / Penganiayaan" -> Color(0xFFD97706)
                                                else -> TablerBlue
                                            }
                                        )
                                    }

                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (complaint.status) {
                                                    "Selesai" -> TablerSuccess.copy(alpha = 0.1f)
                                                    "Rujukan" -> TablerWarning.copy(alpha = 0.1f)
                                                    "Diproses" -> TablerBlue.copy(alpha = 0.1f)
                                                    else -> TablerSecondary.copy(alpha = 0.15f)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = complaint.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (complaint.status) {
                                                "Selesai" -> TablerSuccess
                                                "Rujukan" -> TablerWarning
                                                "Diproses" -> TablerBlue
                                                else -> TablerSecondary
                                            }
                                        )
                                    }
                                }

                                Text(
                                    text = complaint.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TablerDark
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ID: ${complaint.id}",
                                        fontSize = 11.sp,
                                        color = TablerSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = complaint.dateCreated,
                                        fontSize = 11.sp,
                                        color = TablerSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // List menu bantuan cepat
                Text(
                    text = "Bantuan & Layanan Tambahan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TablerDark
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, TablerBorder)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        ProfileMenuItem(
                            icon = Icons.Default.ReportProblem,
                            title = "Lapor Kasus Pengaduan Baru",
                            subtitle = "Laporkan kekerasan, gaji, atau TPPO laut",
                            onClick = { viewModel.openComplaintForm() }
                        )
                        Divider(color = TablerBorder)
                        ProfileMenuItem(
                            icon = Icons.Default.Book,
                            title = "Pelatihan Dasar Pelaut",
                            subtitle = "Edukasi keselamatan laut & hak pelaut",
                            onClick = { viewModel.setTab("Kegiatan") }
                        )
                        Divider(color = TablerBorder)
                        ProfileMenuItem(
                            icon = Icons.Default.Calculate,
                            title = "Kalkulator Pengupahan AKP",
                            subtitle = "Simulasi upah layak, potong kasbon & gaji transparan",
                            onClick = { viewModel.setCalculatorOpen(true) }
                        )
                        Divider(color = TablerBorder)
                        ProfileMenuItem(
                            icon = Icons.Default.HelpOutline,
                            title = "Bantuan / FAQ",
                            subtitle = "Panduan cara kerja & rujukan NFC",
                            onClick = { viewModel.setTab("Tentang Kami") }
                        )
                    }
                }

                // Logout Button
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .testTag("logout_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TablerDanger.copy(alpha = 0.1f),
                        contentColor = TablerDanger
                    ),
                    border = BorderStroke(1.dp, TablerDanger.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Keluar",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Keluar dari Akun",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Sesi kosong. Silakan masuk.", color = TablerSecondary)
            }
        }
    }

    // ==========================================
    // MULTI-STEP COMPLAINT FORM OVERLAY (FASE 3)
    // ==========================================
    AnimatedVisibility(
        visible = showComplaintForm,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        ComplaintFormScreen(
            viewModel = viewModel,
            onBack = { viewModel.closeComplaintForm() }
        )
    }

    // ==========================================
    // TRACKING TIMELINE MODAL/SHEET (FASE 3)
    // ==========================================
    selectedComplaintForTracking?.let { complaint ->
        AlertDialog(
            onDismissRequest = { selectedComplaintForTracking = null },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Status Pengaduan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TablerDark
                    )
                    Text(
                        text = "ID: ${complaint.id}",
                        fontSize = 12.sp,
                        color = TablerSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Divider(color = TablerBorder)

                    Text(
                        text = complaint.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TablerDark
                    )

                    // Case description brief
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Kategori: ${complaint.category}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TablerBlue)
                        Text("Kronologi: ${complaint.description}", fontSize = 12.sp, color = TablerDark.copy(alpha = 0.8f))
                        Text("Lokasi: ${complaint.location}", fontSize = 11.sp, color = TablerSecondary)
                        if (!complaint.evidenceUrl.isNullOrBlank()) {
                            Text(
                                text = "Lampiran Terunggah (Google Drive):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TablerDark
                            )
                            Text(
                                text = complaint.evidenceUrl,
                                fontSize = 10.sp,
                                color = TablerBlue,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    // Klik untuk membuka tautan Google Drive
                                }
                            )
                        }
                    }

                    Divider(color = TablerBorder)

                    Text(
                        text = "Alur Tracking Kasus NFC",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TablerDark
                    )

                    // Timeline flow layout
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TimelineNode(
                            statusName = "Diajukan",
                            description = "Laporan masuk ke database National Fishers Center.",
                            isActive = true,
                            isCurrent = complaint.status == "Diajukan",
                            dateText = complaint.dateCreated
                        )

                        TimelineNode(
                            statusName = "Diproses",
                            description = "Kasus sedang dianalisis oleh legal counsel DFW Indonesia.",
                            isActive = complaint.status == "Diproses" || complaint.status == "Rujukan" || complaint.status == "Selesai",
                            isCurrent = complaint.status == "Diproses",
                            dateText = if (complaint.status != "Diajukan") "Sedang Verifikasi" else null
                        )

                        TimelineNode(
                            statusName = "Rujukan",
                            description = "Kasus dikoordinasikan dengan KKP, BP2MI, atau kepolisian terkait.",
                            isActive = complaint.status == "Rujukan" || complaint.status == "Selesai",
                            isCurrent = complaint.status == "Rujukan",
                            dateText = if (complaint.status == "Rujukan" || complaint.status == "Selesai") "Forwarded" else null
                        )

                        TimelineNode(
                            statusName = "Selesai",
                            description = "Kasus tuntas terselesaikan dengan solusi bagi korban ABK.",
                            isActive = complaint.status == "Selesai",
                            isCurrent = complaint.status == "Selesai",
                            dateText = if (complaint.status == "Selesai") "Terselesaikan" else null
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedComplaintForTracking = null }) {
                    Text("Tutup", fontWeight = FontWeight.Bold, color = TablerBlue)
                }
            }
        )
    }
}

@Composable
fun TimelineNode(
    statusName: String,
    description: String,
    isActive: Boolean,
    isCurrent: Boolean,
    dateText: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCurrent) TablerBlue
                        else if (isActive) TablerSuccess
                        else TablerBorder
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isActive && !isCurrent) {
                    Icon(Icons.Default.Check, contentDescription = "Active", tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .background(if (isActive && !isCurrent) TablerSuccess else TablerBorder)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isCurrent) TablerBlue else if (isActive) TablerSuccess else TablerSecondary
                )
                if (dateText != null) {
                    Text(
                        text = dateText,
                        fontSize = 11.sp,
                        color = TablerSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = description,
                fontSize = 11.sp,
                color = TablerSecondary
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TablerLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TablerBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TablerDark
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TablerSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Buka",
            tint = TablerBorder,
            modifier = Modifier.size(20.dp)
        )
    }
}

