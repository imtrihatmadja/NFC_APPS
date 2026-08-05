package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.ui.theme.*
import com.example.viewmodel.NfcViewModel

@Composable
fun JobsScreen(
    viewModel: NfcViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val jobs = posts.filter { it.type == "lowongan" }
    val context = LocalContext.current
    var showLoginDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TablerBlue.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, TablerBlue.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BusinessCenter,
                            contentDescription = "Jobs Icon",
                            tint = TablerBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Column {
                            Text(
                                text = "Lowongan Kerja Resmi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TablerDark
                            )
                            Text(
                                text = "Daftar lowongan awak kapal perikanan (ABK) berlisensi penuh, jaminan perlindungan asuransi, dan terbebas dari calo ilegal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TablerSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Job Listings
            if (jobs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada lowongan kerja tersedia saat ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TablerSecondary
                        )
                    }
                }
            } else {
                items(jobs) { job ->
                    JobItemCard(
                        job = job,
                        onApplyClick = {
                            if (currentUser != null) {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://wa.me/628119214141?text=Halo%20NFC%20Careers,%20saya%20tertarik%20melamar%20pekerjaan%20sebagai%20${job.title}")
                                }
                                context.startActivity(intent)
                            } else {
                                showLoginDialog = true
                            }
                        },
                        onPostClick = { viewModel.setSelectedPost(job) }
                    )
                }
            }
        }

        // Custom Login Prompt Dialog
        if (showLoginDialog) {
            AlertDialog(
                onDismissRequest = { showLoginDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(TablerBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Akses Terbatas",
                            tint = TablerBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Akses Melamar Terbatas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TablerDark,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Untuk melamar lowongan kerja resmi National Fishers Center (NFC) Indonesia, Anda harus masuk atau mendaftar akun terlebih dahulu untuk keamanan dan validitas data Anda.",
                        fontSize = 13.sp,
                        color = TablerSecondary,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLoginDialog = false
                            viewModel.setTab("Profil")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Masuk / Daftar Sekarang",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLoginDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Nanti Saja",
                            fontSize = 13.sp,
                            color = TablerSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun JobItemCard(
    job: Post,
    onApplyClick: () -> Unit,
    onPostClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TablerBorder)
    ) {
        Column {
            // Thumbnail
            if (!job.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = job.imageUrl,
                    contentDescription = job.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Tag
                Surface(
                    color = TablerSuccess.copy(alpha = 0.12f),
                    contentColor = TablerSuccess,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "TERVERIFIKASI NFC",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Title
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TablerDark
                )

                // Excerpt
                Text(
                    text = job.excerpt,
                    style = MaterialTheme.typography.bodySmall,
                    color = TablerSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                HorizontalDivider(color = TablerBorder)

                // Actions Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Penempatan",
                            fontSize = 9.sp,
                            color = TablerSecondary
                        )
                        Text(
                            text = "Perairan Timur ID / Kontrak Resmi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TablerDark
                        )
                    }

                    // Apply Pill Button (Image 4 Style)
                    Button(
                        onClick = onApplyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Lamar Kerja",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
