package com.example.shopping.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shopping.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions


class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forget_password)
        supportActionBar?.hide()

        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etAccount = findViewById<EditText>(R.id.etEmail)
        val btnNext = findViewById<Button>(R.id.btnNext)

        btnNext.setOnClickListener {
            val account = etAccount.text.toString().trim()

            if (account.isEmpty()) {
                Toast.makeText(this, "請輸入 Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(account).matches()) {
                etAccount.error = "請輸入正確的 Email 格式"
                return@setOnClickListener
            }

            checkEmailExists(account)
        }
    }

    private fun checkEmailExists(email: String) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(this, "此 Email 尚未註冊", Toast.LENGTH_SHORT).show()
                } else {
                    val code = generateVerifyCode()
                    val documentId = querySnapshot.documents[0].id


                    db.collection("users").document(documentId)
                        .update("resetCode", code)
                        .addOnSuccessListener {


                            sendEmailCode(email, code)


                            val intent = Intent(this, VerifyCodeActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "寫入驗證碼失敗", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "連線失敗，請稍後再試", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendEmailCode(email: String, code: String) {
        Log.d("DEBUG", "sendEmailCode() called: $email / $code")

        val functions = Firebase.functions

        val data = hashMapOf(
            "email" to email,
            "code" to code
        )

        functions
            .getHttpsCallable("sendResetCode")
            .call(data)
            .addOnSuccessListener {
                Toast.makeText(this, "驗證碼已寄送！", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("EMAIL", "sendResetCode failed", e)
                Toast.makeText(
                    this,
                    e.localizedMessage ?: "寄送失敗",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun generateVerifyCode(): String {
        return (100000..999999).random().toString()
    }


}
