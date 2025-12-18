package com.example.shopping.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shopping.R
import com.google.firebase.firestore.FirebaseFirestore

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)
        supportActionBar?.hide()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        email = intent.getStringExtra("email") ?: ""


        val data: Uri? = intent?.data
        if (data != null && data.scheme == "shoppingapp") {
            val dlEmail = data.getQueryParameter("email")
            if (!dlEmail.isNullOrEmpty()) {
                email = dlEmail
            }
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email 遺失，請重新驗證", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val etPwd = findViewById<EditText>(R.id.etNewPassword)
        val etPwd2 = findViewById<EditText>(R.id.etConfirmPassword)
        val btnReset = findViewById<Button>(R.id.btnResetPassword)

        btnReset.setOnClickListener {
            val p1 = etPwd.text.toString().trim()
            val p2 = etPwd2.text.toString().trim()

            if (p1.isEmpty() || p2.isEmpty()) {
                Toast.makeText(this, "請輸入密碼", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (p1 != p2) {
                Toast.makeText(this, "密碼不一致", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updatePasswordInFirestore(email, p1)
        }
    }

    private fun updatePasswordInFirestore(email: String, newPassword: String) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshots ->
                if (!snapshots.isEmpty) {
                    val docId = snapshots.documents[0].id
                    db.collection("users").document(docId)
                        .update("password", newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(this, "密碼重設成功", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "密碼更新失敗，請稍後再試!!", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "找不到使用者資料", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "連線錯誤，請稍後再試!!", Toast.LENGTH_SHORT).show()
            }
    }
}
