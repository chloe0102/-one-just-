package com.example.finalproject

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class DressUpActivity : AppCompatActivity() {

    private var selectedBackground: String? = null
    private var selectedCharacter: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dressup)

        // 初始化 Firebase 和 SharedPreferences
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("DressUpPrefs", MODE_PRIVATE)

        // 返回按鈕
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // 初始化 UI
        val targetImage = findViewById<ImageView>(R.id.background_image)
        val characterIcon = findViewById<ImageView>(R.id.character_icon)

        // 從 Firestore 加載設置
        loadSettingsFromDatabase { background, character ->
            selectedBackground = background
            selectedCharacter = character

            // 設置背景和角色圖片
            background?.let {
                val resId = resources.getIdentifier(it, "drawable", packageName)
                targetImage.setImageResource(resId)
            }

            character?.let {
                val resId = resources.getIdentifier(it, "drawable", packageName)
                characterIcon.setImageResource(resId)
            }
        }

        // 背景圖片選擇
        val imageMap = mapOf(
            R.id.background01 to "background01",
            R.id.background02 to "background02",
            R.id.background03 to "background03",
            R.id.background04 to "background04",
            R.id.background05 to "background05"
        )
        imageMap.forEach { (viewId, backgroundName) ->
            val imageView = findViewById<ImageView>(viewId)
            imageView.setOnClickListener { clickedView ->
                val clickedImageView = clickedView as ImageView
                resetSelection(imageMap.keys)
                selectedBackground = backgroundName
                setImageHighlight(clickedImageView, true)
                val resId = resources.getIdentifier(backgroundName, "drawable", packageName)
                targetImage.setImageResource(resId)
            }
        }

        // 角色圖片選擇
        val characterMap = mapOf(
            R.id.female01 to "female01",
            R.id.female02 to "female02",
            R.id.female03 to "female03",
            R.id.female04 to "female04",
            R.id.female05 to "female05"
        )
        characterMap.forEach { (viewId, characterName) ->
            val imageView = findViewById<ImageView>(viewId)
            imageView.setOnClickListener { clickedView ->
                val clickedImageView = clickedView as ImageView
                resetSelection(characterMap.keys)
                selectedCharacter = characterName
                setImageHighlight(clickedImageView, true)
                val resId = resources.getIdentifier(characterName, "drawable", packageName)
                characterIcon.setImageResource(resId)
            }
        }

        // 儲存按鈕
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null && selectedBackground != null && selectedCharacter != null) {
                val userUpdates = mapOf(
                    "background" to selectedBackground,
                    "character" to selectedCharacter
                )
                db.collection("users").document(userId)
                    .set(userUpdates, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "儲存成功！", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "儲存失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "請先選擇背景和角色！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 设置高亮效果
    private fun setImageHighlight(imageView: ImageView, isHighlighted: Boolean) {
        if (isHighlighted) {
            // 添加半透明灰色滤镜
            imageView.setColorFilter(
                ContextCompat.getColor(this, R.color.gray),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )
        } else {
            // 清除滤镜
            imageView.clearColorFilter()
        }
    }

    // 重置所有图片的高亮状态
    private fun resetSelection(keys: Set<Int>) {
        keys.forEach { key ->
            val imageView = findViewById<ImageView>(key)
            setImageHighlight(imageView, false)
        }
    }

    // 从 Firestore 加载设置
    private fun loadSettingsFromDatabase(callback: (String?, String?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val background = document.getString("background")
                    val character = document.getString("character")
                    callback(background, character)
                } else {
                    callback(null, null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "無法加載數據: ${it.message}", Toast.LENGTH_SHORT).show()
                callback(null, null)
            }
    }
}
