package com.example.shopping.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.shopping.R
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.utils.FavoriteManager
import com.example.shopping.utils.FavoriteRepository
import com.example.shopping.utils.UserSession
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.Timestamp

import com.linecorp.linesdk.*
import com.linecorp.linesdk.auth.*

import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var googleClient: GoogleSignInClient
    private lateinit var fbCallback: CallbackManager

    private val RC_GOOGLE = 1001
    private val RC_LINE = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupGoogle()
        setupFacebook()

        val etEmail = findViewById<EditText>(R.id.login)
        val etPassword = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.button)
        val btnRegister = findViewById<Button>(R.id.register)
        val btnForget = findViewById<Button>(R.id.forgetbtn)

        val btnGoogle = findViewById<ImageButton>(R.id.btngoogle)
        val btnFacebook = findViewById<ImageButton>(R.id.btnfacebook)
        val btnLine = findViewById<ImageButton>(R.id.btnline)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pwd = etPassword.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "信箱格式錯誤", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (pwd.isEmpty()) {
                Toast.makeText(this, "密碼不可空白", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            loginEmail(email, pwd)
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, registerActivity::class.java))
        }

        btnForget.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }

        btnGoogle.setOnClickListener {
            startActivityForResult(googleClient.signInIntent, RC_GOOGLE)
        }

        btnFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this, listOf("email", "public_profile")
            )
        }

        btnLine.setOnClickListener { startLineLogin() }
    }

    private fun loginEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                handleLoginSuccess(it.user!!)
            }
            .addOnFailureListener {
                Toast.makeText(this, "登入失敗：${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)
    }

    private fun firebaseAuthWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        signInWithCredentialAutoMerge(credential)
    }

    private fun setupFacebook() {
        fbCallback = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(
            fbCallback,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    signInWithCredentialAutoMerge(credential)
                }

                override fun onCancel() {}

                override fun onError(e: FacebookException) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Facebook 錯誤：${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun startLineLogin() {
        val params = LineAuthenticationParams.Builder()
            .scopes(listOf(Scope.PROFILE))
            .build()

        val intent = LineLoginApi.getLoginIntent(
            this,
            getString(R.string.line_channel_id),
            params
        )

        startActivityForResult(intent, RC_LINE)
    }

    private fun loginWithLineToServer(lineUserId: String, name: String) {
        val json = JSONObject().apply {
            put("lineUserId", lineUserId)
            put("name", name)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "https://line-worker.jmj987654321.workers.dev/line-login",
            json,
            { response ->

                val customToken = response.getString("customToken")

                auth.signInWithCustomToken(customToken)
                    .addOnSuccessListener {
                        handleLoginSuccess(it.user!!,name)
                    }
            },
            { error ->
                Toast.makeText(this, "LINE API 錯誤：${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }


    private fun signInWithCredentialAutoMerge(credential: AuthCredential) {

        val currentUser = auth.currentUser


        if (currentUser != null) {

            val providerAlreadyLinked = currentUser.providerData.any {
                it.providerId == credential.provider
            }

            if (providerAlreadyLinked) {

                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        handleLoginSuccess(it.user!!,it.user!!.displayName)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "登入錯誤：${e.message}", Toast.LENGTH_LONG).show()
                    }
                return
            }


            currentUser.linkWithCredential(credential)
                .addOnSuccessListener {
                    handleLoginSuccess(it.user!!)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "合併帳號失敗：${e.message}", Toast.LENGTH_LONG).show()
                }

            return
        }

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                handleLoginSuccess(it.user!!,it.user!!.displayName)
            }
            .addOnFailureListener { e ->


                if (e is FirebaseAuthUserCollisionException) {
                    val email = e.email ?: return@addOnFailureListener

                    auth.fetchSignInMethodsForEmail(email)
                        .addOnSuccessListener { result ->
                            val methods = result.signInMethods ?: emptyList()

                            when {
                                methods.contains("password") ->
                                    Toast.makeText(
                                        this,
                                        "此帳號使用 Email 註冊，請先用 Email/密碼登入再綁定其他登入方式。",
                                        Toast.LENGTH_LONG
                                    ).show()

                                methods.contains("google.com") ->
                                    Toast.makeText(
                                        this,
                                        "請改用 Google 登入以綁定帳號。",
                                        Toast.LENGTH_LONG
                                    ).show()

                                methods.contains("facebook.com") ->
                                    Toast.makeText(
                                        this,
                                        "請改用 Facebook 登入以綁定帳號。",
                                        Toast.LENGTH_LONG
                                    ).show()

                                else ->
                                    Toast.makeText(
                                        this,
                                        "此 Email 可能被其他登入方式占用，無法自動合併。",
                                        Toast.LENGTH_LONG
                                    ).show()
                            }
                        }

                } else {
                    Toast.makeText(this, "登入錯誤：${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }


    private fun loginSuccess(user: FirebaseUser) {
        UserSession.isLogin = true
        UserSession.email = user.email ?: ""
        UserSession.documentId = user.uid

        FavoriteRepository.getFavorites { firebaseIds ->
            FavoriteManager.setFavorites(
                context = this,
                ids = firebaseIds
            )
            goMainAfterLogin(user)
            finish()
        }

    }
    private fun handleLoginSuccess(user: FirebaseUser, forceName: String? = null) {
        val uid = user.uid
        val userRef = db.collection("users").document(uid)

        userRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) {

                val userData = mapOf(
                    "uid" to uid,
                    "email" to (user.email ?: ""),
                    "name" to (forceName ?: user.displayName ?: ""),
                    "createdAt" to Timestamp.now(),
                    "provider" to user.providerData.joinToString { it.providerId }

                )

                userRef.set(userData)
                    .addOnSuccessListener {
                        grantWelcomeCoupon(uid)
                        loginSuccess(user)
                    }
            } else {
                loginSuccess(user)
            }
        }
    }
    private fun grantWelcomeCoupon(userId: String) {
        val couponRef = db.collection("users")
            .document(userId)
            .collection("coupons")
            .document("WELCOME10")

        couponRef.get().addOnSuccessListener { snap ->
            if (snap.exists()) return@addOnSuccessListener

            val coupons = listOf(
                Pair(
                    "WELCOME10",
                    mapOf(
                        "title" to "新會員 9 折券",
                        "type" to "PERCENT",
                        "value" to 10,
                        "minSpend" to 0,
                        "expireDate" to "2026/02/15",
                        "used" to false,
                        "createdAt" to Timestamp.now()
                    )
                ),
                Pair(
                    "SAVE300",
                    mapOf(
                        "title" to "滿 3000 折 300",
                        "type" to "AMOUNT",
                        "value" to 300,
                        "minSpend" to 3000,
                        "expireDate" to "2026/01/31",
                        "used" to false,
                        "createdAt" to Timestamp.now()
                    )
                )
            )
            val batch = db.batch()

            coupons.forEach { (couponId, data) ->
                val ref = db.collection("users")
                    .document(userId)
                    .collection("coupons")
                    .document(couponId)
                batch.set(ref, data)
            }
            batch.commit()
        }
    }

    private fun goMainAfterLogin(user:FirebaseUser){
        val hasPassword = user.providerData.any {
            it.providerId == EmailAuthProvider.PROVIDER_ID
        }
        if (!hasPassword && user.email != null) {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    putExtra("goSetPassword", true)
                }
            )
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fbCallback.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.result
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                Toast.makeText(this, "Google 登入錯誤：${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        if (requestCode == RC_LINE) {
            val result = LineLoginApi.getLoginResultFromIntent(data)
            if (result.responseCode == LineApiResponseCode.SUCCESS) {
                loginWithLineToServer(result.lineProfile!!.userId, result.lineProfile!!.displayName)
            } else {
                Toast.makeText(this, "LINE 登入失敗", Toast.LENGTH_LONG).show()
            }
        }
    }
}
