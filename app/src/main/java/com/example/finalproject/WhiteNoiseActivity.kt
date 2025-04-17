package com.example.finalproject

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WhiteNoiseActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private var currentIndex = 0

    // 白噪音列表
    private val noiseList = listOf(
        Pair("細雨", R.raw.drizzle),
        Pair("雨天", R.raw.rain),
        Pair("海浪", R.raw.waves),
        Pair("流水", R.raw.running_water),
        Pair("篝火", R.raw.bonfire),
        Pair("蟲鳴", R.raw.insect_chirping),
        Pair("鳥叫", R.raw.bird_chirping),
        Pair("蛙鳴", R.raw.frog_croaking),
        Pair("雜訊", R.raw.noise),
        Pair("溪河", R.raw.stream),
        Pair("森林", R.raw.forest),
        Pair("庭院", R.raw.courtyard)
    )

    private lateinit var nowPlayingText: TextView
    private lateinit var playButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.white_noise)

        nowPlayingText = findViewById(R.id.nowPlayingText)
        playButton = findViewById(R.id.playButton)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)

        setupButtons()
        updateNowPlaying()
    }

    private fun setupButtons() {
        // 按钮点击事件
        findViewById<ImageButton>(R.id.drizzleButton).setOnClickListener { playNoise(0) }
        findViewById<ImageButton>(R.id.rainButton).setOnClickListener { playNoise(1) }
        findViewById<ImageButton>(R.id.wavesButton).setOnClickListener { playNoise(2) }
        findViewById<ImageButton>(R.id.runningwaterButton).setOnClickListener { playNoise(3) }
        findViewById<ImageButton>(R.id.bonfireButton).setOnClickListener { playNoise(4) }
        findViewById<ImageButton>(R.id.insectchirpingButton).setOnClickListener { playNoise(5) }
        findViewById<ImageButton>(R.id.birdchirpingButton).setOnClickListener { playNoise(6) }
        findViewById<ImageButton>(R.id.frogcroakingButton).setOnClickListener { playNoise(7) }
        findViewById<ImageButton>(R.id.NoiseButton).setOnClickListener { playNoise(8) }
        findViewById<ImageButton>(R.id.streamButton).setOnClickListener { playNoise(9) }
        findViewById<ImageButton>(R.id.forestButton).setOnClickListener { playNoise(10) }
        findViewById<ImageButton>(R.id.courtyardButton).setOnClickListener { playNoise(11) }

        playButton.setOnClickListener { togglePlayPause() }
        previousButton.setOnClickListener { playPrevious() }
        nextButton.setOnClickListener { playNext() }

        // 返回按钮设置
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun playNoise(index: Int) {
        currentIndex = index
        playCurrent()
        updatePlayButtonState(true)
    }

    private fun playCurrent() {
        releaseMediaPlayer()
        val noise = noiseList[currentIndex]
        mediaPlayer = MediaPlayer.create(this, noise.second)
        mediaPlayer.start()
        updateNowPlaying()
        updatePlayButtonState(true)
    }

    private fun togglePlayPause() {
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                updatePlayButtonState(false)
            } else {
                mediaPlayer.start()
                updatePlayButtonState(true)
            }
        }
    }

    private fun playPrevious() {
        currentIndex = if (currentIndex > 0) currentIndex - 1 else noiseList.size - 1
        playCurrent()
    }

    private fun playNext() {
        currentIndex = if (currentIndex < noiseList.size - 1) currentIndex + 1 else 0
        playCurrent()
    }

    private fun updateNowPlaying() {
        val noise = noiseList[currentIndex]
        nowPlayingText.text = "${noise.first} — 白噪音"
    }

    private fun updatePlayButtonState(isPlaying: Boolean) {
        if (isPlaying) {
            playButton.setImageResource(R.drawable.baseline_pause_gray) // 确保此为暂停图标
        } else {
            playButton.setImageResource(R.drawable.baseline_play_arrow_gray) // 确保此为播放图标
        }
    }


    private fun releaseMediaPlayer() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }
}
