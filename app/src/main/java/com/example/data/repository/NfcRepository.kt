package com.example.data.repository

import com.example.data.api.WordPressService
import com.example.data.db.PostDao
import com.example.data.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class NfcRepository(private val postDao: PostDao) {

    // Standard Retrofit instantiation
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://nfc.or.id/wp-json/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val apiService = retrofit.create(WordPressService::class.java)

    // Flow for observing posts stored locally
    val postsFlow: Flow<List<Post>> = postDao.getAllPosts()

    // Fetch and sync posts from WordPress API
    suspend fun syncPosts() {
        try {
            // Fetch live data for Berita, Lowongan, Kegiatan
            val rawPosts = apiService.getPosts(perPage = 20)
            if (rawPosts.isNotEmpty()) {
                val posts = rawPosts.map { wpPost ->
                    // Determine category/type based on content or title keywords (simplified for Fase 1 WP feed)
                    val contentText = wpPost.content.rendered.lowercase()
                    val titleText = wpPost.title.rendered.lowercase()
                    val type = when {
                        contentText.contains("lowongan") || titleText.contains("lowongan") || titleText.contains("kerja") -> "lowongan"
                        contentText.contains("kegiatan") || titleText.contains("kegiatan") || titleText.contains("sosialisasi") -> "kegiatan"
                        else -> "berita"
                    }

                    Post(
                        id = wpPost.id,
                        title = cleanHtml(wpPost.title.rendered),
                        content = wpPost.content.rendered,
                        excerpt = cleanHtml(wpPost.excerpt.rendered),
                        date = wpPost.date.split("T").firstOrNull() ?: wpPost.date,
                        imageUrl = wpPost.featuredImageUrl,
                        type = type,
                        authorName = "NFC Indonesia",
                        link = wpPost.link
                    )
                }
                postDao.insertPosts(posts)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Gracefully ignore and rely on Room + Prepopulated Data
        }
    }

    // Prepopulate database with standard default items if empty
    suspend fun checkAndPrepopulate() {
        val existing = postsFlow.firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaultPosts = listOf(
                Post(
                    id = 1,
                    title = "DFW Indonesia Lakukan Sosialisasi Pencegahan TPPO di Pelabuhan Benoa",
                    content = "<p>Destructive Fishing Watch (DFW) Indonesia melakukan kegiatan sosialisasi intensif di Pelabuhan Benoa, Bali, untuk mencegah terjadinya Tindak Pidana Perdagangan Orang (TPPO) di kalangan awak kapal perikanan (ABK).</p>",
                    excerpt = "Sosialisasi intensif dilakukan di Pelabuhan Benoa Bali untuk membekali nelayan dan ABK mengenai pencegahan perdagangan orang.",
                    date = "2026-07-01",
                    imageUrl = "https://images.unsplash.com/photo-1516466723877-e4ec1d736c8a?auto=format&fit=crop&q=80&w=600",
                    type = "berita",
                    authorName = "Humas DFW"
                ),
                Post(
                    id = 2,
                    title = "Pentingnya PKL (Perjanjian Kerja Laut) untuk Perlindungan Hukum ABK",
                    content = "<p>Setiap Anak Buah Kapal (ABK) yang bekerja di kapal perikanan wajib memiliki Perjanjian Kerja Laut (PKL). Dokumen ini merupakan bukti legal hubungan kerja yang sah dan menjamin hak-hak utama seperti gaji, asuransi, dan perlindungan dari kekerasan.</p>",
                    excerpt = "Ketahui mengapa setiap ABK wajib menandatangani Perjanjian Kerja Laut (PKL) sebelum berangkat berlayar.",
                    date = "2026-06-28",
                    imageUrl = "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&q=80&w=600",
                    type = "berita",
                    authorName = "NFC Legal Team"
                ),
                Post(
                    id = 3,
                    title = "Edukasi Keselamatan Kerja & Penggunaan Alat Keselamatan bagi Nelayan Sulut",
                    content = "<p>Tim NFC Indonesia bersama Dinas Perikanan Sulawesi Utara mendistribusikan life jacket dan melakukan demonstrasi penanganan darurat kecelakaan laut untuk nelayan tradisional di perairan Bitung.</p>",
                    excerpt = "Kegiatan demonstrasi penggunaan life jacket dan pelatihan darurat kecelakaan laut di Bitung, Sulawesi Utara.",
                    date = "2026-06-25",
                    imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&q=80&w=600",
                    type = "kegiatan",
                    authorName = "Fasilitator NFC"
                ),
                Post(
                    id = 4,
                    title = "Roadshow National Fishers Center di Sentra Perikanan Bali",
                    content = "<p>Guna mendekatkan layanan pengaduan kepada nelayan lokal, National Fishers Center membuka posko pelayanan keliling di sekitar pesisir pantai Kedonganan untuk konsultasi hukum gratis bagi nelayan.</p>",
                    excerpt = "Pembukaan posko keliling di Kedonganan, Bali untuk membantu nelayan melakukan konsultasi hukum gratis.",
                    date = "2026-06-20",
                    imageUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&q=80&w=600",
                    type = "kegiatan",
                    authorName = "Tim Lapangan"
                ),
                Post(
                    id = 5,
                    title = "Lowongan Kerja: Awak Kapal Perikanan (ABK) Longline Berlisensi Resmi",
                    content = "<p>Dibutuhkan segera 15 ABK untuk kapal longline yang beroperasi resmi di perairan Indonesia Timur. Fasilitas meliputi gaji pokok bulanan, bonus tangkapan, asuransi kesehatan, serta jaminan perlindungan sosial penuh sesuai regulasi PKL.</p>",
                    excerpt = "Peluang kerja resmi sebagai ABK Longline dengan perlindungan hukum, asuransi, dan sistem bagi hasil transparan.",
                    date = "2026-07-03",
                    imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&q=80&w=600",
                    type = "lowongan",
                    authorName = "Recruitment NFC"
                ),
                Post(
                    id = 6,
                    title = "Lowongan Kerja: Deckhand Kapal Tuna untuk Perairan Maluku",
                    content = "<p>Kesempatan berkarir di kapal penangkap tuna modern. Dibutuhkan deckhand berpengalaman dengan sertifikat kecakapan kapal perikanan (Anpin/Bstpi) yang valid. Penempatan pelabuhan pangkalan Ambon.</p>",
                    excerpt = "Dibutuhkan deckhand bersertifikasi untuk kapal penangkap tuna modern dengan kontrak resmi terverifikasi.",
                    date = "2026-06-24",
                    imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&q=80&w=600",
                    type = "lowongan",
                    authorName = "NFC Careers"
                )
            )
            postDao.insertPosts(defaultPosts)
        }
    }

    private fun cleanHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "") // Simple HTML stripping
            .replace("&nbsp;", " ")
            .replace("&#8211;", "-")
            .replace("&#8217;", "'")
            .replace("&#8220;", "\"")
            .replace("&#8221;", "\"")
            .trim()
    }
}
