package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.ui.theme.*
import com.example.viewmodel.NfcStats
import com.example.viewmodel.NfcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NfcViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val posts by viewModel.posts.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Local state for interactive features
    var likedPosts by remember { mutableStateOf(setOf<Int>()) }
    var bookmarkedPosts by remember { mutableStateOf(setOf<Int>()) }
    var showLoginDialog by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = isSyncing,
        onRefresh = { viewModel.syncWordPressData() },
        modifier = modifier
            .fillMaxSize()
            .testTag("home_pull_to_refresh")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_scroll_column"),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // 1. Hero Banner Section
        item {
            HeroBannerSection(
                onCtaClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/628119214141?text=Halo%20National%20Fishers%20Center%20Indonesia,%20saya%20ingin%20berkonsultasi")
                    }
                    context.startActivity(intent)
                }
            )
        }

        // 2. Statistik Pengaduan (Image 2 style)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Statistik Pengaduan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TablerDark,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = "Data statistik aduan awak kapal perikanan (ABK) pembaruan Juli 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = TablerSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                StatsGridSection(stats = stats)
            }
        }

        // Promo Modul Pelatihan Offline (Fase 3.5)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.setTab("Pelatihan") }
                    .testTag("home_training_promo_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(TablerBlue.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Edukasi",
                            tint = TablerBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pelatihan Mandiri ABK",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TablerDark
                        )
                        Text(
                            text = "Unduh modul & tonton video edukasi keselamatan laut secara penuh tanpa internet saat berlayar.",
                            fontSize = 12.sp,
                            color = TablerSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Buka Pelatihan",
                        tint = TablerBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Sub-Menu: Kalkulator Rekrutmen AKP (Member Only)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable {
                        if (currentUser != null) {
                            viewModel.setCalculatorOpen(true)
                        } else {
                            showLoginDialog = true
                        }
                    }
                    .testTag("home_calculator_promo_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(TablerBlue.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Kalkulator Rekrutmen",
                            tint = TablerBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = TablerBlue.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TablerBlue,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "KHUSUS MEMBER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TablerBlue
                                )
                            }
                        }
                        Text(
                            text = "Kalkulator Rekrutmen AKP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TablerDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Simulasi remunerasi transparan, potong kasbon, & standar upah layak sesuai Permen KP & ILO C.188.",
                            fontSize = 12.sp,
                            color = TablerSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Buka Kalkulator",
                        tint = TablerBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3. News / Informasi Terkini Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Informasi & Edukasi Terkini",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TablerDark
                    )
                    Text(
                        text = "Kumpulan berita, edukasi, dan lowongan kerja terbaru",
                        style = MaterialTheme.typography.bodySmall,
                        color = TablerSecondary
                    )
                }
                
                IconButton(
                    onClick = { viewModel.syncWordPressData() },
                    modifier = Modifier.testTag("sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync WordPress",
                        tint = TablerBlue
                    )
                }
            }
        }

        // 4. News Feed Cards (Image 1 style)
        val newsPosts = posts.filter { it.type == "berita" }.take(3)
        if (newsPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TablerBlue)
                }
            }
        } else {
            items(newsPosts) { post ->
                PostCardItem(
                    post = post,
                    isLiked = likedPosts.contains(post.id),
                    isBookmarked = bookmarkedPosts.contains(post.id),
                    onLikeToggle = {
                        likedPosts = if (likedPosts.contains(post.id)) {
                            likedPosts - post.id
                        } else {
                            likedPosts + post.id
                        }
                    },
                    onBookmarkToggle = {
                        bookmarkedPosts = if (bookmarkedPosts.contains(post.id)) {
                            bookmarkedPosts - post.id
                        } else {
                            bookmarkedPosts + post.id
                        }
                    },
                    onPostClick = { viewModel.setSelectedPost(post) }
                )
            }
        }

        // 5. WhatsApp Community CTA Section
        item {
            WhatsAppCtaSection(
                onJoinClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/628119214141?text=Halo%20NFC%20Indonesia,%20saya%20ingin%20bergabung%20dengan%20komunitas%20WhatsApp")
                    }
                    context.startActivity(intent)
                }
            )
        }

        // 6. Newsletter Subscribe Section (Tabler styled)
        item {
            NewsletterSection(viewModel = viewModel)
        }

        // 7. Mitra Pendukung Section
        item {
            MitraSection()
        }
    }

    // Dialog Peringatan Login untuk Akses Kalkulator
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Akses Terbatas",
                    tint = TablerBlue,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Akses Kalkulator Terbatas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TablerDark,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Untuk mengakses Kalkulator Rekrutmen & Upah Layak AKP, Anda harus masuk atau mendaftar akun terlebih dahulu.",
                    fontSize = 13.sp,
                    color = TablerSecondary,
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
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Masuk / Daftar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLoginDialog = false }
                ) {
                    Text("Batal", color = TablerSecondary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
}

@Composable
fun HeroBannerSection(onCtaClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(TablerBlue, TablerBlueHover)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "National Fishers Center",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                
                Text(
                    text = "Pusat Informasi, Edukasi & Pengaduan Awak Kapal Perikanan",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    lineHeight = 28.sp
                )
                
                Text(
                    text = "Menjamin hak-hak nelayan, memberikan bantuan hukum, dan menentang segala bentuk eksploitasi di laut.",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Button(
                    onClick = onCtaClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(30.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("hero_cta_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "WhatsApp",
                            tint = TablerBlue
                        )
                        Text(
                            text = "Konsultasi Cepat (WhatsApp)",
                            color = TablerBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsGridSection(stats: NfcStats) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First Row (Total Aduan & Total Korban)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Total Aduan with Circular Indicator
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${stats.totalAduan} Aduan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TablerDark
                        )
                        Text(
                            text = "${stats.aduanPending} belum verifikasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = TablerSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(44.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 0.85f },
                            modifier = Modifier.fillMaxSize(),
                            color = TablerBlue,
                            strokeWidth = 4.dp,
                            trackColor = TablerBorder,
                        )
                    }
                }
            }

            // Card 2: Total Korban with Progress Circle
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${stats.totalKorban} Korban",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TablerDark
                        )
                        Text(
                            text = "${stats.korbanTertangani} tertangani",
                            style = MaterialTheme.typography.bodySmall,
                            color = TablerSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(44.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { stats.korbanTertangani.toFloat() / stats.totalKorban.toFloat() },
                            modifier = Modifier.fillMaxSize(),
                            color = TablerSuccess,
                            strokeWidth = 4.dp,
                            trackColor = TablerBorder,
                        )
                    }
                }
            }
        }

        // Second Row (Dalam Negeri & Luar Negeri Trend Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 3: Dalam Negeri with Up-Trend Green Arrow
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Trend Up",
                                tint = TablerSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "+${stats.trenDalamNegeri}%",
                                color = TablerSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        
                        Text(
                            text = "${stats.aduanDalamNegeri} Kasus",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TablerDark,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "Dalam Negeri",
                            style = MaterialTheme.typography.bodySmall,
                            color = TablerSecondary
                        )
                    }
                }
            }

            // Card 4: Luar Negeri with Down-Trend Red Arrow
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Trend Down",
                                tint = TablerDanger,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${stats.trenLuarNegeri}%",
                                color = TablerDanger,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        
                        Text(
                            text = "${stats.aduanLuarNegeri} Kasus",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TablerDark,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "Luar Negeri",
                            style = MaterialTheme.typography.bodySmall,
                            color = TablerSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostCardItem(
    post: Post,
    isLiked: Boolean,
    isBookmarked: Boolean,
    onLikeToggle: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onPostClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onPostClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TablerBorder)
    ) {
        Column {
            // Featured Image (always displayed)
            val cardImageUrl = if (!post.imageUrl.isNullOrEmpty()) post.imageUrl else {
                when (post.type) {
                    "lowongan" -> "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&q=80&w=800"
                    "kegiatan" -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&q=80&w=800"
                    else -> "https://images.unsplash.com/photo-1516466723877-e4ec1d736c8a?auto=format&fit=crop&q=80&w=800"
                }
            }

            AsyncImage(
                model = cardImageUrl,
                contentDescription = post.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            // Post Details Content
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Type Badge (Pill button layout in Image 1/4)
                Surface(
                    color = when(post.type) {
                        "lowongan" -> TablerSuccess.copy(alpha = 0.12f)
                        "kegiatan" -> TablerWarning.copy(alpha = 0.12f)
                        else -> TablerBlue.copy(alpha = 0.12f)
                    },
                    contentColor = when(post.type) {
                        "lowongan" -> TablerSuccess
                        "kegiatan" -> TablerWarning
                        else -> TablerBlue
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = post.type.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Title
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TablerDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Excerpt
                Text(
                    text = post.excerpt,
                    style = MaterialTheme.typography.bodySmall,
                    color = TablerSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                // Divider line
                HorizontalDivider(color = TablerBorder, modifier = Modifier.padding(vertical = 4.dp))

                // Image 1 Style actions footer (Heart, Bookmark/Comments, Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: Author & Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(TablerBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.authorName.firstOrNull()?.toString() ?: "A",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TablerBlue
                            )
                        }
                        
                        Column {
                            Text(
                                text = post.authorName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TablerDark
                            )
                            Text(
                                text = post.date,
                                fontSize = 9.sp,
                                color = TablerSecondary
                            )
                        }
                    }

                    // Right Side: Heart, Comments/Bookmark, Share Icon buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Likes Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable { onLikeToggle() }
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) TablerDanger else TablerSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isLiked) "13" else "12",
                                fontSize = 11.sp,
                                color = TablerSecondary
                            )
                        }

                        // Bookmark Icon
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) TablerBlue else TablerSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onBookmarkToggle() }
                        )

                        // Share Icon
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TablerSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppCtaSection(onJoinClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)), // Soft Green background
        border = BorderStroke(1.dp, TablerSuccess.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TablerSuccess.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "WhatsApp Community",
                    tint = TablerSuccess,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Grup Komunitas ABK & Nelayan",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TablerDark
                )
                Text(
                    text = "Bergabung dengan ribuan nelayan Indonesia untuk saling berbagi info & sosialisasi hukum.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TablerSecondary,
                    lineHeight = 16.sp
                )
            }
            
            IconButton(
                onClick = onJoinClick,
                colors = IconButtonDefaults.iconButtonColors(containerColor = TablerSuccess)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Gabung",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun NewsletterSection(viewModel: NfcViewModel) {
    val email by viewModel.newsletterEmail.collectAsState()
    val isSubscribed by viewModel.newsletterSubscribed.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TablerBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Dapatkan Berita Terupdate",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TablerDark
            )
            Text(
                text = "Berlangganan buletin berkala untuk info edukasi, pengaduan terbaru, serta lowongan kerja kelautan resmi.",
                style = MaterialTheme.typography.bodySmall,
                color = TablerSecondary,
                lineHeight = 18.sp
            )

            AnimatedVisibility(
                visible = !isSubscribed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.newsletterEmail.value = it },
                        placeholder = { 
                            Text(
                                text = "Alamat Email Anda", 
                                fontSize = 13.sp,
                                color = TablerSecondary
                            ) 
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedTextColor = TablerDark,
                            unfocusedTextColor = TablerDark,
                            focusedBorderColor = TablerBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedPlaceholderColor = TablerSecondary,
                            unfocusedPlaceholderColor = TablerSecondary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("newsletter_email_input")
                    )

                    Button(
                        onClick = { viewModel.onSubscribeNewsletter() },
                        colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("newsletter_subscribe_button")
                    ) {
                        Text(
                            text = "Subscribe", 
                            color = Color.White, 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isSubscribed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = TablerSuccess.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = TablerSuccess
                        )
                        Text(
                            text = "Terima kasih! Email Anda telah terdaftar.",
                            color = TablerSuccess,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MitraSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Mitra Pendukung Kami",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TablerDark
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            val mitras = listOf("DFW Indonesia", "KKP RI", "SPPI", "Kemenlu RI", "Bakamla")
            items(mitras) { name ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TablerLight),
                    border = BorderStroke(1.dp, TablerBorder),
                    modifier = Modifier.height(38.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = name,
                            color = TablerSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
