package com.example.daysuntil.database

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton


class DataRepository @Inject constructor(
    private val Dao: DatabaseDao
){
    suspend fun insertDate(name: String, date: LocalDate, repeat: Int): Long = Dao.insertDate(name, date, repeat)

    suspend fun getDate(): List<DatesDatabase> = Dao.getDate()
    suspend fun getDate(id: Long): DatesDatabase = Dao.getDate(id)

    suspend fun deleteDate(id: Long) = Dao.deleteDate(id)

    suspend fun updateDate(id: Long, name: String, date: LocalDate, repeat: Int) = Dao.updateDate(id, name, date, repeat)
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule{
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext app: Context) =
        Room.databaseBuilder(app, Database::class.java, "database")
            .addMigrations(MIGRATION_2_3).build()

    @Singleton
    @Provides
    fun provideDao(db: Database) = db.getDao()
}

@HiltAndroidApp
class BaseApplication: Application()

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE DatesDatabase ADD COLUMN repeat INTEGER DEFAULT -1 NOT NULL")
    }
}