package com.example.data.db

import androidx.room.*
import com.example.data.model.Complaint
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY localId DESC")
    fun getAllComplaints(): Flow<List<Complaint>>

    @Query("SELECT * FROM complaints WHERE reporterPhone = :phone ORDER BY localId DESC")
    fun getComplaintsByPhone(phone: String): Flow<List<Complaint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: Complaint)

    @Query("UPDATE complaints SET status = :status WHERE id = :complaintId")
    suspend fun updateComplaintStatus(complaintId: String, status: String)

    @Query("DELETE FROM complaints WHERE id = :complaintId")
    suspend fun deleteComplaintById(complaintId: String)
}
