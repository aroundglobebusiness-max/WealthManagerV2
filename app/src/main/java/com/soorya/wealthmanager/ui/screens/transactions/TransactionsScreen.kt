package com.soorya.wealthmanager.ui.screens.transactions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soorya.wealthmanager.data.repository.WealthRepository
import com.soorya.wealthmanager.domain.model.*
import com.soorya.wealthmanager.ui.components.*
import com.soorya.wealthmanager.ui.theme.*
import com.soorya.wealthmanager.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repo: WealthRepository,
    private val prefs: PreferencesManager
) : ViewModel() {
    val transactions = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val symbol = prefs.symbol.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "₹")

    fun delete(id: Long) = viewModelScope.launch { repo.delete(id) }
}

@Composable
fun TransactionsScreen(onBack: () -> Unit, vm: TransactionsViewModel = hiltViewModel()) {
    val txns by vm.transactions.collectAsState()
    val symbol by vm.symbol.collectAsState()
    val haptic = LocalHapticFeedback.current

    val grouped = txns.groupBy { t ->
        val cal = Calendar.getInstance().apply { timeInMillis = t.date }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        when {
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
            cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
            else -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(t.date))
        }
    }

    Scaffold(containerColor = Pearl) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBackIosNew, null, tint = InkBlack, modifier = Modifier.size(20.dp))
                }
                Text("Transactions", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            }

            if (txns.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("💳", style = MaterialTheme.typography.displayMedium)
                        Text("No transactions yet", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    grouped.forEach { (day, list) ->
                        item { SectionLabel(day) }
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PearlLight).border(1.dp, PearlBorder, RoundedCornerShape(16.dp))
                            ) {
                                list.forEachIndexed { i, txn ->
                                    var dismissed by remember { mutableStateOf(false) }
                                    if (!dismissed) {
                                        TransactionRow(
                                            txn = txn,
                                            symbol = symbol,
                                            showDivider = i < list.size - 1,
                                            onClick = {}
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
