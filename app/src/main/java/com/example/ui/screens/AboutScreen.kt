package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TablerLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.linearGradient(colors = listOf(TablerBlue, TablerBlueHover)))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Logo",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Tentang Kami",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "National Fishers Center (NFC) Indonesia",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Sejarah
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = TablerBlue)
                        Text(
                            text = "Sejarah & Pengelola",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TablerDark
                        )
                    }
                    Text(
                        text = "National Fishers Center (NFC) Indonesia didirikan dan dioperasikan oleh DFW (Destructive Fishing Watch) Indonesia sebagai respons atas maraknya kekerasan, kerja paksa, dan TPPO (Tindak Pidana Perdagangan Orang) yang dialami oleh awak kapal perikanan (ABK) baik di dalam negeri maupun internasional.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TablerDark,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "NFC berkomitmen sebagai wadah koordinasi penanganan kasus, pusat rujukan bantuan hukum dan medis, serta jembatan informasi ketenagakerjaan kelautan resmi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TablerDark,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Visi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = TablerBlue)
                        Text(
                            text = "Visi Kami",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TablerDark
                        )
                    }
                    Text(
                        text = "Terwujudnya ruang kerja laut yang adil, aman, dan sejahtera bagi seluruh awak kapal perikanan Indonesia, bebas dari segala bentuk intimidasi, pemerasan, dan eksploitasi perdagangan manusia.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TablerDark,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Misi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = TablerBlue)
                        Text(
                            text = "Misi Kami",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TablerDark
                        )
                    }
                    
                    val misis = listOf(
                        "Memberikan layanan edukasi hak-hak ketenagakerjaan bagi ABK pra-keberangkatan.",
                        "Menyediakan sistem pengaduan multi-kanal yang aman, gratis, dan terpercaya bagi korban atau saksi kekerasan di laut.",
                        "Melakukan advokasi hukum dan rujukan pemulihan medis/sosial dengan jaringan pemerintah (KKP, Kemenlu) serta organisasi internasional.",
                        "Menyebarluaskan info lowongan kerja perikanan resmi demi menekan angka calo ilegal."
                    )
                    
                    misis.forEachIndexed { index, misi ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = TablerBlue,
                                fontSize = 14.sp
                            )
                            Text(
                                text = misi,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TablerDark,
                                modifier = Modifier.weight(1f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
