package com.example.aijournalcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.aijournalcompanion.network.HttpJournalApi
import com.example.aijournalcompanion.ui.journal.JournalRoute
import com.example.aijournalcompanion.ui.journal.JournalViewModel
import com.example.aijournalcompanion.ui.theme.AIJournalCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(
            this,
            JournalViewModel.Factory(HttpJournalApi(applicationContext)),
        )[JournalViewModel::class.java]
        setContent {
            AIJournalCompanionTheme {
                JournalRoute(viewModel)
            }
        }
    }
}
