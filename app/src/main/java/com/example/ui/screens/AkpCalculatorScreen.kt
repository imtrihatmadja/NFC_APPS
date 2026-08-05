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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.viewmodel.NfcViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Model Data untuk Item Premi Hasil Tangkap
data class PremiItem(
    val id: String = UUID.randomUUID().toString(),
    var jenis: String,
    var tarifPerKg: Double,
    var jumlahKg: Double
) {
    val subtotal: Double get() = tarifPerKg * jumlahKg
}

// Model Data untuk Komponen Biaya Operasional Kapal (Opex)
data class OpexItem(
    val id: String = UUID.randomUUID().toString(),
    var nama: String,
    var kategori: String,
    var jumlahRp: Double
)

// Model Data untuk Pembelian Grosiran ABK
data class GrosiranItem(
    val id: String = UUID.randomUUID().toString(),
    var nama: String,
    var modalPerUnit: Double,
    var jualPerUnit: Double,
    var qty: Double
) {
    val subtotalModal: Double get() = modalPerUnit * qty
    val subtotalJual: Double get() = jualPerUnit * qty
    val totalMarkup: Double get() = subtotalJual - subtotalModal
    val markupPct: Double get() = if (modalPerUnit > 0) ((jualPerUnit - modalPerUnit) / modalPerUnit) * 100 else 0.0
}

// Data Provinsi UMP Referensi 2026
data class ProvinsiUmp(val nama: String, val ump: Double)

val DAFTAR_PROVINSI_UMP = listOf(
    ProvinsiUmp("DKI Jakarta", 3800000.0),
    ProvinsiUmp("Kepulauan Riau", 3562000.0),
    ProvinsiUmp("Jawa Timur", 3100000.0),
    ProvinsiUmp("Jawa Tengah", 2970677.0),
    ProvinsiUmp("Bali", 3527000.0),
    ProvinsiUmp("Nusa Tenggara Timur", 2000000.0),
    ProvinsiUmp("Sulawesi Utara", 3623778.0),
    ProvinsiUmp("Maluku", 3200000.0),
    ProvinsiUmp("Papua", 4033000.0)
)

// Data Jabatan & Bobot Bagi Hasil
data class JabatanInfo(val key: String, val label: String, val bobot: Double)

