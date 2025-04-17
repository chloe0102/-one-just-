package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // 初始化 FirebaseAuth 和 Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 取得 XML 元件的參照
        emailEditText = findViewById(R.id.Edittextemail)
        passwordEditText = findViewById(R.id.Edittextpassword)
        loginButton = findViewById(R.id.emailloginButton)

        // 登入按鈕的點擊事件
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "請輸入電子郵件和密碼", Toast.LENGTH_SHORT).show()
            }
        }

        // 註冊超連結 TextView
        val registerTextView = findViewById<TextView>(R.id.registerTextView)

        // 點擊 "沒有帳號嗎？點此註冊" 跳轉到註冊頁面
        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterAccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 登入成功，檢查資料庫中的欄位
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        checkUserFields(userId)
                    }
                } else {
                    // 登入失敗
                    Toast.makeText(this, "登入失敗：${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserFields(userId: String) {
        val userRef = db.collection("users").document(userId)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val character = document.getString("character")
                    val background = document.getString("background")
                    val profilePicture = document.getString("profilepicture")

                    if (character.isNullOrEmpty() || background.isNullOrEmpty() || profilePicture.isNullOrEmpty()) {
                        // 如果有欄位為空，跳轉到 LoginChooseActivity
                        val intent = Intent(this, LoginChooseActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // 所有欄位非空，跳轉到 MainActivity
                        Toast.makeText(this, "登入成功！", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, "使用者資料不存在！", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "資料檢查失敗：${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
