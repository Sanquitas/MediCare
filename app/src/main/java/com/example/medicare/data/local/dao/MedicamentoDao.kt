package com.example.medicare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medicare.data.local.entities.MedicamentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicamento(medicamento: MedicamentoEntity)

    @Update
    suspend fun updateMedicamento(medicamento: MedicamentoEntity)

    @Query("SELECT * FROM medicamentos_locales")
    suspend fun getAllMedicamentos(): List<MedicamentoEntity>

    @Query("SELECT * FROM medicamentos_locales WHERE isSynced = 0")
    suspend fun getUnsyncedMedicamentos(): List<MedicamentoEntity>

    @Query("UPDATE medicamentos_locales SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT * FROM medicamentos_locales WHERE id = :id")
    suspend fun getMedicamentoById(id: String): MedicamentoEntity?

    @Query("UPDATE medicamentos_locales SET estado = :estado, isSynced = 0 WHERE id = :id")
    suspend fun updateEstado(id: String, estado: String)

    @Query("UPDATE medicamentos_locales SET estado = 'Tomado', horaToma = :horaToma, fotoLocalUri = :fotoUri, isSynced = 0 WHERE id = :id")
    suspend fun registrarTomaConFoto(id: String, horaToma: String, fotoUri: String)

    @Query("SELECT * FROM medicamentos_locales")
    fun getAllMedicamentosLive(): Flow<List<MedicamentoEntity>>
}