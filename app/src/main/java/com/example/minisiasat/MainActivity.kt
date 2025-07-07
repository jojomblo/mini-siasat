package com.example.minisiasat

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.example.minisiasat.utils.DatabaseNodes
import com.example.minisiasat.utils.Users
import com.google.firebase.database.getValue

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val database = Firebase.database("https://mini-siasat-a36ae-default-rtdb.firebaseio.com")
        val usersRef = database.getReference(DatabaseNodes.USERS)

        val textView = findViewById<TextView>(R.id.textView)
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val builder = StringBuilder()
                for (userSnapshot in snapshot.children){
                    val user = userSnapshot.getValue(Users::class.java)
                    val name = user?.name?:"-"
                    val email = user?.email?:"-"
                    builder.append("Name: $name\nEmail: $email\n\n")
                }
                textView.text =  if (builder.isNotEmpty()) builder.toString() else "NO DATA"
            }

            override fun onCancelled(error: DatabaseError) {
                textView.text = "ERROR: ${error.message}"
            }
        })


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}