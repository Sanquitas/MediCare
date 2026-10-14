package com.example.medicare

import android.os.Bundle
import androidx.fragment.app.FragmentActivity // Asegúrate de importar esto

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}