val DAFTAR_JABATAN = listOf(
    JabatanInfo("nakhoda", "Nakhoda", 1.8),
    JabatanInfo("mualim", "Mualim I", 1.5),
    JabatanInfo("kkg", "Kepala Kamar Mesin (KKG)", 1.5),
    JabatanInfo("abk_senior", "ABK Senior / Juru Mudi", 1.2),
    JabatanInfo("abk", "ABK Biasa", 1.0),
    JabatanInfo("juru_masak", "Juru Masak", 1.1),
    JabatanInfo("magang", "Magang / ABK Pemula", 0.85)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AkpCalculatorScreen(
    viewModel: NfcViewModel,
    onBackClick: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Kalkulator, 1: Standar Nilai, 2: Cara Pakai

    // ==========================================
    // STATE FORM KALKULATOR
    // ==========================================
    var namaAkp by remember { mutableStateOf("") }
    var selectedJabatan by remember { mutableStateOf(DAFTAR_JABATAN[3]) } // ABK Senior
    var selectedProvinsi by remember { mutableStateOf(DAFTAR_PROVINSI_UMP[3]) } // Jateng
    var namaKapal by remember { mutableStateOf("") }

    // Sistem Upah
    var systemType by remember { mutableStateOf("tetap") } // "tetap" or "bagi"
    var durasiTripHari by remember { mutableStateOf("30") }
    var gajiPokokText by remember { mutableStateOf("3500000") }

    // Bagi Hasil
    var nilaiTangkapanText by remember { mutableStateOf("50000000") }
    var proporsiKruPctText by remember { mutableStateOf("40") }
    var crewCounts by remember {
        mutableStateOf(
            mapOf(
                "nakhoda" to 1,
                "mualim" to 1,
                "kkg" to 1,
                "abk_senior" to 2,
                "abk" to 5,
                "juru_masak" to 1,
                "magang" to 0
            )
        )
    }

    // Komponen Upah Tambahan
    var tunjanganBerlayarText by remember { mutableStateOf("") }
    var premiList by remember {
        mutableStateOf(
            listOf(
                PremiItem(jenis = "Ikan Tuna", tarifPerKg = 2500.0, jumlahKg = 200.0),
                PremiItem(jenis = "Cumi-cumi", tarifPerKg = 3000.0, jumlahKg = 80.0)
            )
        )
    }
    var uangLemburText by remember { mutableStateOf("0") }
    var premiLainText by remember { mutableStateOf("0") }

    // Biaya Operasional Kapal (Opex)
    var opexList by remember {
        mutableStateOf(
            listOf(
                OpexItem(nama = "BBM / Solar", kategori = "bbm", jumlahRp = 15000000.0),
                OpexItem(nama = "Makanan & Minuman Kru", kategori = "logistik", jumlahRp = 5000000.0),
                OpexItem(nama = "APD & Keselamatan", kategori = "keselamatan", jumlahRp = 1500000.0),
                OpexItem(nama = "Surat Izin & Logbook", kategori = "admin", jumlahRp = 750000.0)
            )
        )
    }

    // Potongan
    var bpjsTkMode by remember { mutableStateOf("pu") } // "pu", "bpu", "tidak"
    var bpjsKesMode by remember { mutableStateOf("ada") } // "ada", "tidak"
    var cicilanKasbonText by remember { mutableStateOf("500000") }
    var biayaRekrutmenText by remember { mutableStateOf("0") }

    // Grosiran ABK
    var grosiranList by remember {
        mutableStateOf(
            listOf(
                GrosiranItem(nama = "Beras 5kg", modalPerUnit = 70000.0, jualPerUnit = 90000.0, qty = 2.0),
                GrosiranItem(nama = "Rokok (slop)", modalPerUnit = 200000.0, jualPerUnit = 280000.0, qty = 1.0)
            )
        )
    }

    // Dialog Cetak Slip
    var showSlipModal by remember { mutableStateOf(false) }

    // ==========================================
    // PERHITUNGAN MATEMATIKA REAL-TIME
    // ==========================================
    val durasiHari = durasiTripHari.toDoubleOrNull() ?: 30.0
    val umpVal = selectedProvinsi.ump

    // 1. Bagi Hasil Computation
    val nilaiTangkapan = nilaiTangkapanText.toDoubleOrNull() ?: 0.0
    val proporsiKruPct = proporsiKruPctText.toDoubleOrNull() ?: 40.0
    val poolBagiHasil = nilaiTangkapan * (proporsiKruPct / 100.0)

    var totalBobotKru = 0.0
    var totalJumlahKru = 0
    DAFTAR_JABATAN.forEach { j ->
        val count = crewCounts[j.key] ?: 0
        totalJumlahKru += count
        totalBobotKru += j.bobot * count
    }

    val bagianAKPBagiHasil = if (totalBobotKru > 0) {
        poolBagiHasil * (selectedJabatan.bobot / totalBobotKru)
    } else 0.0

    // Gaji Pokok Efektif
    val gajiPokokVal = if (systemType == "bagi") bagianAKPBagiHasil else (gajiPokokText.toDoubleOrNull() ?: 0.0)

    // Tunjangan Berlayar (Standard min 3% * gajiPokok * durasiHari)
    val tunjanganBerlayarVal = tunjanganBerlayarText.toDoubleOrNull() ?: 0.0
    val tunjanganMinStandard = gajiPokokVal * 0.03 * durasiHari
    val adaTunjanganInput = tunjanganBerlayarText.isNotBlank() && tunjanganBerlayarVal > 0

    // Premi Hasil Tangkap Total
    val totalPremiVal = premiList.sumOf { it.subtotal }
    val totalPremiKg = premiList.sumOf { it.jumlahKg }

    val uangLemburVal = uangLemburText.toDoubleOrNull() ?: 0.0
    val premiLainVal = premiLainText.toDoubleOrNull() ?: 0.0

    // Pendapatan Kotor
    val totalKotorVal = gajiPokokVal + tunjanganBerlayarVal + totalPremiVal + uangLemburVal + premiLainVal

    // Potongan BPJS TK (Worker portion)
    val bpjsTkWorkerVal = when (bpjsTkMode) {
        "pu" -> (gajiPokokVal * 0.02) + (gajiPokokVal * 0.01) // JHT 2% + JP 1%
        "bpu" -> {
            val bulan = durasiHari / 30.0
            val jkk = (gajiPokokVal * 0.005).coerceIn(10000.0, 103500.0)
            val jkm = 3400.0
            val jht = (gajiPokokVal * 0.02).coerceIn(20000.0, 414000.0)
            (jkk + jkm + jht) * bulan
        }
        else -> 0.0
    }

    // Potongan BPJS Kes (Worker portion 1%)
    val bpjsKesWorkerVal = if (bpjsKesMode == "ada") gajiPokokVal * 0.01 else 0.0

    val cicilanKasbonVal = cicilanKasbonText.toDoubleOrNull() ?: 0.0
    val biayaRekrutmenVal = biayaRekrutmenText.toDoubleOrNull() ?: 0.0

    // Grosiran ABK Totals
    val totalGrosiranModal = grosiranList.sumOf { it.subtotalModal }
    val totalGrosiranJual = grosiranList.sumOf { it.subtotalJual }
    val totalGrosiranMarkup = totalGrosiranJual - totalGrosiranModal
    val grosiranMarkupPct = if (totalGrosiranModal > 0) (totalGrosiranMarkup / totalGrosiranModal) * 100.0 else 0.0

    // Opex Totals
    val totalOpexVal = opexList.sumOf { it.jumlahRp }
    val opexPerHari = if (durasiHari > 0) totalOpexVal / durasiHari else 0.0
    val opexPerAKP = if (totalJumlahKru > 0) totalOpexVal / totalJumlahKru else totalOpexVal

    // Total Potongan & Net Income
    val totalPotonganVal = bpjsTkWorkerVal + bpjsKesWorkerVal + cicilanKasbonVal + biayaRekrutmenVal + totalGrosiranJual
    val netIncomeVal = totalKotorVal - totalPotonganVal
    val netIncomePerHari = if (durasiHari > 0) netIncomeVal / durasiHari else 0.0

    // Indikator Kepatuhan (8 Poin)
    val isGajiLayak = gajiPokokVal >= umpVal
    val isBiayaRekrutmenNol = biayaRekrutmenVal == 0.0
    val isTunjanganValid = !adaTunjanganInput || (tunjanganBerlayarVal >= tunjanganMinStandard)
    val isBpjsTkTerdaftar = bpjsTkMode != "tidak"
    val isBpjsKesTerdaftar = bpjsKesMode == "ada"
    val isKasbonWajar = cicilanKasbonVal <= (totalKotorVal * 0.3)
    val isGrosiranWajar = totalGrosiranJual == 0.0 || grosiranMarkupPct <= 20.0
    val isNetPositif = netIncomeVal > 0.0

    val allCompliancePassed = isGajiLayak && isBiayaRekrutmenNol && isTunjanganValid &&
            isBpjsTkTerdaftar && isBpjsKesTerdaftar && isKasbonWajar && isGrosiranWajar && isNetPositif

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = TablerDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Kalkulator Upah Layak AKP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TablerDark
                        )
                    }

                    Surface(
                        color = TablerBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "Permen KP No.4/2026",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TablerBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (activeTab == 0) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, TablerBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Estimasi Bersih:", fontSize = 11.sp, color = TablerSecondary)
                                Surface(
                                    color = if (allCompliancePassed) TablerSuccess.copy(alpha = 0.12f) else TablerDanger.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (allCompliancePassed) "✓ 8/8 Lulus" else "⚠️ Catatan Hukum",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (allCompliancePassed) TablerSuccess else TablerDanger,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = formatRupiah(netIncomeVal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TablerBlue
                            )
                        }
                        Button(
                            onClick = { showSlipModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cetak Slip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = TablerLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector (Compact Height)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = TablerBlue,
                modifier = Modifier.height(34.dp),
                divider = { HorizontalDivider(color = TablerBorder) }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier.height(34.dp),
                    text = { Text("🧮 Kalkulator", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.height(34.dp),
                    text = { Text("📋 Standar Nilai", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    modifier = Modifier.height(34.dp),
                    text = { Text("📖 Cara Pakai", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            when (activeTab) {
                0 -> {
                    // MAIN CALCULATOR TAB
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Profil Pekerjaan Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder.copy(alpha = 0.8f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SectionHeader(
                                        icon = Icons.Default.Person,
                                        title = "Profil Pekerjaan AKP",
                                        stepNumber = "LANGKAH 1 DARI 7"
                                    )

                                    OutlinedTextField(
                                        value = namaAkp,
                                        onValueChange = { namaAkp = it },
                                        label = { Text("Nama Awak Kapal (AKP)") },
                                        placeholder = { Text("cth. Budi Santoso") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Dropdown Jabatan
                                    var expandedJabatan by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = expandedJabatan,
                                        onExpandedChange = { expandedJabatan = !expandedJabatan }
                                    ) {
                                        OutlinedTextField(
                                            value = "${selectedJabatan.label} (${selectedJabatan.bobot}x)",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Jabatan Kapal") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJabatan) },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expandedJabatan,
                                            onDismissRequest = { expandedJabatan = false }
                                        ) {
                                            DAFTAR_JABATAN.forEach { j ->
                                                DropdownMenuItem(
                                                    text = { Text("${j.label} (${j.bobot}x bobot)") },
                                                    onClick = {
                                                        selectedJabatan = j
                                                        expandedJabatan = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Dropdown Provinsi UMP
                                    var expandedProvinsi by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = expandedProvinsi,
                                        onExpandedChange = { expandedProvinsi = !expandedProvinsi }
                                    ) {
                                        OutlinedTextField(
                                            value = "${selectedProvinsi.nama} — ${formatRupiah(selectedProvinsi.ump)}",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Provinsi Operasional (Ref. UMP)") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvinsi) },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expandedProvinsi,
                                            onDismissRequest = { expandedProvinsi = false }
                                        ) {
                                            DAFTAR_PROVINSI_UMP.forEach { p ->
                                                DropdownMenuItem(
                                                    text = { Text("${p.nama} — ${formatRupiah(p.ump)}") },
                                                    onClick = {
                                                        selectedProvinsi = p
                                                        expandedProvinsi = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = namaKapal,
                                        onValueChange = { namaKapal = it },
                                        label = { Text("Nama Kapal Perikanan") },
                                        placeholder = { Text("cth. KM Sumber Rejeki") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // 2. Sistem Upah & Trip Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder.copy(alpha = 0.8f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SectionHeader(
                                        icon = Icons.Default.Payments,
                                        title = "Sistem Pengupahan & Trip",
                                        stepNumber = "LANGKAH 2 DARI 7"
                                    )

                                    Text(
                                        text = "Skema Pembayaran Upah",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TablerDark
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Option 1: Gaji Tetap
                                        SelectableCardOption(
                                            title = "Gaji Tetap",
                                            subtitle = "Bulanan per trip",
                                            isSelected = systemType == "tetap",
                                            onClick = { systemType = "tetap" },
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Option 2: Bagi Hasil
                                        SelectableCardOption(
                                            title = "Bagi Hasil",
                                            subtitle = "Profit sharing",
                                            isSelected = systemType == "bagi",
                                            onClick = { systemType = "bagi" },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = durasiTripHari,
                                        onValueChange = { durasiTripHari = it },
                                        label = { Text("Durasi Trip (Hari)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (systemType == "tetap") {
                                        OutlinedTextField(
                                            value = gajiPokokText,
                                            onValueChange = { gajiPokokText = it },
                                            label = { Text("Gaji Pokok / Bulan (Rp)") },
                                            prefix = { Text("Rp ") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        if (!isGajiLayak && gajiPokokVal > 0) {
                                            WarningBanner(
                                                text = "⚠️ Gaji pokok (${formatRupiah(gajiPokokVal)}) di bawah UMP ${selectedProvinsi.nama} (${formatRupiah(umpVal)})! Tidak memenuhi standar minimum."
                                            )
                                        }
                                    } else {
                                        // Input Bagi Hasil
                                        OutlinedTextField(
                                            value = nilaiTangkapanText,
                                            onValueChange = { nilaiTangkapanText = it },
                                            label = { Text("Nilai Tangkapan Bersih Kapal / Trip (Rp)") },
                                            prefix = { Text("Rp ") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        OutlinedTextField(
                                            value = proporsiKruPctText,
                                            onValueChange = { proporsiKruPctText = it },
                                            label = { Text("Proporsi Pool Total Kru (%)") },
                                            suffix = { Text("%") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        // Summary Bagi Hasil Calculation
                                        Surface(
                                            color = TablerBlue.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, TablerBlue.copy(alpha = 0.2f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                SummaryRow("Total Kru Kapal", "$totalJumlahKru orang")
                                                SummaryRow("Total Bobot Kru", String.format("%.2f", totalBobotKru))
                                                SummaryRow("Pool Bagi Hasil Kru", formatRupiah(poolBagiHasil))
                                                HorizontalDivider(color = TablerBlue.copy(alpha = 0.2f))
                                                SummaryRow(
                                                    label = "Bagian AKP (${selectedJabatan.label})",
                                                    value = formatRupiah(bagianAKPBagiHasil),
                                                    isBold = true,
                                                    valueColor = TablerBlue
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Komponen Upah Tambahan Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder.copy(alpha = 0.8f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SectionHeader(
                                        icon = Icons.Default.TrendingUp,
                                        title = "Komponen Upah Tambahan",
                                        stepNumber = "LANGKAH 3 DARI 7"
                                    )

                                    OutlinedTextField(
                                        value = tunjanganBerlayarText,
                                        onValueChange = { tunjanganBerlayarText = it },
                                        label = { Text("Tunjangan Berlayar (Rp) — Opsional") },
                                        placeholder = { Text("Kosong / 0 = Tidak ada") },
                                        prefix = { Text("Rp ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    InfoBanner(
                                        text = "ℹ️ Standard minimum tunjangan berlayar Permen KP No.4/2026: 3% × Gaji Pokok × $durasiHari hari = ${formatRupiah(tunjanganMinStandard)}"
                                    )

                                    Text(
                                        text = "Premi Hasil Tangkap (Per kg)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TablerDark
                                    )

                                    premiList.forEachIndexed { index, item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = item.jenis,
                                                onValueChange = { newName ->
                                                    premiList = premiList.toMutableList().apply {
                                                        this[index] = item.copy(jenis = newName)
                                                    }
                                                },
                                                placeholder = { Text("Jenis Ikan") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1.2f)
                                            )

                                            OutlinedTextField(
                                                value = if (item.tarifPerKg > 0) item.tarifPerKg.toInt().toString() else "",
                                                onValueChange = { newTarif ->
                                                    val valTarif = newTarif.toDoubleOrNull() ?: 0.0
                                                    premiList = premiList.toMutableList().apply {
                                                        this[index] = item.copy(tarifPerKg = valTarif)
                                                    }
                                                },
                                                placeholder = { Text("Rp/kg") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )

                                            OutlinedTextField(
                                                value = if (item.jumlahKg > 0) item.jumlahKg.toInt().toString() else "",
                                                onValueChange = { newKg ->
                                                    val valKg = newKg.toDoubleOrNull() ?: 0.0
                                                    premiList = premiList.toMutableList().apply {
                                                        this[index] = item.copy(jumlahKg = valKg)
                                                    }
                                                },
                                                placeholder = { Text("kg") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.weight(0.9f)
                                            )

                                            IconButton(
                                                onClick = {
                                                    if (premiList.size > 1) {
                                                        premiList = premiList.filterIndexed { i, _ -> i != index }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus",
                                                    tint = TablerDanger
                                                )
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            premiList = premiList + PremiItem(jenis = "Ikan Lain", tarifPerKg = 2000.0, jumlahKg = 50.0)
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Tambah Jenis Tangkapan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedTextField(
                                        value = uangLemburText,
                                        onValueChange = { uangLemburText = it },
                                        label = { Text("Uang Lembur (Rp)") },
                                        prefix = { Text("Rp ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = premiLainText,
                                        onValueChange = { premiLainText = it },
                                        label = { Text("Premi / Pendapatan Lain (Rp)") },
                                        prefix = { Text("Rp ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // 4. Biaya Operasional Kapal (Tanggungan Perusahaan) Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder.copy(alpha = 0.8f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SectionHeader(
                                        icon = Icons.Default.DirectionsBoat,
                                        title = "Biaya Operasional Kapal",
                                        stepNumber = "LANGKAH 4 DARI 7"
                                    )

                                    Text(
                                        text = "Biaya operasional kapal (BBM, perbekalan/logistik, APD keselamatan, dokumen) adalah 100% tanggungan pemilik kapal sesuai Permen KP No. 4/2026 — dilarang dipotong dari upah AKP.",
                                        fontSize = 11.sp,
                                        color = TablerSecondary,
                                        lineHeight = 16.sp
                                    )

                                    opexList.forEachIndexed { index, item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = item.nama,
                                                onValueChange = { newName ->
                                                    opexList = opexList.toMutableList().apply {
                                                        this[index] = item.copy(nama = newName)
                                                    }
                                                },
                                                placeholder = { Text("Nama Komponen") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1.5f)
                                            )

                                            OutlinedTextField(
                                                value = if (item.jumlahRp > 0) item.jumlahRp.toLong().toString() else "",
                                                onValueChange = { newRp ->
                                                    val valRp = newRp.toDoubleOrNull() ?: 0.0
                                                    opexList = opexList.toMutableList().apply {
                                                        this[index] = item.copy(jumlahRp = valRp)
                                                    }
                                                },
                                                placeholder = { Text("Jumlah (Rp)") },
                                                prefix = { Text("Rp ") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.weight(1.5f)
                                            )

                                            IconButton(
                                                onClick = {
                                                    if (opexList.size > 1) {
                                                        opexList = opexList.filterIndexed { i, _ -> i != index }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus",
                                                    tint = TablerDanger
                                                )
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            opexList = opexList + OpexItem(nama = "Biaya Lainnya", kategori = "lain", jumlahRp = 500000.0)
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Tambah Komponen Biaya Operasional", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Surface(
                                        color = TablerLight,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, TablerBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            SummaryRow("Total Biaya Operasional / Trip", formatRupiah(totalOpexVal), isBold = true)
                                            SummaryRow("Biaya Operasional / Hari", formatRupiah(opexPerHari))
                                            SummaryRow("Estimasi Operasional / AKP", "${formatRupiah(opexPerAKP)} / orang")
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Potongan Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder.copy(alpha = 0.8f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SectionHeader(
                                        icon = Icons.Default.RemoveCircleOutline,
                                        title = "Potongan Upah & Kasbon",
                                        stepNumber = "LANGKAH 5 DARI 7"
                                    )

                                    // BPJS TK Selection
                                    Text("BPJS Ketenagakerjaan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SelectablePill(
                                            label = "PU (Penerima Upah)",
                                            isSelected = bpjsTkMode == "pu",
                                            onClick = { bpjsTkMode = "pu" },
                                            modifier = Modifier.weight(1f)
                                        )
                                        SelectablePill(
                                            label = "BPU (Mandiri)",
                                            isSelected = bpjsTkMode == "bpu",
                                            onClick = { bpjsTkMode = "bpu" },
                                            modifier = Modifier.weight(1f)
                                        )
                                        SelectablePill(
                                            label = "Tidak Ada",
                                            isSelected = bpjsTkMode == "tidak",
                                            onClick = { bpjsTkMode = "tidak" },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    if (bpjsTkMode == "pu") {
                                        InfoBanner("ℹ️ Dipotong dari upah AKP: JHT 2% + JP 1% = ${formatRupiah(bpjsTkWorkerVal)}. Tanggungan perusahaan (JHT 3.7%, JKK 1.74%, JKM 0.3%, JP 2%) tidak dipotong dari AKP.")
                                    }

                                    // BPJS Kesehatan Selection
                                    Text("BPJS Kesehatan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SelectablePill(
                                            label = "Ada (Terdaftar)",
                                            isSelected = bpjsKesMode == "ada",
                                            onClick = { bpjsKesMode = "ada" },
                                            modifier = Modifier.weight(1f)
                                        )
                                        SelectablePill(
                                            label = "Tidak Ada",
                                            isSelected = bpjsKesMode == "tidak",
                                            onClick = { bpjsKesMode = "tidak" },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    if (bpjsKesMode == "ada") {
                                        InfoBanner("ℹ️ Dipotong dari upah AKP: 1% (${formatRupiah(bpjsKesWorkerVal)}). Tanggungan perusahaan: 4% (${formatRupiah(gajiPokokVal * 0.04)}).")
                                    }

                                    OutlinedTextField(
                                        value = cicilanKasbonText,
                                        onValueChange = { cicilanKasbonText = it },
                                        label = { Text("Cicilan Kasbon Perusahaan (Rp)") },
                                        prefix = { Text("Rp ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (!isKasbonWajar && cicilanKasbonVal > 0) {
                                        WarningBanner("⚠️ Cicilan kasbon (${formatRupiah(cicilanKasbonVal)}) melebihi 30% pendapatan kotor (${formatRupiah(totalKotorVal * 0.3)})! Berisiko jerat utang.")
                                    }

                                    OutlinedTextField(
                                        value = biayaRekrutmenText,
                                        onValueChange = { biayaRekrutmenText = it },
                                        label = { Text("Biaya Rekrutmen ke Pekerja (Harus Rp 0)") },
                                        prefix = { Text("Rp ") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (!isBiayaRekrutmenNol) {
                                        WarningBanner("⚠️ Biaya rekrutmen yang dibebankan ke pekerja = ILEGAL (UU No.18/2017 Pasal 30). Seluruh biaya rekrutmen wajib ditanggung pemberi kerja.")
                                    }
                                }
                            }
                        }

                        // 6. Pembelian Grosiran ABK Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder.copy(alpha = 0.8f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SectionHeader(
                                        icon = Icons.Default.ShoppingBag,
                                        title = "Pembelian Grosiran ABK di Kapal",
                                        stepNumber = "LANGKAH 6 DARI 7"
                                    )

                                    Text(
                                        text = "Catat barang grosiran (sembako, rokok, dll.) yang dijual ke ABK selama trip. Kalkulator mendeteksi markup berlebih.",
                                        fontSize = 11.sp,
                                        color = TablerSecondary,
                                        lineHeight = 16.sp
                                    )

                                    grosiranList.forEachIndexed { index, item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = item.nama,
                                                onValueChange = { newName ->
                                                    grosiranList = grosiranList.toMutableList().apply {
                                                        this[index] = item.copy(nama = newName)
                                                    }
                                                },
                                                placeholder = { Text("Barang") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1.2f)
                                            )

                                            OutlinedTextField(
                                                value = if (item.modalPerUnit > 0) item.modalPerUnit.toLong().toString() else "",
                                                onValueChange = { newModal ->
                                                    val valModal = newModal.toDoubleOrNull() ?: 0.0
                                                    grosiranList = grosiranList.toMutableList().apply {
                                                        this[index] = item.copy(modalPerUnit = valModal)
                                                    }
                                                },
                                                placeholder = { Text("Modal") },
                                                prefix = { Text("Rp ") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.weight(1.1f)
                                            )

                                            OutlinedTextField(
                                                value = if (item.jualPerUnit > 0) item.jualPerUnit.toLong().toString() else "",
                                                onValueChange = { newJual ->
                                                    val valJual = newJual.toDoubleOrNull() ?: 0.0
                                                    grosiranList = grosiranList.toMutableList().apply {
                                                        this[index] = item.copy(jualPerUnit = valJual)
                                                    }
                                                },
                                                placeholder = { Text("Jual") },
                                                prefix = { Text("Rp ") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.weight(1.1f)
                                            )

                                            IconButton(
                                                onClick = {
                                                    if (grosiranList.size > 1) {
                                                        grosiranList = grosiranList.filterIndexed { i, _ -> i != index }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus",
                                                    tint = TablerDanger
                                                )
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            grosiranList = grosiranList + GrosiranItem(nama = "Kopi & Gula", modalPerUnit = 15000.0, jualPerUnit = 20000.0, qty = 1.0)
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Tambah Barang Grosiran", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Surface(
                                        color = TablerLight,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, TablerBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            SummaryRow("Nilai Modal Grosir", formatRupiah(totalGrosiranModal))
                                            SummaryRow("Total Dibayar ABK", formatRupiah(totalGrosiranJual), isBold = true)
                                            SummaryRow("Selisih Markup", "${formatRupiah(totalGrosiranMarkup)} (${String.format("%.1f", grosiranMarkupPct)}%)", valueColor = if (grosiranMarkupPct > 20) TablerDanger else TablerDark)
                                        }
                                    }

                                    if (grosiranMarkupPct > 20 && totalGrosiranJual > 0) {
                                        WarningBanner("⚠️ Markup grosiran (${String.format("%.1f", grosiranMarkupPct)}%) melebihi batas wajar 20%! Berpotensi dikategorikan jerat utang (debt bondage).")
                                    }
                                }
                            }
                        }

                        // 7. HASIL SIMULASI SLIP UPAH & STATUS KEPATUHAN CARD
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, TablerBlue)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SectionHeader(
                                        icon = Icons.Default.ReceiptLong,
                                        title = "Hasil Simulasi Slip Upah",
                                        stepNumber = "LANGKAH 7 DARI 7 (HASIL)"
                                    )
                                    Text(
                                        text = "${if (namaAkp.isBlank()) "AKP" else namaAkp} • ${selectedJabatan.label} • $durasiTripHari hari",
                                        fontSize = 11.sp,
                                        color = TablerSecondary
                                    )

                                    HorizontalDivider(color = TablerBorder)

                                    // PENDAPATAN KOTOR
                                    Text("Pendapatan Kotor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TablerDark)
                                    SummaryRow("Gaji Pokok / Bagi Hasil", formatRupiah(gajiPokokVal))
                                    SummaryRow("Tunjangan Berlayar", formatRupiah(tunjanganBerlayarVal))
                                    SummaryRow("Premi Hasil Tangkap (${totalPremiKg.toInt()} kg)", formatRupiah(totalPremiVal))
                                    SummaryRow("Uang Lembur & Lain-lain", formatRupiah(uangLemburVal + premiLainVal))
                                    HorizontalDivider(color = TablerBorder)
                                    SummaryRow("TOTAL PENDAPATAN KOTOR", formatRupiah(totalKotorVal), isBold = true, valueColor = TablerBlue)

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // POTONGAN
                                    Text("Potongan Upah", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TablerDark)
                                    SummaryRow("BPJS Ketenagakerjaan Pekerja", "- ${formatRupiah(bpjsTkWorkerVal)}")
                                    SummaryRow("BPJS Kesehatan Pekerja", "- ${formatRupiah(bpjsKesWorkerVal)}")
                                    SummaryRow("Cicilan Kasbon Perusahaan", "- ${formatRupiah(cicilanKasbonVal)}")
                                    if (totalGrosiranJual > 0) {
                                        SummaryRow("Pembelian Grosiran ABK", "- ${formatRupiah(totalGrosiranJual)}")
                                    }
                                    if (biayaRekrutmenVal > 0) {
                                        SummaryRow("Biaya Rekrutmen (ILEGAL)", "- ${formatRupiah(biayaRekrutmenVal)} ⚠️", valueColor = TablerDanger)
                                    }

                                    // PENDAPATAN BERSIH BOX
                                    Surface(
                                        color = TablerBlue.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, TablerBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "ESTIMASI PENDAPATAN BERSIH / TRIP",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TablerBlue
                                            )
                                            Text(
                                                text = formatRupiah(netIncomeVal),
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TablerBlue
                                            )
                                            Text(
                                                text = "≈ ${formatRupiah(netIncomePerHari)} / hari berlayar",
                                                fontSize = 11.sp,
                                                color = TablerSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // STATUS KEPATUHAN 8 POIN
                                    Text("Status Kepatuhan Hukum (8 Indikator)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TablerDark)

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        ComplianceCheckItem(
                                            isPassed = isGajiLayak,
                                            label = "Gaji pokok (${formatRupiah(gajiPokokVal)}) ≥ UMP ${selectedProvinsi.nama} (${formatRupiah(umpVal)})"
                                        )
                                        ComplianceCheckItem(
                                            isPassed = isBiayaRekrutmenNol,
                                            label = "Bebas biaya rekrutmen ke pekerja (Rp 0)"
                                        )
                                        ComplianceCheckItem(
                                            isPassed = isTunjanganValid,
                                            label = "Tunjangan berlayar sesuai standar minimum"
                                        )
                                        ComplianceCheckItem(
                                            isPassed = isBpjsTkTerdaftar,
                                            label = "BPJS Ketenagakerjaan terdaftar"
                                        )
                                        ComplianceCheckItem(
                                            isPassed = isBpjsKesTerdaftar,
                                            label = "BPJS Kesehatan terdaftar"
                                        )
                                        ComplianceCheckItem(
                                            isPassed = isKasbonWajar,
                                            label = "Cicilan kasbon ≤ 30% pendapatan kotor"
                                        )
                                        ComplianceCheckItem(
                                            isPassed = isGrosiranWajar,
                                            label = "Markup grosiran ≤ 20% harga modal"
                                        )
                                        ComplianceCheckItem(
                                            isPassed = isNetPositif,
                                            label = "Pendapatan bersih positif (> Rp 0)"
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { showSlipModal = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Print, contentDescription = "Cetak")
                                            Text("Pratinjau & Cetak Slip Simulasi", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // STANDAR NILAI REGULASI TAB
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Standar Nilai Rujukan Regulasi Indonesia & Internasional",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TablerDark
                            )
                            Text(
                                text = "Seluruh parameter perhitungan dalam kalkulator ini mengacu pada hukum dan konvensi perikanan yang berlaku:",
                                fontSize = 12.sp,
                                color = TablerSecondary
                            )
                        }

                        val references = listOf(
                            Pair("Gaji Pokok AKP Minimum", "≥ 1× UMP/UMK wilayah operasional (Permen KP No. 4/2026 & Permen KP No. 33/2021)"),
                            Pair("Tunjangan Berlayar Harian", "≥ 3% Gaji Pokok / hari berlayar (Permen KP No. 4/2026)"),
                            Pair("Bagi Hasil Tangkapan", "Berdasarkan kesepakatan tertulis proporsional bobot lokasi & jabatan (Permen KP No. 4/2026)"),
                            Pair("Biaya Rekrutmen & Penempatan", "Rp 0 — Dilarang membebankan biaya rekrutmen/penempatan ke AKP (Employer Pays Principle, Permen KP No. 4/2026 & UU No. 18/2017)"),
                            Pair("Biaya Operasional & Logistik", "100% Tanggungan Pemilik Kapal — BBM, logistik/pangan, APD keselamatan, & dokumen (Permen KP No. 4/2026 & ILO C.188)"),
                            Pair("BPJS Ketenagakerjaan Wajib", "Wajib terdaftar — JHT (2% pekerja, 3.7% pemberi kerja) & JP (1% pekerja, 2% pemberi kerja)"),
                            Pair("BPJS Kesehatan Wajib", "Wajib terdaftar — 1% pekerja, 4% pemberi kerja (Permen KP No. 4/2026)"),
                            Pair("Batas Potongan Kasbon Perusahaan", "Maksimal cicilan 30% dari pendapatan kotor per trip untuk mencegah jerat utang"),
                            Pair("Batas Markup Grosiran Laut", "Maksimal 20% di atas harga modal grosir toko kapal untuk perlindungan AKP"),
                            Pair("Jam Kerja & Istirahat Wajib", "Istirahat minimum 10 jam per 24 jam & jaminan keselamatan kerja (Permen KP No. 4/2026 & ILO C.188)")
                        )

                        items(references) { (title, desc) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TablerBlue)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = desc, fontSize = 12.sp, color = TablerDark, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // CARA PAKAI TAB
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Panduan Penggunaan Kalkulator Upah AKP",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TablerDark
                            )
                        }

                        val steps = listOf(
                            Pair("1. Lengkapi Profil Pekerjaan", "Masukkan nama AKP, pilih jabatan kapal, dan pilih provinsi wilayah operasional untuk menentukan acuan UMP."),
                            Pair("2. Pilih Sistem Upah & Durasi Trip", "Pilih apakah menggunakan skema Gaji Tetap atau Bagi Hasil. Masukkan estimasi durasi trip dalam jumlah hari."),
                            Pair("3. Tambahkan Premi & Tunjangan", "Masukkan premi hasil tangkap per kg dan tunjangan berlayar sesuai kesepakatan tertulis."),
                            Pair("4. Masukkan Potongan Kasbon & BPJS", "Pilih status kepesertaan BPJS Ketenagakerjaan dan Kesehatan serta masukkan jumlah cicilan kasbon jika ada."),
                            Pair("5. Catat Grosiran Kapal", "Masukkan barang grosiran yang dibeli ABK di laut untuk memverifikasi agar markup tidak melebihi batas wajar 20%."),
                            Pair("6. Tinjau Status Kepatuhan 8 Indikator", "Pastikan seluruh 8 indikator kepatuhan hukum berwarna hijau sebelum menandatangani Perjanjian Kerja Laut (PKL)."),
                            Pair("7. Cetak Slip Simulasi", "Gunakan tombol Pratinjau & Cetak Slip untuk menyimpan hasil simulasi sebagai bukti transparansi rekrutmen.")
                        )

                        items(steps) { (title, desc) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, TablerBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TablerDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = desc, fontSize = 12.sp, color = TablerSecondary, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG CETAK SLIP SIMULASI
    // ==========================================
    if (showSlipModal) {
        Dialog(
            onDismissRequest = { showSlipModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Preview Slip Simulasi Upah", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TablerDark)
                        IconButton(onClick = { showSlipModal = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    HorizontalDivider(color = TablerBorder)

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (namaKapal.isBlank()) "KM. PERIKANAN INDONESIA" else namaKapal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "SLIP SIMULASI UPAH — TRANSPARANSI REKRUTMEN AKP",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Tanggal: ${SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())}",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                HorizontalDivider(color = Color.LightGray, modifier = Modifier.padding(vertical = 6.dp))

                                Text("Nama AKP: ${if (namaAkp.isBlank()) "Budi Santoso" else namaAkp}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Jabatan: ${selectedJabatan.label}", fontSize = 11.sp)
                                Text("Durasi Trip: $durasiTripHari hari | Acuan UMP: ${selectedProvinsi.nama} (${formatRupiah(umpVal)})", fontSize = 11.sp)

                                HorizontalDivider(color = Color.LightGray, modifier = Modifier.padding(vertical = 6.dp))

                                Text("PENDAPATAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TablerBlue)
                                SummaryRow("Gaji Pokok / Bagi Hasil", formatRupiah(gajiPokokVal))
                                SummaryRow("Tunjangan Berlayar", formatRupiah(tunjanganBerlayarVal))
                                SummaryRow("Premi Hasil Tangkap (${totalPremiKg.toInt()} kg)", formatRupiah(totalPremiVal))
                                SummaryRow("Uang Lembur & Lain", formatRupiah(uangLemburVal + premiLainVal))
                                SummaryRow("TOTAL PENDAPATAN KOTOR", formatRupiah(totalKotorVal), isBold = true)

                                HorizontalDivider(color = Color.LightGray, modifier = Modifier.padding(vertical = 6.dp))

                                Text("POTONGAN UPAH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TablerDanger)
                                SummaryRow("BPJS Ketenagakerjaan Pekerja", "- ${formatRupiah(bpjsTkWorkerVal)}")
                                SummaryRow("BPJS Kesehatan Pekerja", "- ${formatRupiah(bpjsKesWorkerVal)}")
                                SummaryRow("Cicilan Kasbon Perusahaan", "- ${formatRupiah(cicilanKasbonVal)}")
                                if (totalGrosiranJual > 0) {
                                    SummaryRow("Grosiran ABK di Kapal", "- ${formatRupiah(totalGrosiranJual)}")
                                }
                                if (biayaRekrutmenVal > 0) {
                                    SummaryRow("Biaya Rekrutmen (ILEGAL)", "- ${formatRupiah(biayaRekrutmenVal)}", valueColor = TablerDanger)
                                }

                                HorizontalDivider(color = Color.LightGray, modifier = Modifier.padding(vertical = 6.dp))

                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("ESTIMASI PENDAPATAN BERSIH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                        Text(formatRupiah(netIncomeVal), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Surface(
                                    color = if (allCompliancePassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (allCompliancePassed) "✅ MEMENUHI STANDAR FAIR REKRUTMEN & UPAH LAYAK" else "❌ TERDAPAT INDIKATOR KETIDAKPATUHAN HUKUM — PERLU REVISI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (allCompliancePassed) Color(0xFF2E7D32) else TablerDanger,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Dokumen ini adalah simulasi transparansi remunerasi sebelum PKL ditandatangani.\nDasar Hukum: Permen KP No.4/2026 • Permen KP No.33/2021 • UU No.18/2017 • ILO C.188",
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showSlipModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = TablerBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tutup Preview", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// HELPER COMPOSABLE COMPONENTS
// ==========================================

@Composable
fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    stepNumber: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(TablerBlue.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TablerBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            if (stepNumber != null) {
                Text(
                    text = stepNumber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TablerBlue,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TablerDark
            )
        }
    }
}

@Composable
fun SelectableCardOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) TablerBlue.copy(alpha = 0.08f) else Color.White,
        border = BorderStroke(2.dp, if (isSelected) TablerBlue else TablerBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = TablerBlue)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) TablerBlue else TablerDark
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TablerSecondary
                )
            }
        }
    }
}

@Composable
fun SelectablePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) TablerBlue else Color.White,
        border = BorderStroke(1.dp, if (isSelected) TablerBlue else TablerBorder),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else TablerDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = TablerDark
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 13.sp else 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) TablerDark else TablerSecondary
        )
        Text(
            text = value,
            fontSize = if (isBold) 13.sp else 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
fun WarningBanner(text: String) {
    Surface(
        color = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Peringatan",
                tint = TablerDanger,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                color = TablerDanger,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun InfoBanner(text: String) {
    Surface(
        color = Color(0xFFE1F5FE),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFB3E5FC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Informasi",
                tint = Color(0xFF0288D1),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                color = Color(0xFF01579B),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ComplianceCheckItem(isPassed: Boolean, label: String) {
    Surface(
        color = if (isPassed) TablerSuccess.copy(alpha = 0.06f) else TablerDanger.copy(alpha = 0.06f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isPassed) TablerSuccess.copy(alpha = 0.2f) else TablerDanger.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = if (isPassed) "Lulus" else "Gagal",
                tint = if (isPassed) TablerSuccess else TablerDanger,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (isPassed) TablerDark else TablerDanger,
                fontWeight = if (isPassed) FontWeight.Medium else FontWeight.Bold
            )
        }
    }
}

fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}
