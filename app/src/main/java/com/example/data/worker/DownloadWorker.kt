package com.example.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.db.AppDatabase
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val materialId = inputData.getString(KEY_MATERIAL_ID) ?: return Result.failure()
        val contentUrl = inputData.getString(KEY_MATERIAL_URL) ?: return Result.failure()
        val title = inputData.getString(KEY_MATERIAL_TITLE) ?: "Materi Pelatihan"

        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.trainingDao()

        try {
            // 1. Mulai proses unduhan
            dao.updateDownloadStatus(materialId, isDownloaded = false, localFilePath = null, progress = 5)
            setProgress(workDataOf(KEY_PROGRESS to 5))

            val localFile = File(applicationContext.filesDir, "training_${materialId}.pdf")

            // Jika URL berupa simulasi atau kosong, kita lakukan download simulasi yang andal
            if (!contentUrl.startsWith("http")) {
                val steps = listOf(10, 25, 45, 70, 90, 100)
                for (progress in steps) {
                    delay(800) // Simulasikan delay unduhan jaringan
                    dao.updateDownloadStatus(materialId, isDownloaded = (progress == 100), localFilePath = if (progress == 100) localFile.absolutePath else null, progress = progress)
                    setProgress(workDataOf(KEY_PROGRESS to progress))
                }

                // Buat file simulasi di local agar terdeteksi offline
                localFile.writeText("Materi Pelatihan Offline: $title\n\nIsi konten edukasi yang telah diunduh di latar belakang menggunakan WorkManager.")
            } else {
                // Unduhan nyata via OkHttp
                val client = OkHttpClient()
                val request = Request.Builder().url(contentUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Gagal mengunduh file, kode respons: ${response.code}")
                }

                val body = response.body ?: throw Exception("Respons body kosong")
                val contentLength = body.contentLength()
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(localFile)

                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead: Long = 0

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = ((totalBytesRead * 100) / contentLength).toInt()
                        dao.updateDownloadStatus(materialId, isDownloaded = false, localFilePath = null, progress = progress)
                        setProgress(workDataOf(KEY_PROGRESS to progress))
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                dao.updateDownloadStatus(materialId, isDownloaded = true, localFilePath = localFile.absolutePath, progress = 100)
                setProgress(workDataOf(KEY_PROGRESS to 100))
            }

            return Result.success(workDataOf(KEY_RESULT_PATH to localFile.absolutePath))
        } catch (e: Exception) {
            e.printStackTrace()
            // Reset status unduhan jika gagal
            dao.updateDownloadStatus(materialId, isDownloaded = false, localFilePath = null, progress = 0)
            return Result.failure(workDataOf(KEY_ERROR_MESSAGE to (e.message ?: "Kesalahan tidak dikenal")))
        }
    }

    companion object {
        const val KEY_MATERIAL_ID = "material_id"
        const val KEY_MATERIAL_URL = "material_url"
        const val KEY_MATERIAL_TITLE = "material_title"
        const val KEY_PROGRESS = "progress"
        const val KEY_RESULT_PATH = "result_path"
        const val KEY_ERROR_MESSAGE = "error_message"
    }
}
