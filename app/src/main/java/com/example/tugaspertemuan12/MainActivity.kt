package com.example.tugaspertemuan12

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.tugaspertemuan12.databinding.ActivityMainBinding
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.AdapterView.OnItemLongClickListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mNoteDao: NoteDao
    private lateinit var executorService: ExecutorService
    private var updateId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executorService = Executors.newSingleThreadExecutor()
        val db = NoteRoomDatabase.getDatabase(this)
        mNoteDao = db!!.noteDao()!!

        with(binding) {
            btnAdd.setOnClickListener {
                insert(
                    Note(
                        title = edtTitle.text.toString(),
                        description = edtDesc.text.toString()
                    )
                )
                setEmptyField()
            }
            listView.setOnItemClickListener{
                adapterView, view, i, l ->
                val item = adapterView.adapter.getItem(i) as Note
                updateId = item.id
                edtTitle.setText(item.title)
                edtDesc.setText(item.description)
            }

            btnUpdate.setOnClickListener {
                update(Note(
                    id = updateId,
                    title = edtTitle.text.toString(),
                    description = edtDesc.text.toString()
                ))
                updateId = 0
                setEmptyField()
            }

            listView.onItemLongClickListener =
                AdapterView.OnItemLongClickListener{
                    adapterView, view, i, l ->
                    val item = adapterView.adapter.getItem(i) as Note
                    delete(item)
                    true
                }
        }
    }

    private fun setEmptyField() {
        with(binding) {
            edtTitle.setText("")
            edtDesc.setText("")
        }
    }

    private fun getAllNotes(){
        mNoteDao.allNotes.observe(this) {
                notes ->
                val adapter : ArrayAdapter<Note> = ArrayAdapter(this, android.R.layout.simple_list_item_1, notes)
            binding.listView.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        getAllNotes()
    }

    private fun insert(note: Note){
        executorService.execute {
            mNoteDao.insert(note)
        }
    }

    private fun delete(note: Note){
        executorService.execute {
            mNoteDao.delete(note)
        }
    }

    private fun update(note: Note){
        executorService.execute {
            mNoteDao.update(note)
        }
    }
}