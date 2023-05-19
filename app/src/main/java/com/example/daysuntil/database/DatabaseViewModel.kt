package com.example.daysuntil.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DatabaseViewModel @Inject constructor(private val repository: DataRepository): ViewModel() {

    suspend fun insertDate(name: String, date: LocalDate, repeat: Int): Long = repository.insertDate(name, date, repeat)

    suspend fun getDate(): List<DatesDatabase> = repository.getDate()
    suspend fun getDate(id: Long): DatesDatabase = repository.getDate(id)

    fun deleteDate(id: Long){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDate(id)
        }
    }
    fun update(id: Long, name: String, date: LocalDate, repeat: Int){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDate(id, name, date, repeat)
        }
    }
}