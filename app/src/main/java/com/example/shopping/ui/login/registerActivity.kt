package com.example.shopping.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shopping.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class registerActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnregister = findViewById<Button>(R.id.btnregister)
        val reEmail = findViewById<EditText>(R.id.reemail)
        val rePwd = findViewById<EditText>(R.id.repwd)
        val rePwd2 = findViewById<EditText>(R.id.repwd2)

        btnregister.setOnClickListener {
            val email = reEmail.text.toString().trim()
            val pwd = rePwd.text.toString().trim()
            val pwd2 = rePwd2.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "帳號不可以空白", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "帳號格式不符合", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pwd.isEmpty() || pwd2.isEmpty()) {
                Toast.makeText(this, "密碼不可以空白", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pwd != pwd2) {
                Toast.makeText(this, "密碼不一致", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerWithFirebaseAuth(email, pwd)
        }
    }

    private fun registerWithFirebaseAuth(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    Toast.makeText(this, "註冊失敗，請稍後再試", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val uid = user.uid

                val userData = hashMapOf(
                    "email" to email,
                    "name" to "",
                    "phone" to "",
                    "address" to "",
                    "birthday" to "",
                    "gender" to "",
                    "createdAt" to Timestamp.now(),
                    "provider" to "password"
                )

                db.collection("users")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "註冊成功！", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "註冊成功但寫入資料失敗：${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "註冊失敗：${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
