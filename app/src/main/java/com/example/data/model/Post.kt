package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val excerpt: String,
    val date: String,
    val imageUrl: String?,
    val type: String, // "berita", "lowongan", "kegiatan"
    val authorName: String = "Admin NFC",
    val link: String = ""
)
