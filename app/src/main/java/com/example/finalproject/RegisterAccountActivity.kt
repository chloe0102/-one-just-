package com.example.finalproject

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var nicknameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var checkboxAgreement: CheckBox
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_account)

        // 初始化 FirebaseAuth 和 Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 取得 XML 元件的參照
        nicknameEditText = findViewById(R.id.nickname)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirm_password)
        spinnerGender = findViewById(R.id.spinnerGender)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        checkboxAgreement = findViewById(R.id.checkboxAgreement)
        registerButton = findViewById(R.id.emailSignInButton)

        // 設定性別選單
        val genderOptions = listOf("女性", "男性", "其他")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        // 設定狀態選單
        val statusOptions = listOf("小學生", "中學生", "高中生", "大學生", "其他")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        // 註冊按鈕的點擊事件
        registerButton.setOnClickListener {
            val nickname = nicknameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val gender = spinnerGender.selectedItem.toString()
            val status = spinnerStatus.selectedItem.toString()
            val agreementChecked = checkboxAgreement.isChecked

            if (nickname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && agreementChecked) {
                if (password == confirmPassword) {
                    registerUser(nickname, email, password, gender, status)
                } else {
                    Toast.makeText(this, "密碼不一致，請重新確認", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "請填寫所有資料並同意條款", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(nickname: String, email: String, password: String, gender: String, status: String) {
        generateRandomId { randomId ->
            if (randomId.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // 註冊成功，將用戶資料儲存到 Firestore
                            val user = auth.currentUser
                            val userData = hashMapOf(
                                "nickname" to nickname,
                                "email" to email,
                                "gender" to gender,
                                "status" to status,
                                "uid" to user?.uid,
                                "randomId" to randomId,
                                "character" to "",
                                "background" to "",
                                "profilepicture" to "",
                                "availableDraws" to 0,
                                "totalReadingTime" to 0
                            )

                            db.collection("users").document(user!!.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    initializeQuoteDictionary(user.uid)
                                    Toast.makeText(this, "註冊成功！ID: $randomId", Toast.LENGTH_SHORT).show()
                                    finish() // 註冊成功後可以返回上一頁或執行其他操作
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "儲存資料失敗：${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "註冊失敗：${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "生成用戶ID失敗，請重試", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeQuoteDictionary(userId: String) {
        val initialQuotes = listOf(
            "Don’t let the past steal your present.",
            "Be cheerful and hopeful.",
            "No cross, no crown.",
            "Sometime ever, sometime never.",
            "Treat yourself well.",
            "No fear of words, no fear of years.",
            "Better late than never.",
            "Stars can’t shine without darkness.",
            "Spend your life in your own way.",
            "Life is short and you deserve to be happy.",
            "Time is that we do not come loose.",
            "You don’t have time to be timid.",
            "It’s up to you how far you’ll go.",
            "To laugh at yourself is to love yourself.",
            "Make each day your masterpiece.",
            "Travel far enough you meet yourself.",
            "Above all, don’t lie to yourself.",
            "Light tomorrow with today."
        )

        val quotesCollection = db.collection("users").document(userId).collection("quotes")

        for ((index, quote) in initialQuotes.withIndex()) {
            val quoteId = "id${index + 1}"
            val quoteData = mapOf(
                "drawn" to false,
                "id" to quoteId,
                "text" to quote
            )

            quotesCollection.document(quoteId).set(quoteData)
                .addOnSuccessListener {
                    Log.d("RegisterAccountActivity", "語錄 $quoteId 初始化成功")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "初始化語錄失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterAccountActivity", "初始化語錄失敗", e)
                }
        }
    }


    private fun generateRandomId(callback: (String) -> Unit) {
        val randomId = (10000..99999).random().toString() // 生成五位數字亂數
        val usersRef = db.collection("users")
        usersRef.whereEqualTo("randomId", randomId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // 如果 ID 唯一
                    callback(randomId)
                } else {
                    // 如果 ID 已存在，重新生成
                    generateRandomId(callback)
                }
            }
            .addOnFailureListener {
                callback("") // 如果發生錯誤，回傳空字串
            }
    }
}
