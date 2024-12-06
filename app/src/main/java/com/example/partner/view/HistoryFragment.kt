package com.example.partner.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hello_world.R
import com.example.hello_world.databinding.FragmentHistoricoBinding
import com.example.partner.model.History
import com.example.partner.model.Info
import com.example.partner.model.User
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var tableLayout: TableLayout
    private lateinit var binding: FragmentHistoricoBinding
    private lateinit var historyModel: History
    private val TAG = "HistoryFragment"
    private lateinit var user: User

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoricoBinding.inflate(inflater, container, false)
        tableLayout = binding.tableLayout

        // Inicializando o Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Inicializando o historyModel
        historyModel = History()

        // Lendo os dados do Firebase
        readDataFromFirebase()
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar os dados do usuário do Bundle
        Log.i(TAG,"User vindo do login: ${(arguments?.getSerializable("USER_DATA") as User?)!!}")
        user = (arguments?.getSerializable("USER_DATA") as User?)!!

        binding.buttonSearch.setOnClickListener {
            val searchText = binding.editTextDate.text.toString()
            filterTableByActivityName(searchText)
        }
    }

    private fun filterTableByActivityName(searchText: String) {
        // Iterar sobre todas as linhas da tabela e verificar se o nome da atividade corresponde ao texto de pesquisa
        for (i in 0 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as TableRow
            val textViewActivityName = row.getChildAt(0) as TextView
            if (textViewActivityName.text.toString().contains(searchText, true)) {
                row.visibility = View.VISIBLE
            } else {
                row.visibility = View.GONE
            }
        }
    }

    private fun readDataFromFirebase() {
        Log.i(TAG, "readDataFromFirebase: Started")
        this.user = (arguments?.getSerializable("USER_DATA") as User?)!!
        Log.i(TAG, "Usuário: ${user}")
        var arrayHistory = HashSet<String>() // Usar HashSet para evitar duplicatas
        val activitiesList = mutableListOf<History>()

        try {
            database.child("log_activities").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Limpar a tabela antes de adicionar novos dados
                    tableLayout.removeAllViews()
                    activitiesList.clear()

                    for (dataSnapshot in snapshot.children) {
                        var idActivity = dataSnapshot.child("id").getValue(String::class.java)

                        val idAluno = dataSnapshot.child("aluno").getValue(String::class.java)
                        val idTurma = dataSnapshot.child("turma").getValue(String::class.java)

                        var nameActivity: String? = null
                        var dateActivity: String? = null
                        var infoActivity: Info? = null

                        if (user.id == idAluno && user.turma == idTurma) {
                            nameActivity = dataSnapshot.child("activityName").getValue(String::class.java)
                            dateActivity = dataSnapshot.child("date").getValue(String::class.java)
                            infoActivity = dataSnapshot.child("info").getValue(Info::class.java)
                        }

                        if (nameActivity != null && infoActivity != null) {
                            historyModel = History()
                            historyModel.nameActivity = nameActivity
                            historyModel.dateActivity = dateActivity ?: ""
                            historyModel.infoActivity = infoActivity
                            arrayHistory.add(idActivity!!)

                            activitiesList.add(historyModel)
                        }
                    }

                    // Ordenar a lista de atividades pela data, considerando "N/F" como data vazia
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    activitiesList.sortWith(compareBy {
                        if (it.dateActivity!!.isEmpty()) null else dateFormat.parse(it.dateActivity)
                    })

                    // Adicionar as linhas ordenadas à tabela
                    for (activity in activitiesList) {
                        addRowToTable(activity.nameActivity, activity.dateActivity!!, activity.infoActivity)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to read data", error.toException())
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error to read history", e)
        }
    }

    @SuppressLint("WeekBasedYear")
    private fun addRowToTable(activityName: String, date: String, info: Info) {
        var nd = ""

        val tableRow = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT
            )
            if (date.isNotEmpty()) {
                setBackgroundColor(Color.parseColor("#3CB7AF"))
            } else {
                nd = "N/F"
                setBackgroundColor(Color.parseColor("#FFF37D7D"))
            }
            setHorizontalGravity(Gravity.CENTER)
            setVerticalGravity(Gravity.CENTER)
            minimumHeight = 60
        }

        val textViewActivityName = TextView(requireContext()).apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            text = activityName
            minHeight = 54
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }

        val textViewDate = TextView(requireContext()).apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            minHeight = 54
            text = if (nd == "N/F") "N/F" else date
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }

        val textViewInfo = AppCompatButton(requireContext()).apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            text = "INFO"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#3CB7AF"))
            setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button))
            setOnClickListener {
                // Inflate the custom layout/view
                val inflater = LayoutInflater.from(requireContext())
                val view = inflater.inflate(R.layout.custom_dialog_history, null)
                // Set the activity data to the dialog's views
                view.findViewById<TextView>(R.id.title).text = info.nameActivity
                view.findViewById<TextView>(R.id.date).text = info.dateActivity
                view.findViewById<TextView>(R.id.grade).text = info.resultActivity.toString()
                view.findViewById<TextView>(R.id.description).text = info.descriptionInfo

                // Create the AlertDialog
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(view)
                    .setPositiveButton("VOLTAR") { dialog, _ -> dialog.dismiss() }
                    .create()

                // Show the AlertDialog
                dialog.show()
            }
        }

        val layoutParamsInfoButton = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 16, 16, 16) // Adicionando margem à esquerda
        }
        textViewInfo.layoutParams = layoutParamsInfoButton

        tableRow.addView(textViewActivityName)
        tableRow.addView(textViewDate)
        tableRow.addView(textViewInfo)
        tableLayout.addView(tableRow)
    }
}
