package com.example.aijournalcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aijournalcompanion.ui.journal.JournalScreen
import com.example.aijournalcompanion.ui.theme.AIJournalCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIJournalCompanionTheme {
                JournalScreen()
            }
        }
    }
}
