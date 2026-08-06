package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.ui.theme.*
import com.example.viewmodel.NfcViewModel

@Composable
fun ActivitiesScreen(
    viewModel: NfcViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val activities = posts.filter { it.type == "kegiatan" }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Activities Icon",
                        tint = TablerBlue,
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = "Kegiatan & Sosialisasi Kami",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TablerDark
                        )
                        Text(
                            text = "Ikuti rekam jejak kegiatan sosialisasi, advokasi keliling, dan roadshow yang diselenggarakan oleh National Fishers Center.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TablerSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Activity Listings
        if (activities.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada kegiatan tersedia saat ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TablerSecondary
                    )
                }
            }
        } else {
            items(activities) { activity ->
                ActivityItemCard(
                    activity = activity,
                    onPostClick = { viewModel.setSelectedPost(activity) }
                )
            }
        }
    }
}

@Composable
fun ActivityItemCard(
    activity: Post,
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
            // Featured Thumbnail (always displayed)
            val activityImageUrl = if (!activity.imageUrl.isNullOrEmpty()) activity.imageUrl else "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&q=80&w=800"

            AsyncImage(
                model = activityImageUrl,
                contentDescription = activity.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header details
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Tanggal",
                        tint = TablerWarning,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = activity.date,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TablerWarning
                    )
                }

                // Title
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TablerDark
                )

                // Excerpt
                Text(
                    text = activity.excerpt,
                    style = MaterialTheme.typography.bodySmall,
                    color = TablerSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                HorizontalDivider(color = TablerBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Oleh ${activity.authorName}",
                        fontSize = 11.sp,
                        color = TablerSecondary
                    )

                    Text(
                        text = "LIHAT DETAIL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TablerBlue
                    )
                }
            }
        }
    }
}
