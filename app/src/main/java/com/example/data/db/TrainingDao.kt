package com.example.data.db

import androidx.room.*
import com.example.data.model.TrainingMaterial
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training_materials ORDER BY id ASC")
    fun getAllMaterials(): Flow<List<TrainingMaterial>>

    @Query("SELECT * FROM training_materials WHERE id = :id")
    suspend fun getMaterialById(id: String): TrainingMaterial?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<TrainingMaterial>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: TrainingMaterial)

    @Update
    suspend fun updateMaterial(material: TrainingMaterial)

    @Query("UPDATE training_materials SET isDownloaded = :isDownloaded, localFilePath = :localFilePath, downloadProgress = :progress WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean, localFilePath: String?, progress: Int)

    @Query("UPDATE training_materials SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: String, isCompleted: Boolean)

    @Query("DELETE FROM training_materials WHERE id = :id")
    suspend fun deleteMaterial(id: String)
}
