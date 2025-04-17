package com.example.finalproject

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.finalproject.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // 初始化 View Binding
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // 设置 Toolbar 作為 ActionBar
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.elevation = 0f

            // 查找 NavHostFragment
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav) as? NavHostFragment
            if (navHostFragment == null) {
                Log.e("MainActivity", "NavHostFragment is null")
                Toast.makeText(this, "初始化失敗，請檢查 FragmentContainerView 的設定", Toast.LENGTH_SHORT).show()
                return
            }

            // 獲取 NavController
            navController = navHostFragment.navController

            // 配置 BottomNavigationView
            val bottomNavigationView: BottomNavigationView = binding.navView

            // 配置 AppBarConfiguration
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_room,
                    R.id.navigation_social,
                    R.id.navigation_book,
                    R.id.navigation_profile
                )
            )

            // 設置 ActionBar 和 NavController 的連動
            setupActionBarWithNavController(navController, appBarConfiguration)

            // 設置 BottomNavigationView 與 NavController 的連動
            bottomNavigationView.setupWithNavController(navController)

            // 添加自定義行為（僅在必要時處理特定菜單項的額外邏輯）
            bottomNavigationView.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.navigation_room,
                    R.id.navigation_social,
                    R.id.navigation_book,
                    R.id.navigation_profile -> {

                        navController.navigate(menuItem.itemId)
                        true
                    }
                    else -> false
                }
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "初始化錯誤，請檢查配置", Toast.LENGTH_SHORT).show()
        }
    }
}
