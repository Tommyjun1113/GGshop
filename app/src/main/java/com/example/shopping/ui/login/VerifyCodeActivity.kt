package com.example.shopping.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.shopping.R
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

class VerifyCodeActivity : AppCompatActivity() {
    private var timer: CountDownTimer?=null
    private var canResend = false
    private lateinit var txtResend: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var email:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_code)
        supportActionBar?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = FirebaseFirestore.getInstance()
        email = intent.getStringExtra("email") ?:""
        val btnCheck = findViewById<Button>(R.id.btnCheck)
        val etCode = findViewById<EditText>(R.id.etVerifyCode)
        txtResend = findViewById(R.id.tvResend)

        startTimer()
        btnCheck.setOnClickListener {
            val inputCode = etCode.text.toString().trim()

            if (inputCode.isEmpty()) {
                Toast.makeText(this, "請輸入驗證碼", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            checkCodeInFirestore(email, inputCode)
        }

        txtResend.setOnClickListener {
            if (canResend) {
                val code = (100000..999999).random().toString()
                Toast.makeText(this, "正在重新發送驗證碼...", Toast.LENGTH_SHORT).show()
                db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { snap ->
                        if (!snap.isEmpty) {
                            snap.documents[0].reference.update("resetCode", code)
                                .addOnSuccessListener {
                                    sendEmailCode(email, code)
                                    startTimer()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "重新寫入驗證碼失敗，請稍後再試",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        } else {
                            Toast.makeText(this, "錯誤：此 Email 紀錄不存在", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "連線失敗，請檢查網路或稍後再試", Toast.LENGTH_LONG)
                            .show()
                    }
            }
        }
    }
    private fun checkCodeInFirestore(email:String, inputCode: String){
        db.collection("users")
            .whereEqualTo("email",email)
            .get()
            .addOnSuccessListener { snapshots ->
                if(!snapshots.isEmpty){
                    val doc = snapshots.documents[0]
                    val realCode = doc.getString("resetCode")
                    if(inputCode == realCode){
                        Toast.makeText(this,"驗證成功", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ResetPasswordActivity::class.java)
                        intent.putExtra("email",email)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this,"驗證碼錯誤", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this,"驗證失敗，請稍後在試~", Toast.LENGTH_SHORT).show()
            }
    }
    private fun sendEmailCode(email: String, code: String) {
        val url = "https://ggshop-tkg.pages.dev/api/send-code"


        val json = JSONObject().apply {
            put("to", email)
            put("code", code)
        }

        val request = object : JsonObjectRequest(
            Method.POST, url, json,
            { response ->
                Toast.makeText(this, "寄送成功！", Toast.LENGTH_SHORT).show()
            },
            { error ->
                val status = error.networkResponse?.statusCode
                val data = error.networkResponse?.data?.let { String(it) }
                val errorMessage = when (status) {
                    405 -> "重寄失敗：後端不允許 POST 方法 (405)"
                    401, 403 -> "重寄失敗：授權無效或金鑰錯誤 ($status)"
                    else -> "重寄失敗：HTTP $status"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                println("ERROR BODY: $data")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["User-Agent"] = "Android-App"
                return headers
            }
        }


        request.retryPolicy = com.android.volley.DefaultRetryPolicy(
            7000,
            1,
            1f
        )

        Volley.newRequestQueue(this).add(request)
    }
    private fun startTimer(){
        canResend = false
        txtResend.isEnabled = false

        timer?.cancel()
        timer = object : CountDownTimer(6000,1000){
            override fun onTick(ms: Long) {
                txtResend.text="重寄驗證碼(${ms / 1000})"
            }
            override fun onFinish() {
                txtResend.text= "重寄驗證碼"
                txtResend.isEnabled=true
                canResend= true
            }
        }.start()
    }

}