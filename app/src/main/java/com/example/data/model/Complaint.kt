package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "complaints")
data class Complaint(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val location: String,
    val date: String,
    val isAnonymous: Boolean,
    val witnesses: String,
    val evidenceUrl: String?,
    val status: String, // "Diajukan", "Diproses", "Selesai", "Rujukan"
    val dateCreated: String,
    val reporterPhone: String // No WhatsApp pelapor untuk pencocokan sesi
)
