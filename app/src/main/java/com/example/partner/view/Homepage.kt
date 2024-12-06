package com.example.partner.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import com.example.hello_world.R
import com.example.hello_world.databinding.ActivityHomepageBinding
import com.example.partner.dto.HistoryDTO
import com.example.partner.model.Activity
import com.example.partner.model.History
import com.example.partner.model.Info
import com.example.partner.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Homepage : AppCompatActivity() {
    private lateinit var binding: ActivityHomepageBinding
    private var TAG: String = "HomePage"
    private lateinit var user: User
    private lateinit var database: DatabaseReference
    private lateinit var tableLayout: TableLayout
    private lateinit var activity: Activity
    private lateinit var history: History
    private lateinit var historyDto: HistoryDTO
    private var selectedFileUri: Uri? = null
    private var dateToday: String = ""

    @SuppressLint("WrongViewCast")
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Adicionado para remover a barra de título
        enableEdgeToEdge()
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val homepageScroll: ScrollView = findViewById(R.id.homepage_scroll)
        this.database = FirebaseDatabase.getInstance().reference
        this.user = (intent.getSerializableExtra("USER_DATA") as User?)!!
        this.tableLayout = binding.tableLayout
        // Referência ao botão e ao layout container
        val buttonAtividades: Button = findViewById(R.id.buttonatividades)
        val buttonPerfil: Button = findViewById(R.id.buttonperfil)
        val buttonHome: Button = findViewById(R.id.buttonhome)
        val icon: ImageView = findViewById(R.id.icon)
        val turno: TextView = findViewById(R.id.turno)
        turno.text = user.turno
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateToday = dateFormat.format(Date())
        history = History()
        historyDto = HistoryDTO()
        activity = Activity()

        readDataFromFirebase()

        icon.setOnClickListener {
            restoreHomePage()
        }

        buttonHome.setOnClickListener {
            val intent = Intent(this@Homepage, Home::class.java)
            startActivity(intent)
        }

        buttonPerfil.setOnClickListener {
            val perfilFragment = PerfilFragment()
            val bundle = Bundle()
            bundle.putSerializable("USER_DATA", user)
            perfilFragment.arguments = bundle
            homepageScroll.visibility = View.INVISIBLE

            // Substituir o contêiner de fragmentos pelo HistoryFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_homepage, perfilFragment)
                .addToBackStack(null) // Adiciona a transação à pilha de volta para permitir navegação
                .commit()
        }

        // Configura o listener para o botão
        buttonAtividades.setOnClickListener {
            // Criar uma instância do HistoryFragment
            homepageScroll.visibility = View.INVISIBLE
            val historyFragment = HistoryFragment()
            val bundle = Bundle()
            bundle.putSerializable("USER_DATA", user)
            historyFragment.arguments = bundle
            //binding.homepageScroll.visibility

            // Substituir o contêiner de fragmentos pelo HistoryFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_homepage, historyFragment)
                .addToBackStack(null) // Adiciona a transação à pilha de volta para permitir navegação
                .commit()
        }
    }

    @Suppress("DEPRECATION")
    private fun readDataFromFirebase() {
        this.user = (intent.getSerializableExtra("USER_DATA") as User?)!!
        var arrayActivities = HashSet<String>() // Usar HashSet para evitar duplicatas
        try {
            database.child("activities").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Limpar a tabela antes de adicionar novos dados
                    tableLayout.removeAllViews()

                    for (dataSnapshot in snapshot.children) {
                        Log.i(TAG, "entrou no for")
                        if (dataSnapshot != null) {
                            Log.i(TAG, "entrou aqui 1")
                            var id: String? = null
                            var nameActivity: String? = null
                            var turma: String? = null
                            var infoActivity: Info? = null
                            val today = Date()

                            val idTurma = dataSnapshot.child("turma").getValue(String::class.java)

                            // Verificar se a atividade já está no arrayActivities
                            if (idTurma != null && arrayActivities.contains(dataSnapshot.key)) {
                                Log.i(TAG, "entrou aqui 2")
                                continue // Pular se já estiver presente
                            }

                            if (user.turma == idTurma) {
                                Log.i(TAG, "entrou aqui 3")
                                id = dataSnapshot.child("id").getValue(String::class.java)
                                nameActivity = dataSnapshot.child("activityName").getValue(String::class.java)
                                turma = dataSnapshot.child("turma").getValue(String::class.java)
                                infoActivity = dataSnapshot.child("info").getValue(Info::class.java)
                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val date = dateFormat.parse(infoActivity!!.dateActivity)

                                if (date != null && today.after(date)) {
                                    historyDto.id = id!!
                                    historyDto.nameActivity = nameActivity!!
                                    historyDto.date = ""
                                    historyDto.info = infoActivity
                                    historyDto.turma = turma!!
                                    historyDto.aluno = user.id!!
                                    database.child("log_activities").child(dataSnapshot.key!!)
                                        .setValue(historyDto)
                                    database.child("activities").child(dataSnapshot.key!!).removeValue()

                                    // Parar o loop para passar ao próximo registro
                                    break
                                }
                            }

                            if (nameActivity != null && turma != null && infoActivity != null) {
                                Log.i(TAG, "entrou aqui 4")
                                activity.nameActivity = nameActivity
                                activity.turma = turma
                                activity.infoActivity = infoActivity
                                arrayActivities.add(dataSnapshot.key!!)
                                val key: String = dataSnapshot.key.toString()

                                addRowToTable(id, activity.nameActivity,
                                    activity.turma, activity.infoActivity, key)
                            }
                        }
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

    private fun restoreHomePage() {
        // Remover todos os fragmentos empilhados
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        // Tornar a homepage visível novamente
        val homepageScroll: ScrollView = findViewById(R.id.homepage_scroll)
        homepageScroll.visibility = View.VISIBLE
    }

    private fun isActivityRunning(): Boolean {
        return !isFinishing && !isDestroyed
    }

    @Suppress("DEPRECATION")
    @SuppressLint("WeekBasedYear")
    private fun addRowToTable(id: String?, activityName: String, turma: String, info: Info, key: String) {
        val tableRow = TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#FFFFFF"))
            setHorizontalGravity(Gravity.CENTER_VERTICAL)
            minimumHeight = 60
        }

        val textViewActivityName = TextView(this).apply {
            textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            text = activityName
            minHeight = 54
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }

        val textViewInfo = AppCompatButton(this).apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            text = "INFO"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#3CB7AF"))
            setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.rounded_button))
            setOnClickListener {
                if (isActivityRunning()) {
                    runOnUiThread {
                        // Inflate the custom layout/view
                        val inflater = LayoutInflater.from(context)
                        val view = inflater.inflate(R.layout.custom_dialog, null)

                        // Set the activity data to the dialog's views
                        view.findViewById<TextView>(R.id.title).text = info.nameActivity
                        view.findViewById<TextView>(R.id.date).text = info.dateActivity
                        view.findViewById<TextView>(R.id.grade).text = info.resultActivity.toString()
                        view.findViewById<TextView>(R.id.description).text = info.descriptionInfo

                        // Handle file selection
                        val selectFileButton = view.findViewById<Button>(R.id.select_file_button)
                        selectFileButton.setOnClickListener {
                            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "*/*"
                                addCategory(Intent.CATEGORY_OPENABLE)
                            }
                            startActivityForResult(Intent.createChooser(intent, "Select File"), 1)
                        }

                        // Create the AlertDialog
                        val dialog = AlertDialog.Builder(this@Homepage)
                            .setView(view)
                            .setPositiveButton("CONFIRMAR") { dialog, _ ->
                                if (selectedFileUri != null) {
                                    val storageRef = FirebaseStorage.getInstance().reference
                                    val fileRef = storageRef.child("log_activities/${selectedFileUri!!.lastPathSegment}")
                                    val uploadTask = fileRef.putFile(selectedFileUri!!)

                                    uploadTask.addOnSuccessListener {
                                        fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                            // Save the log activity with file URL
                                            val logActivity = hashMapOf(
                                                "info" to info,
                                                "activityName" to activityName,
                                                "id" to id,
                                                "resultActivity" to null,
                                                "date" to dateToday,
                                                "turma" to turma,
                                                "aluno" to user.id,
                                                "descriptionInfo" to info.descriptionInfo,
                                                "fileUrl" to downloadUri.toString()
                                            )
                                            database.child("activities").child(key).removeValue()
                                            database.child("log_activities").push().setValue(logActivity)
                                        }
                                    }.addOnFailureListener {
                                        // Handle unsuccessful uploads
                                        Log.e(TAG, "File upload failed", it)
                                    }
                                }
                                dialog.dismiss()
                            }
                            .setNegativeButton("VOLTAR") { dialog, _ ->
                                dialog.cancel() }
                            .create()

                        // Show the AlertDialog
                        dialog.show()
                    }
                } else {
                    Log.w(TAG, "Activity is not in a valid state to show dialog")
                }
            }
        }

        // Definindo parâmetros de layout para adicionar margem entre os elementos
        val layoutParamsActivityName = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f
        )
        textViewActivityName.layoutParams = layoutParamsActivityName

        val layoutParamsInfoButton = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(28, 0, 0, 16) // Adicionando margem à esquerda
        }
        textViewInfo.layoutParams = layoutParamsInfoButton

        tableRow.addView(textViewActivityName)
        tableRow.addView(textViewInfo)
        tableLayout.addView(tableRow)
    }


    // Handle the result of file selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
            }
        }
    }
}