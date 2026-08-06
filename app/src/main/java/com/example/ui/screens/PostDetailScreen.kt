package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.text.Html
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: Post,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = post.type.uppercase(), 
                        fontSize = 15.sp, 
                        fontWeight = FontWeight.Bold,
                        color = TablerDark
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Kembali",
                            tint = TablerDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    // Share Option
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_SUBJECT, post.title)
                            putExtra(Intent.EXTRA_TEXT, "${post.title}\n\nBaca selengkapnya di: ${post.link.ifEmpty { "https://nfc.or.id" }}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan artikel"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share, 
                            contentDescription = "Bagikan",
                            tint = TablerBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TablerDark,
                    navigationIconContentColor = TablerDark,
                    actionIconContentColor = TablerBlue
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Featured Image (always displayed)
            val displayImageUrl = if (!post.imageUrl.isNullOrEmpty()) post.imageUrl else {
                when (post.type) {
                    "lowongan" -> "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&q=80&w=800"
                    "kegiatan" -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&q=80&w=800"
                    else -> "https://images.unsplash.com/photo-1516466723877-e4ec1d736c8a?auto=format&fit=crop&q=80&w=800"
                }
            }

            AsyncImage(
                model = displayImageUrl,
                contentDescription = post.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                contentScale = ContentScale.Crop
            )

            // Article Content Area
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TablerDark,
                    lineHeight = 28.sp
                )

                // Metadata details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Penulis: ${post.authorName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TablerSecondary
                    )
                    Text(
                        text = "Tanggal: ${post.date}",
                        fontSize = 11.sp,
                        color = TablerSecondary
                    )
                }

                HorizontalDivider(color = TablerBorder, modifier = Modifier.padding(vertical = 4.dp))

                // Full text (cleaning HTML tags dynamically)
                val cleanText = Html.fromHtml(post.content, Html.FROM_HTML_MODE_LEGACY).toString()
                Text(
                    text = cleanText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TablerDark,
                    lineHeight = 22.sp
                )

                // External Link Button (Image 4 Style Pill Button)
                if (post.link.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(post.link)
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(
                                text = "Baca di Website Resmi",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
