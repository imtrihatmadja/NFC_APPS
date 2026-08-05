package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
fun LoginScreen(viewModel: NfcViewModel) {
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val isVerifying by viewModel.isVerifying.collectAsState()
    val resendCountdown by viewModel.resendCountdown.collectAsState()
    val otpError by viewModel.otpError.collectAsState()

    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TablerLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo & Title Header
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = "Shield Verified",
                tint = TablerSuccess,
                modifier = Modifier.size(56.dp)
            )

            Text(
                text = "Keanggotaan NFC",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TablerDark
            )

            Text(
                text = "Khusus ABK & Nelayan. Masuk atau Daftar dalam 1 menit tanpa email, cukup nomor WhatsApp aktif.",
                fontSize = 13.sp,
                color = TablerSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card Input Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TablerBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isOtpSent) {
                        // Form Input Telepon & Nama (Daftar/Masuk)
                        Text(
                            text = "Masuk / Registrasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TablerDark
                        )

                        // Input Nama
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Nama Lengkap (Sesuai KTP)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TablerDark
                            )
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = { Text("Contoh: Ahmad Budiman", fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Nama",
                                        tint = TablerSecondary
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_name_input"),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TablerBlue,
                                    unfocusedBorderColor = TablerBorder
                                )
                            )
                        }

                        // Input Nomor Telepon
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Nomor WhatsApp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TablerDark
                            )
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { 
                                    // Hanya izinkan angka
                                    phoneInput = it.filter { char -> char.isDigit() } 
                                },
                                placeholder = { Text("Contoh: 08123456789", fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "WhatsApp",
                                        tint = TablerSecondary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_phone_input"),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TablerBlue,
                                    unfocusedBorderColor = TablerBorder
                                )
                            )
                        }

                        // Pesan Error jika ada
                        if (otpError != null) {
                            Text(
                                text = otpError ?: "",
                                color = TablerDanger,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Tombol Kirim OTP via WhatsApp
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.sendWhatsAppOtp(nameInput, phoneInput)
                            },
                            enabled = phoneInput.isNotBlank() && !isVerifying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TablerSuccess,
                                contentColor = Color.White,
                                disabledContainerColor = TablerSecondary.copy(alpha = 0.3f)
                            )
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send OTP",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Kirim Kode OTP via WhatsApp",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Form Input OTP
                        Text(
                            text = "Masukkan Kode Verifikasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TablerDark
                        )

                        Text(
                            text = "Kode OTP 6-digit dikirim via WhatsApp ke nomor $phoneInput. Silakan periksa pesan masuk Anda.",
                            fontSize = 12.sp,
                            color = TablerSecondary
                        )

                        // Input OTP
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { 
                                if (it.length <= 6) {
                                    otpInput = it.filter { char -> char.isDigit() }
                                }
                            },
                            placeholder = { Text("------", fontSize = 18.sp, textAlign = TextAlign.Center) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 8.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_otp_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TablerBlue,
                                unfocusedBorderColor = TablerBorder
                            )
                        )

                        // Pesan Error jika ada
                        if (otpError != null) {
                            Text(
                                text = otpError ?: "",
                                color = TablerDanger,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Tombol Verifikasi
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                val success = viewModel.verifyOtp(otpInput)
                                if (!success) {
                                    otpInput = ""
                                }
                            },
                            enabled = otpInput.length == 6 && !isVerifying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("verify_otp_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TablerBlue,
                                contentColor = Color.White
                            )
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = "Verifikasi & Masuk",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Info hitung mundur & Kirim Ulang OTP
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (resendCountdown > 0) {
                                Text(
                                    text = "Kirim ulang OTP dalam $resendCountdown detik",
                                    fontSize = 12.sp,
                                    color = TablerSecondary
                                )
                            } else {
                                Text(
                                    text = "Tidak menerima kode?",
                                    fontSize = 12.sp,
                                    color = TablerSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Kirim Ulang",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TablerBlue,
                                    modifier = Modifier.clickable {
                                        viewModel.sendWhatsAppOtp(nameInput, phoneInput)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Jaminan Kerahasiaan Data
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Aman",
                    tint = TablerSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Data KTP & Kontak dilindungi end-to-end.",
                    fontSize = 11.sp,
                    color = TablerSecondary
                )
            }
        }

        // =========================================================================
        // SIMULASI NOTIFIKASI WHATSAPP UNTUK PENGUJIAN WEB PREVIEW (SANGAT MEMBANTU)
        // =========================================================================
        AnimatedVisibility(
            visible = isOtpSent,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        otpInput = "123456" // Auto-fill saat di-klik untuk memudahkan pengujian
                    },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF075E54)), // Hijau WhatsApp asli
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "WhatsApp Sim",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Simulasi WhatsApp Gateway",
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Halo $nameInput, Kode OTP Anda adalah: 123456",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "(Klik card ini untuk memasukkan otomatis)",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}
