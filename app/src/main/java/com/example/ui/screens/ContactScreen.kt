package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.NfcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    viewModel: NfcViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contactName by viewModel.contactName.collectAsState()
    val contactEmail by viewModel.contactEmail.collectAsState()
    val contactMessage by viewModel.contactMessage.collectAsState()
    val isSubmitted by viewModel.contactSubmitted.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TablerLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TablerBlue),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Contact Logo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Hubungi Kami",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Kami siap membantu keluhan, pendampingan hukum, dan konsultasi bagi ABK/Nelayan Indonesia secara gratis.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Office Locations
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Lokasi Kantor Pelayanan Kami",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TablerDark
                )
                
                OfficeLocationCard(
                    city = "Jakarta (Kantor Pusat DFW)",
                    address = "Jl. Kembang Raya No. 12, Kramat, Senen, Jakarta Pusat, DKI Jakarta",
                    phone = "021-3140590"
                )
                OfficeLocationCard(
                    city = "Posko Pelayanan Bali",
                    address = "Pesisir Pantai Kedonganan, Kuta Selatan, Kabupaten Badung, Bali",
                    phone = "0811-1222-333"
                )
                OfficeLocationCard(
                    city = "Posko Pelayanan Sulawesi Utara",
                    address = "Pelabuhan Perikanan Samudera Bitung, Kota Bitung, Sulawesi Utara",
                    phone = "0811-1222-444"
                )
            }
        }

        // Contact / Message Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Kirim Pesan Langsung",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TablerDark
                    )
                    
                    AnimatedVisibility(
                        visible = !isSubmitted,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Name Input
                            TextField(
                                value = contactName,
                                onValueChange = { viewModel.contactName.value = it },
                                label = { Text("Nama Lengkap") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = TablerLight,
                                    unfocusedContainerColor = TablerLight,
                                    focusedIndicatorColor = TablerBlue
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_name_input")
                            )

                            // Email Input
                            TextField(
                                value = contactEmail,
                                onValueChange = { viewModel.contactEmail.value = it },
                                label = { Text("Alamat Email") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = TablerLight,
                                    unfocusedContainerColor = TablerLight,
                                    focusedIndicatorColor = TablerBlue
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_email_input")
                            )

                            // Message Input
                            TextField(
                                value = contactMessage,
                                onValueChange = { viewModel.contactMessage.value = it },
                                label = { Text("Isi Pesan / Pertanyaan Anda") },
                                minLines = 3,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = TablerLight,
                                    unfocusedContainerColor = TablerLight,
                                    focusedIndicatorColor = TablerBlue
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_message_input")
                            )

                            Button(
                                onClick = { viewModel.onSubmitContactForm() },
                                colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("contact_submit_button")
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                                    Text("Kirim Pesan", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isSubmitted,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            color = TablerSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = TablerSuccess,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Pesan Berhasil Terkirim!",
                                    color = TablerSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Tim National Fishers Center akan meninjau pesan Anda dan membalas melalui email secepatnya.",
                                    color = TablerSuccess,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Image 4 Social Icon Buttons
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Ikuti Media Sosial Kami",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TablerSecondary
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SocialIconButton(
                            icon = Icons.Default.Facebook,
                            color = Color(0xFF1877F2),
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://facebook.com/dfwindonesia")
                                }
                                context.startActivity(intent)
                            }
                        )
                        SocialIconButton(
                            icon = Icons.Default.Language,
                            color = Color(0xFFE1306C),
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://instagram.com/nfc.indonesia")
                                }
                                context.startActivity(intent)
                            }
                        )
                        SocialIconButton(
                            icon = Icons.Default.Share,
                            color = Color(0xFF1DA1F2),
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://x.com/dfw_indonesia")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OfficeLocationCard(
    city: String,
    address: String,
    phone: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TablerBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TablerBlue
                )
                Text(
                    text = city,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TablerDark
                )
            }
            
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = TablerSecondary,
                lineHeight = 16.sp
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = TablerSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = phone,
                    fontSize = 11.sp,
                    color = TablerSecondary
                )
            }
        }
    }
}

@Composable
fun SocialIconButton(
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
