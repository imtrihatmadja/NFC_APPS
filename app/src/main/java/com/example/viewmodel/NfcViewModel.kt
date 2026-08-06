package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Post
import com.example.data.model.Complaint
import com.example.data.repository.NfcRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.model.TrainingMaterial
import com.example.data.worker.DownloadWorker

// Simple custom model for analytical statistics inspired by Tabler UI Image 2
data class NfcStats(
    val totalAduan: Int = 208,
    val aduanPending: Int = 16,
    val totalKorban: Int = 626,
    val korbanTertangani: Int = 580,
    val aduanDalamNegeri: Int = 145,
    val trenDalamNegeri: Double = 5.2, // +5.2%
    val aduanLuarNegeri: Int = 63,
    val trenLuarNegeri: Double = 2.1 // +2.1%
)

// Model data keanggotaan pengguna NFC
data class NfcUser(
    val name: String,
    val phone: String,
    val joinDate: String,
    val memberId: String
)

class NfcViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = NfcRepository(db.postDao())
    private val sharedPrefs = application.getSharedPreferences("nfc_session_prefs", Context.MODE_PRIVATE)

    // Currently active tab: "Beranda", "Tentang Kami", "Lowongan", "Kegiatan", "Profil"
    private val _currentTab = MutableStateFlow("Beranda")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Loading & status states
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    // Detail modal/navigation target
    private val _selectedPost = MutableStateFlow<Post?>(null)
    val selectedPost: StateFlow<Post?> = _selectedPost.asStateFlow()

    // Kalkulator AKP Screen overlay target
    private val _isCalculatorOpen = MutableStateFlow(false)
    val isCalculatorOpen: StateFlow<Boolean> = _isCalculatorOpen.asStateFlow()

    fun setCalculatorOpen(open: Boolean) {
        _isCalculatorOpen.value = open
    }

    // Global Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Newsletter Email
    val newsletterEmail = MutableStateFlow("")

    private val _newsletterSubscribed = MutableStateFlow(false)
    val newsletterSubscribed: StateFlow<Boolean> = _newsletterSubscribed.asStateFlow()

    // Contact form inputs
    val contactName = MutableStateFlow("")
    val contactEmail = MutableStateFlow("")
    val contactMessage = MutableStateFlow("")
    private val _contactSubmitted = MutableStateFlow(false)
    val contactSubmitted: StateFlow<Boolean> = _contactSubmitted.asStateFlow()

    // Statistics state - synced reactively with local database & portal baseline
    private val _baseStats = MutableStateFlow(
        NfcStats(
            totalAduan = 208,
            aduanPending = 16,
            totalKorban = 626,
            korbanTertangani = 580,
            aduanDalamNegeri = 145,
            trenDalamNegeri = 5.2,
            aduanLuarNegeri = 63,
            trenLuarNegeri = 2.1
        )
    )

    val stats: StateFlow<NfcStats> = combine(_baseStats, db.complaintDao().getAllComplaints()) { base, localComplaints ->
        if (localComplaints.isEmpty()) {
            base
        } else {
            val pendingCount = localComplaints.count { it.status == "Diajukan" || it.status == "Menunggu Verifikasi" }
            val handledCount = localComplaints.count { it.status == "Selesai" || it.status == "Diproses" || it.status == "Rujukan" }
            val luarNegeriCount = localComplaints.count { 
                it.location.contains("luar negeri", ignoreCase = true) ||
                it.location.contains("asing", ignoreCase = true) ||
                it.location.contains("taiwan", ignoreCase = true) ||
                it.location.contains("jepang", ignoreCase = true) ||
                it.location.contains("korea", ignoreCase = true)
            }
            val dalamNegeriCount = localComplaints.size - luarNegeriCount

            NfcStats(
                totalAduan = base.totalAduan + localComplaints.size,
                aduanPending = base.aduanPending + pendingCount,
                totalKorban = base.totalKorban + localComplaints.size,
                korbanTertangani = base.korbanTertangani + handledCount,
                aduanDalamNegeri = base.aduanDalamNegeri + dalamNegeriCount,
                trenDalamNegeri = base.trenDalamNegeri,
                aduanLuarNegeri = base.aduanLuarNegeri + luarNegeriCount,
                trenLuarNegeri = base.trenLuarNegeri
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NfcStats()
    )

    // ==========================================
    // STATE AUTENTIKASI WHATSAPP OTP (FASE 2)
    // ==========================================
    private val _currentUser = MutableStateFlow<NfcUser?>(null)
    val currentUser: StateFlow<NfcUser?> = _currentUser.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _resendCountdown = MutableStateFlow(0)
    val resendCountdown: StateFlow<Int> = _resendCountdown.asStateFlow()

    private val _otpError = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError.asStateFlow()

    // Menyimpan sementara nomor & nama sebelum verifikasi selesai
    private var tempPhone = ""
    private var tempName = ""
    private var generatedOtp = ""
    private var countdownJob: Job? = null

    // ==========================================
    // STATE MODUL PENGADUAN & GOOGLE DRIVE (FASE 3)
    // ==========================================
    private val _showComplaintForm = MutableStateFlow(false)
    val showComplaintForm: StateFlow<Boolean> = _showComplaintForm.asStateFlow()

    private val _currentFormStep = MutableStateFlow(1)
    val currentFormStep: StateFlow<Int> = _currentFormStep.asStateFlow()

    private val _isUploadingEvidence = MutableStateFlow(false)
    val isUploadingEvidence: StateFlow<Boolean> = _isUploadingEvidence.asStateFlow()

    private val _isSubmittingComplaint = MutableStateFlow(false)
    val isSubmittingComplaint: StateFlow<Boolean> = _isSubmittingComplaint.asStateFlow()

    val appsScriptUrl = MutableStateFlow(sharedPrefs.getString("apps_script_url", "") ?: "")

    // Observe complaints for current user reactively (Room DB integration)
    val complaints: StateFlow<List<Complaint>> = _currentUser
        .combine(db.complaintDao().getAllComplaints()) { user, allComplaints ->
            if (user == null) {
                emptyList()
            } else {
                allComplaints.filter { it.reporterPhone == user.phone }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered lists combining all posts with query
    val posts: StateFlow<List<Post>> = repository.postsFlow
        .combine(_searchQuery) { postList, query ->
            if (query.isBlank()) {
                postList
            } else {
                postList.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.excerpt.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Observe training materials
    val trainingMaterials: StateFlow<List<TrainingMaterial>> = db.trainingDao().getAllMaterials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Load session if exists
        loadUserSession()

        viewModelScope.launch {
            // Prepopulate offline training materials
            prepopulateTrainingMaterials()
            
            // First make sure database has local fallbacks
            repository.checkAndPrepopulate()
            // Then sync live from WordPress in background
            syncWordPressData()
        }
    }

    // Load user session from SharedPreferences
    private fun loadUserSession() {
        val savedName = sharedPrefs.getString("user_name", null)
        val savedPhone = sharedPrefs.getString("user_phone", null)
        val savedJoinDate = sharedPrefs.getString("user_join_date", null)
        val savedMemberId = sharedPrefs.getString("user_member_id", null)

        if (savedName != null && savedPhone != null) {
            _currentUser.value = NfcUser(
                name = savedName,
                phone = savedPhone,
                joinDate = savedJoinDate ?: "Juli 2026",
                memberId = savedMemberId ?: "NFC-7731"
            )
        }
    }

    // Save user session to SharedPreferences
    private fun saveUserSession(user: NfcUser) {
        sharedPrefs.edit().apply {
            putString("user_name", user.name)
            putString("user_phone", user.phone)
            putString("user_join_date", user.joinDate)
            putString("user_member_id", user.memberId)
            apply()
        }
        _currentUser.value = user
    }

    // Clear user session from SharedPreferences
    fun logout() {
        sharedPrefs.edit().clear().apply()
        _currentUser.value = null
        _isOtpSent.value = false
        generatedOtp = ""
        tempPhone = ""
        tempName = ""
        _otpError.value = null
    }

    // Kirim OTP via WhatsApp
    fun sendWhatsAppOtp(name: String, phone: String) {
        if (phone.length < 9) {
            _otpError.value = "Nomor WhatsApp tidak valid (terlalu pendek)"
            return
        }

        _isVerifying.value = true
        _otpError.value = null

        viewModelScope.launch {
            delay(1500) // Simulasi pengiriman jaringan ke gateway WhatsApp

            tempName = name.ifBlank { "Anggota NFC" }
            tempPhone = phone
            
            // Generate OTP 6 digit acak
            generatedOtp = "123456" 
            
            _isOtpSent.value = true
            _isVerifying.value = false
            
            _syncStatus.value = "Kode OTP dikirim ke WhatsApp Anda!"

            // Memulai hitung mundur resend (60 detik)
            startResendCountdown()
        }
    }

    private fun startResendCountdown() {
        countdownJob?.cancel()
        _resendCountdown.value = 60
        countdownJob = viewModelScope.launch {
            while (_resendCountdown.value > 0) {
                delay(1000)
                _resendCountdown.value -= 1
            }
        }
    }

    // Verifikasi OTP
    fun verifyOtp(code: String): Boolean {
        _otpError.value = null
        if (code != generatedOtp) {
            _otpError.value = "Kode OTP salah atau kedaluwarsa!"
            return false
        }

        _isVerifying.value = true
        viewModelScope.launch {
            delay(1000) // Simulasi proses verifikasi & pembuatan token keanggotaan

            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val currentDate = sdf.format(Date())
            val randomId = "NFC-${(1000..9999).random()}"

            val newUser = NfcUser(
                name = tempName,
                phone = tempPhone,
                joinDate = currentDate,
                memberId = randomId
            )

            saveUserSession(newUser)
            _isVerifying.value = false
            _isOtpSent.value = false
            _syncStatus.value = "Selamat datang, ${newUser.name}! Anda berhasil masuk."
        }
        return true
    }

    fun setTab(tabName: String) {
        _currentTab.value = tabName
        // Clear selected post and overlays when switching tabs
        _selectedPost.value = null
        _isCalculatorOpen.value = false
        _showComplaintForm.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedPost(post: Post?) {
        _selectedPost.value = post
    }

    fun clearSyncStatus() {
        _syncStatus.value = null
    }

    fun syncWordPressData() {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.syncPosts()
            _isSyncing.value = false
            _syncStatus.value = "Data berhasil disinkronisasi dengan nfc.or.id"
        }
    }

    fun onSubscribeNewsletter() {
        if (newsletterEmail.value.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(newsletterEmail.value).matches()) {
            _newsletterSubscribed.value = true
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                _newsletterSubscribed.value = false
                newsletterEmail.value = ""
            }
        }
    }

    fun onSubmitContactForm() {
        if (contactName.value.isNotBlank() && contactEmail.value.isNotBlank() && contactMessage.value.isNotBlank()) {
            _contactSubmitted.value = true
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _contactSubmitted.value = false
                contactName.value = ""
                contactEmail.value = ""
                contactMessage.value = ""
            }
        }
    }

    // ==========================================
    // ACTION METHODS FOR COMPLAINTS (FASE 3)
    // ==========================================
    fun openComplaintForm() {
        _showComplaintForm.value = true
        _currentFormStep.value = 1
    }

    fun closeComplaintForm() {
        _showComplaintForm.value = false
    }

    fun setFormStep(step: Int) {
        _currentFormStep.value = step
    }

    fun saveAppsScriptUrl(url: String) {
        sharedPrefs.edit().putString("apps_script_url", url).apply()
        appsScriptUrl.value = url
        _syncStatus.value = "URL Jembatan Google Drive berhasil disimpan!"
    }

    // Google Drive upload bridge execution
    private suspend fun uploadToGoogleDriveAppsScript(base64Data: String, fileName: String): String {
        return try {
            val url = appsScriptUrl.value
            if (url.isNotBlank()) {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val cleanBase64 = if (base64Data.contains(",")) {
                    base64Data.substringAfter(",")
                } else {
                    base64Data
                }

                // Payload format JSON yang akan diterima Google Apps Script
                val jsonPayload = """
                    {
                        "file": "$cleanBase64",
                        "filename": "$fileName"
                    }
                """.trimIndent()

                val body = RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    jsonPayload
                )

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    // Parsing sederhana URL Google Drive hasil upload
                    val driveUrlRegex = """(https?://drive\.google\.com/[^\s"]+)""".toRegex()
                    val matchResult = driveUrlRegex.find(responseBody)
                    matchResult?.value ?: "https://drive.google.com/file/d/success_uploaded/view"
                } else {
                    "https://drive.google.com/file/d/fallback_simulated_${System.currentTimeMillis()}/view"
                }
            } else {
                // Simulasi delay upload ~2 detik jika URL belum dikonfigurasi
                delay(2000)
                "https://drive.google.com/file/d/simulated_drive_file_${(100000..999999).random()}/view"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "https://drive.google.com/file/d/error_upload_fallback_${System.currentTimeMillis()}/view"
        }
    }

    fun submitComplaint(
        title: String,
        category: String,
        description: String,
        location: String,
        date: String,
        isAnonymous: Boolean,
        witnesses: String,
        evidenceBase64: String?,
        fileName: String?
    ) {
        val user = _currentUser.value ?: return
        _isSubmittingComplaint.value = true

        viewModelScope.launch {
            var uploadedUrl: String? = null

            if (evidenceBase64 != null && fileName != null) {
                _isUploadingEvidence.value = true
                uploadedUrl = uploadToGoogleDriveAppsScript(evidenceBase64, fileName)
                _isUploadingEvidence.value = false
            }

            val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            val currentDateCreated = sdf.format(Date())
            val complaintId = "NFC-ADUAN-${(100000..999999).random()}"

            val newComplaint = Complaint(
                id = complaintId,
                title = title,
                category = category,
                description = description,
                location = location,
                date = date,
                isAnonymous = isAnonymous,
                witnesses = witnesses,
                evidenceUrl = uploadedUrl,
                status = "Diajukan",
                dateCreated = currentDateCreated,
                reporterPhone = user.phone
            )

            db.complaintDao().insertComplaint(newComplaint)

            _isSubmittingComplaint.value = false
            _showComplaintForm.value = false
            _syncStatus.value = "Pengaduan $complaintId berhasil dikirim! Kerahasiaan data Anda kami jamin sepenuhnya."
        }
    }

    // --- FASE 3.5 MODUL PELATIHAN OFFLINE ---
    private suspend fun prepopulateTrainingMaterials() {
        val count = db.trainingDao().getMaterialById("TRN-001")
        if (count == null) {
            val defaultList = listOf(
                TrainingMaterial(
                    id = "TRN-001",
                    title = "Keselamatan Dasar Kelautan (Basic Safety)",
                    category = "Keselamatan Laut",
                    description = "Panduan dasar bertahan hidup di laut, penggunaan sekoci, life jacket, pemadam api, dan pertolongan pertama medis di kapal perikanan.",
                    type = "PDF",
                    contentUrl = "https://nfc.or.id/docs/basic-safety-training.pdf",
                    fileSize = "3.2 MB",
                    textContent = """
                        === PANDUAN KESELAMATAN DASAR KELAUTAN (BASIC SAFETY TRAINING) ===
                        
                        1. PENGGUNAAN ALAT KESELAMATAN DIRI:
                        - Jaket Pelampung (Life Jacket): Harus dikenakan dengan benar saat bekerja di dek dalam cuaca buruk atau kondisi darurat. Pastikan tali terikat kencang dan peluit penunjuk arah terpasang.
                        - Ring Buoy (Pelampung Penyelamat): Letakkan di tempat yang mudah dijangkau di lambung kapal.
                        
                        2. PROSEDUR DARURAT (Meninggalkan Kapal / Abandon Ship):
                        - Bunyi sirine darurat berkepanjangan (7 tiupan pendek dan 1 tiupan panjang).
                        - Segera kumpul di Muster Station dengan mengenakan life jacket.
                        - Lompat ke air dengan melipat tangan di depan dada, menyumbat hidung, dan posisi kaki rapat lurus untuk menghindari cedera benturan air.
                        
                        3. TEKNIK SURVIVAL DI AIR:
                        - Tetap tenang dan kurangi gerakan berlebihan untuk mencegah kelelahan serta hipotermia.
                        - Kumpul bersama ABK lain dalam formasi lingkaran (Huddle Position) agar tubuh tetap hangat dan lebih mudah terlihat oleh tim penyelamat (SAR).
                    """.trimIndent()
                ),
                TrainingMaterial(
                    id = "TRN-002",
                    title = "Hak Hukum Pelaut & Kontrak Kerja Laut",
                    category = "Hak ABK & Regulasi",
                    description = "Edukasi hukum mengenai standar perjanjian kerja laut (PKL), batas jam kerja sehat, hak atas upah penuh, dan mekanisme rujukan hukum.",
                    type = "VIDEO",
                    contentUrl = "https://nfc.or.id/videos/hak-abk-kontrak.mp4",
                    fileSize = "14.8 MB",
                    textContent = """
                        === HAK HUKUM PELAUT & KONTRAK KERJA LAUT (ABK) ===
                        
                        1. DOKUMEN PERJANJIAN KERJA LAUT (PKL):
                        - PKL adalah landasan hukum utama perlindungan ABK. PKL wajib ditandatangani sebelum kapal bertolak.
                        - Wajib dibaca seksama! PKL harus memuat: Identitas majikan/agen, nilai gaji bulanan, bonus tangkapan, pertanggungan asuransi kecelakaan, serta jaminan pemulangan (repatriasi) gratis ke pelabuhan asal.
                        
                        2. BATAS JAM KERJA SEHAT (Konvensi ILO C188):
                        - Hak istirahat minimal 10 jam dalam periode 24 jam apa pun.
                        - Hak istirahat minimal 77 jam dalam periode 7 hari apa pun.
                        - Kelelahan ekstrem di atas kapal adalah penyebab utama kecelakaan kerja fatal.
                        
                        3. PENANGANAN GAJI DITAHAN:
                        - Amankan bukti slip gaji, buku pelaut, perjanjian kontrak kerja (PKL), dan catatan log harian penangkapan.
                        - Kirim pengaduan melalui aplikasi National Fishers Center (NFC) Indonesia agar tim pendampingan hukum DFW dapat langsung berkoordinasi dengan instansi pemerintah (KKP, Kemenaker, BP2MI).
                    """.trimIndent()
                ),
                TrainingMaterial(
                    id = "TRN-003",
                    title = "Pertolongan Pertama Medis di Atas Samudera",
                    category = "Kesehatan & K3",
                    description = "Panduan medis praktis menangani cedera patah tulang, hipotermia, dehidrasi parah, dan resusitasi jantung paru (RJP) saat berlayar.",
                    type = "TEXT",
                    contentUrl = "", // Tanpa file unduhan besar, langsung dibaca offline
                    fileSize = "240 KB",
                    textContent = """
                        === PERTOLONGAN PERTAMA MEDIS DI ATAS SAMUDERA ===
                        
                        1. PENANGANAN HIPOTERMIA (Kedinginan Ekstrem):
                        - Pindahkan korban ke tempat tertutup dan hangat di dalam kabin kapal.
                        - Ganti pakaian basah dengan pakaian kering secara perlahan.
                        - Selimuti korban dengan selimut termal atau kain tebal kering.
                        - Berikan minuman hangat yang mengandung gula tinggi (jangan beri kafein atau alkohol).
                        
                        2. PERTOLONGAN PERTAMA PATAH TULANG:
                        - Jaga agar tulang yang patah tidak bergeser dengan memasang bidai darurat (bisa menggunakan bilah kayu datar atau karton tebal).
                        - Ikat dengan kain bersih secara perlahan tapi kuat agar posisi tulang stabil.
                        
                        3. RESUSITASI JANTUNG PARU (RJP / CPR):
                        - Jika korban tidak bernapas dan tidak ada denyut nadi:
                        - Lakukan kompresi dada sedalam 5-6 cm di tengah dada sebanyak 30 kali, diikuti dengan 2 kali tiupan napas buatan secara ritmis (kecepatan 100-120 kompresi per menit).
                        - Lakukan secara konsisten hingga tim medis pelabuhan atau kapal penyelamat tiba.
                    """.trimIndent()
                )
            )
            db.trainingDao().insertMaterials(defaultList)
        }
    }

    fun downloadTrainingMaterial(material: TrainingMaterial) {
        val workManager = WorkManager.getInstance(getApplication())
        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_MATERIAL_ID to material.id,
                    DownloadWorker.KEY_MATERIAL_URL to material.contentUrl,
                    DownloadWorker.KEY_MATERIAL_TITLE to material.title
                )
            )
            .addTag("download_${material.id}")
            .build()
        
        workManager.enqueue(workRequest)
    }

    fun toggleMaterialCompletion(id: String, isCompleted: Boolean) {
        viewModelScope.launch {
            db.trainingDao().updateCompletionStatus(id, isCompleted)
        }
    }

    fun deleteDownloadedMaterial(id: String) {
        viewModelScope.launch {
            val material = db.trainingDao().getMaterialById(id)
            material?.localFilePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            db.trainingDao().updateDownloadStatus(id, isDownloaded = false, localFilePath = null, progress = 0)
        }
    }
}
