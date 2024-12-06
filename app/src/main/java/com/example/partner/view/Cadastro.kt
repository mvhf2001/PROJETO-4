package com.example.partner.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.partner.LoginActivity
import com.example.hello_world.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Cadastro : AppCompatActivity() {
    private lateinit var matricula: EditText
    private lateinit var editTextTextEmailAddress: EditText
    private lateinit var editTextTextPassword: EditText
    private lateinit var cadastrar: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var usuarioID: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Adicionado para remover a barra de título
        setContentView(R.layout.activity_cadastro)
        iniciarComponentes()
        firebaseAuth = FirebaseAuth.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cadastrar.setOnClickListener {
            val email = editTextTextEmailAddress.text.toString()
            val senha = editTextTextPassword.text.toString()
            val matricula = matricula.text.toString()
            if (email.isEmpty() || senha.isEmpty() || matricula.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                cadastrarUsuario()
            }
        }
    }
    private fun cadastrarUsuario() {
        val email = editTextTextEmailAddress.text.toString()
        val senha = editTextTextPassword.text.toString()
        firebaseAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SalvarDados()
                    Toast.makeText(this@Cadastro, "Usuário cadastrado com sucesso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Cadastro, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@Cadastro, "Erro ao cadastrar usuário", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun SalvarDados() {
        val matricula = matricula.text.toString()

        val db = FirebaseFirestore.getInstance()

        val usuarios = hashMapOf(
            "matricula" to matricula
        )

        usuarioID = FirebaseAuth.getInstance().currentUser?.uid  // Assign value to usuarioID here

        val documentReference = db.collection("Usuarios").document(usuarioID!!)
        documentReference.set(usuarios)
            .addOnSuccessListener { }
            .addOnFailureListener { }
    }


    private fun iniciarComponentes() {
        editTextTextEmailAddress = findViewById(R.id.editTextTextEmailAddress)
        editTextTextPassword = findViewById(R.id.editTextTextPassword)
        matricula = findViewById(R.id.matricula)
        cadastrar = findViewById(R.id.cadastrar)
    }
}