package com.example.partner.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hello_world.R
import com.example.hello_world.databinding.FragmentPerfilBinding
import com.example.partner.model.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
class PerfilFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var binding: FragmentPerfilBinding
    private lateinit var user: User
    private val TAG = "PerfilFragment"

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPerfilBinding.inflate(inflater, container, false)

        // Inicializando o Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Inicializando o historyModel
        user = User()

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar os dados do usuário do Bundle
        Log.i(TAG, "User vindo do login: ${(arguments?.getSerializable("USER_DATA") as User?)!!}")
        user = (arguments?.getSerializable("USER_DATA") as User?)!!

        // Exibir o nome do usuário no TextView com o id textView3
        val textView3 = view.findViewById<TextView>(R.id.textView3)
        textView3.text = user.name

        // Exibir a matrícula do usuário no TextView com o id textView11
        val textView11 = view.findViewById<TextView>(R.id.textView11)
        textView11.text = user.matricula

        // Exibir o email do usuário no TextView com o id textView15
        val textView15 = view.findViewById<TextView>(R.id.textView15)
        textView15.text = user.email

        // Exibir o telefone do usuário no TextView com o id textView14
        val textView14 = view.findViewById<TextView>(R.id.textView14)
        textView14.text = user.phone

        // Exibir o turno do usuário no TextView com o id textView12
        val textView12 = view.findViewById<TextView>(R.id.textView12)
        textView12.text = user.turno

        // Exibir a data de criação do usuário no TextView com o id textView13
        val textView13 = view.findViewById<TextView>(R.id.textView13)
        textView13.text = user.data_criacao

        // Exibir a data de nascimento do usuário no TextView com o id textView16
        val textView16 = view.findViewById<TextView>(R.id.textView16)
        textView16.text = user.data_nasc
    }



    private fun readDataFromFirebase() {
        /*
        Log.i(TAG, "readDataFromFirebase: Started")
        this.user = (arguments?.getSerializable("USER_DATA") as User?)!!
        Log.i(TAG, "Usuário: ${user}")
        try{
            database.child("log_activities").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        var nameActivity: String? = null
                        var dateActivity: String? = null
                        var infoActivity: Info? = null
                        Log.i(TAG, "Loop: Entrou no loop")

                        val idAluno = dataSnapshot.child("aluno").getValue(String::class.java)
                        val idTurma = dataSnapshot.child("turma").getValue(String::class.java)
                        Log.i(TAG, "IdAluno: ${idAluno} , IdTurma: ${idTurma}")
                        if (user.id == idAluno.toString() && user.turma == idTurma.toString()) {
                            Log.i(TAG, "Entrou aqui")
                            nameActivity = dataSnapshot.child("activityName").getValue(String::class.java)
                            dateActivity = dataSnapshot.child("date").getValue(String::class.java)
                            infoActivity = dataSnapshot.child("info").getValue(Info::class.java)
                        }

                        if (nameActivity != null && dateActivity != null && infoActivity != null) {
                            historyModel.nameActivity = nameActivity
                            historyModel.dateActivity = dateActivity
                            historyModel.infoActivity = infoActivity

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to read data", error.toException())
                }
            })
        }catch (e: Exception){
            Log.e(TAG, "Error to read history")
        }
        */
    }

}
