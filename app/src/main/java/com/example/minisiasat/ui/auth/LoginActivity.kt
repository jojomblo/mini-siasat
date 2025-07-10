package com.example.minisiasat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.minisiasat.MainActivity
import com.example.minisiasat.R
import com.example.minisiasat.data.DatabaseNodes
import com.google.firebase.database.*
import com.example.minisiasat.domain.model.Users


class LoginActivity : AppCompatActivity() {

    private lateinit var inputKode: EditText
    private lateinit var inputPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var lupaPassowrdButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        inputKode = findViewById(R.id.inputKode)
        inputPassword = findViewById(R.id.inputPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        lupaPassowrdButton = findViewById(R.id.lupaPasswordButton)

        buttonLogin.setOnClickListener {
            val kode = inputKode.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (kode.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Kode dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(kode, password)
        }
        lupaPassowrdButton.setOnClickListener{
            AlertDialog.Builder(this)
                .setTitle("Informasi")
                .setMessage("Untuk mengatur ulang password, silakan hubungi admin kampus.")
                .setPositiveButton("OK"){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun loginUser(kode: String, password: String) {
        DatabaseNodes.usersRef.orderByChild("kode").equalTo(kode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnap in snapshot.children) {
                            val userData = userSnap.value as Map<*, *>
                            val dbPassword = userData["password"] as? String
                            val user = Users(
                                email = userData["email"] as? String,
                                name = userData["name"] as? String,
                                role = userData["role"] as? String,
                                kode = userData["kode"] as? String,
                                position = userData["position"] as? String
                            )

                            if (dbPassword == password) {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.putExtra("user", user)
                                startActivity(intent)
                                finish()
                                return
                            }
                        }
                        Toast.makeText(this@LoginActivity, "Password salah", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Kode tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "Koneksi Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
