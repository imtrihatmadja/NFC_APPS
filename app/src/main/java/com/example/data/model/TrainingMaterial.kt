package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_materials")
data class TrainingMaterial(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val description: String,
    val type: String, // "PDF", "VIDEO", "TEXT"
    val contentUrl: String,
    val fileSize: String,
    val localFilePath: String? = null,
    val isDownloaded: Boolean = false,
    val downloadProgress: Int = 0,
    val isCompleted: Boolean = false,
    val textContent: String? = null // Rich text content for in-app reading offline
)
