package com.example.videodownloader

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.videodownloader.databinding.ActivityMainBinding
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.downloadVideoButton.setOnClickListener {
            if(!binding.videoUrl.text.isBlank()){
            val intent = Intent(this, DownloadService::class.java).apply {
                action = DownloadService.ACTION_START
                putExtra("VideoURL",binding.videoUrl.text)
            }

                startService(intent)

        }
            else{
                Toast.makeText(this,"you must provide a video url",Toast.LENGTH_LONG).show()
            }
        }
    }
}