package com.example.daysuntil.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

class Converters{
    @TypeConverter
    fun toDate(long: Long): LocalDate = LocalDate.ofEpochDay(long)

    @TypeConverter
    fun fromDate(date: LocalDate): Long = date.toEpochDay()
}
@Entity
data class DatesDatabase(
    @PrimaryKey(autoGenerate = true) val id: Long,
    var name: String,
    var date: LocalDate,
    var repeat: Int
)
@Dao
interface DatabaseDao{
    @Query("INSERT INTO DatesDatabase (name, date, repeat) VALUES (:name, :date, :repeat)")
    suspend fun insertDate(name: String, date: LocalDate, repeat: Int): Long

    @Query("SELECT * FROM DatesDatabase")
    suspend fun getDate(): List<DatesDatabase>

    @Query("SELECT * FROM DatesDatabase WHERE id LIKE :id")
    suspend fun getDate(id: Long): DatesDatabase

    @Query("DELETE FROM DatesDatabase WHERE id LIKE :id")
    suspend fun deleteDate(id: Long)

    @Query("UPDATE DatesDatabase SET name = :name, date = :date, repeat = :repeat WHERE id LIKE :id")
    suspend fun updateDate(id: Long, name: String, date: LocalDate, repeat: Int)
}
@Database(entities = [DatesDatabase::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class Database: RoomDatabase(){
    abstract fun getDao(): DatabaseDao
}