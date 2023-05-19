package com.example.daysuntil

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.daysuntil.database.DatabaseViewModel
import com.example.daysuntil.database.DatesDatabase
import com.example.daysuntil.databinding.ActivityMainBinding
import com.example.daysuntil.databinding.NewDateDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val list = listOf(true, true, false, false)
        val string = list.joinToString()
        Log.d("MyLog", string)

        val list1 = string.split(", ")
        val string1 = list1.joinToString()
        Log.d("MyLog", string1)
    }

    override fun onStart() {
        super.onStart()
        val databaseViewModel = ViewModelProvider(this)["databaseViewModel", DatabaseViewModel::class.java]
        var datesListAdapter: DatesListAdapter? = null
        fun getPeriod(date: LocalDate, repeat: Int): String{
            when(repeat){
                0->{
                    var period = date.dayOfWeek.value - LocalDate.now().dayOfWeek.value
                    if(period < 0) period += 7
                    return getString(R.string.period, period)
                }
                1->{
                    var period = date.dayOfMonth - LocalDate.now().dayOfMonth
                    if(period<0) period += LocalDate.now().month.length(LocalDate.now().isLeapYear)
                    return getString(R.string.period, period)
                }
                2->{
                    var period = date.dayOfYear - LocalDate.now().dayOfYear
                    if(period<0) period += if(LocalDate.now().isLeapYear) 366 else 365
                    return getString(R.string.period, period)
                }
                else->{
                    val period = ChronoUnit.DAYS.between(LocalDate.now(), date)
                    return getString(R.string.period, period)
                }
            }
        }
        fun getPeriod(date: DatesDatabase): String{
            when(date.repeat){
                0->{
                    var period = date.date.dayOfWeek.value - LocalDate.now().dayOfWeek.value
                    if(period < 0) period += 7
                    return getString(R.string.period, period)
                }
                1->{
                    var period = date.date.dayOfMonth - LocalDate.now().dayOfMonth
                    if(period<0) period += LocalDate.now().month.length(LocalDate.now().isLeapYear)
                    return getString(R.string.period, period)
                }
                2->{
                    var period = date.date.dayOfYear - LocalDate.now().dayOfYear
                    if(period<0) period += if(LocalDate.now().isLeapYear) 366 else 365
                    return getString(R.string.period, period)
                }
                else->{
                    val period = ChronoUnit.DAYS.between(LocalDate.now(), date.date)
                    return getString(R.string.period, period)
                }
            }
        }
        fun getPeriods(datesList: Array<DatesDatabase>): Array<String>{
            var periods = emptyArray<String>()
            for(date in datesList){
                periods += getPeriod(date)
            }
            return periods
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val dates = databaseViewModel.getDate().toTypedArray()
            datesListAdapter = DatesListAdapter(dates, getPeriods(dates)){position, date, delete, update->
                val dialog = BottomSheetDialog(this@MainActivity)
                val dialogBinding = NewDateDialogBinding.inflate(layoutInflater)

                val isRepeat = date.repeat != -1
                dialogBinding.repeatEvery.isEnabled = isRepeat
                if(isRepeat) dialogBinding.repeatEvery.setSelection(date.repeat)

                dialogBinding.repeat.isChecked = isRepeat
                dialogBinding.repeat.setOnCheckedChangeListener {_,isChanged->
                    dialogBinding.repeatEvery.isEnabled = isChanged
                }

                dialogBinding.newName.setText(date.name)
                dialogBinding.selectedDate.text = date.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))

                dialogBinding.OKBtn.isEnabled = true
                dialogBinding.deleteBtn.visibility = View.VISIBLE

                var newDate = date.date
                dialogBinding.newName.addTextChangedListener {
                    dialogBinding.OKBtn.isEnabled = it!!.isNotBlank()
                }
                val listener = DatePickerDialog.OnDateSetListener{_,year,month,dayOfMonth->
                    newDate = LocalDate.of(year, month+1, dayOfMonth)
                    dialogBinding.selectedDate.text = newDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                    dialogBinding.OKBtn.isEnabled = dialogBinding.newName.text.isNotBlank()
                }
                val datePickerDialog = DatePickerDialog(this@MainActivity, listener, date.date.year, date.date.monthValue-1, date.date.dayOfMonth)

                dialogBinding.pickDateBtn.setOnClickListener {
                    datePickerDialog.show()
                }
                dialogBinding.deleteBtn.setOnClickListener {
                    dialog.dismiss()
                    delete(position)
                    databaseViewModel.deleteDate(date.id)
                }
                dialogBinding.cancelBtn.setOnClickListener { dialog.dismiss() }
                dialogBinding.OKBtn.setOnClickListener {
                    dialog.dismiss()
                    val newName = dialogBinding.newName.text.toString()
                    val repeat =
                        if(dialogBinding.repeat.isChecked) dialogBinding.repeatEvery.selectedItemPosition
                        else -1
                    update(position, newName, newDate, repeat, getPeriod(newDate, repeat))
                    databaseViewModel.update(date.id, newName, newDate, repeat)
                }
                dialog.setContentView(dialogBinding.root)
                dialog.show()

                dialog.setContentView(dialogBinding.root)
            }
            runOnUiThread{
                binding.datesList.adapter = datesListAdapter
            }
        }
        binding.addDate.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val dialogBinding = NewDateDialogBinding.inflate(layoutInflater)
            var newDate: LocalDate? = null
            dialogBinding.repeatEvery.isEnabled = false
            dialogBinding.repeat.setOnCheckedChangeListener {_,isChanged->
                dialogBinding.repeatEvery.isEnabled = isChanged
            }

            dialogBinding.newName.addTextChangedListener {
                    dialogBinding.OKBtn.isEnabled = (it!!.isNotBlank() && newDate != null)
            }
            val listener = DatePickerDialog.OnDateSetListener{_,year,month,dayOfMonth->
                newDate = LocalDate.of(year, month+1, dayOfMonth)
                dialogBinding.selectedDate.text = newDate?.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                dialogBinding.OKBtn.isEnabled = (dialogBinding.newName.text.isNotBlank() && newDate != null)
            }
            val datePickerDialog = DatePickerDialog(this, listener, LocalDate.now().year, LocalDate.now().monthValue-1, LocalDate.now().dayOfMonth)

            dialogBinding.pickDateBtn.setOnClickListener {
                datePickerDialog.show()
            }
            dialogBinding.cancelBtn.setOnClickListener { dialog.dismiss() }
            dialogBinding.OKBtn.setOnClickListener {
                dialog.dismiss()
                val newName = dialogBinding.newName.text.toString()
                val repeat =
                    if(dialogBinding.repeat.isChecked) dialogBinding.repeatEvery.selectedItemPosition
                    else -1
                lifecycleScope.launch {
                    val dateId = databaseViewModel.insertDate(
                        newName,
                        newDate!!,
                        repeat
                    )
                    val date = DatesDatabase(dateId, newName, newDate!!, repeat)
                    runOnUiThread{
                        datesListAdapter?.addItem(date, getPeriod(date))
                    }
                }
            }
            dialog.setContentView(dialogBinding.root)
            dialog.show()
        }
    }
}