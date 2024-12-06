package com.example.partner

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.partner.model.User
import com.example.hello_world.R
import com.example.hello_world.databinding.ActivityLoginBinding
import com.example.partner.util.EMAIL_REGEX
import com.example.partner.util.isFieldValid
import com.example.partner.view.Cadastro
import com.example.partner.view.Homepage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Adicionado para remover a barra de título
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        setUpView()
    }

    private fun setUpView() {
        binding.apply {
            btnLogin.setOnClickListener {
                goToHomePageActivity()
            }
            gotocadastro.setOnClickListener {
                val intent = Intent(this@LoginActivity, Cadastro::class.java)
                startActivity(intent)
            }
        }
    }

    private fun goToHomePageActivity() {
        var result = true
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPwd.text.toString()
        if (email.isFieldValid(Regex(EMAIL_REGEX)).not()) {
            Toast.makeText(
                this@LoginActivity,
                getString(R.string.email_field),
                Toast.LENGTH_LONG
            ).show()
            result = false
        }
        if (result) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Authentication succeeded, fetch user data from Realtime Database
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val database = FirebaseDatabase.getInstance()
                        val userRef = database.getReference("users").child(userId)
                        userRef.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                // User exists, fetch user data
                                val user: User? = snapshot.getValue(User::class.java)
                                user?.id = userId
                                val intent = Intent(this@LoginActivity, Homepage::class.java)
                                intent.putExtra("USER_DATA", user as Parcelable)
                                startActivity(intent)
                            } else {
                                // User does not exist, create new user
                                val newUser = User(
                                    id = userId,
                                    email = email
                                )
                                userRef.setValue(newUser).addOnSuccessListener {
                                    val intent = Intent(this@LoginActivity, Homepage::class.java)
                                    intent.putExtra("USER_DATA", newUser as Parcelable)
                                    startActivity(intent)
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        getString(R.string.user_data_error),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(
                                this@LoginActivity,
                                getString(R.string.user_data_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.user_data_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}