package com.soorya.wealthmanager.ui.screens.dashboard

import androidx.compose.animation.*
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
import java.util.Calendar
import javax.inject.Inject

data class DashboardState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val recent: List<Transaction> = emptyList(),
    val symbol: String = "₹",
    val isSyncing: Boolean = false,
    val syncMsg: String? = null
) {
    val balance get() = totalIncome - totalExpense
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: WealthRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val end = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0)
            val start = cal.timeInMillis

            combine(
                repo.totalIncome(), repo.totalExpense(),
                repo.incomeInRange(start, end), repo.expenseInRange(start, end),
                repo.getRecent(), prefs.symbol
            ) { vals ->
                DashboardState(
                    totalIncome = vals[0] as Double,
                    totalExpense = vals[1] as Double,
                    monthlyIncome = vals[2] as Double,
                    monthlyExpense = vals[3] as Double,
                    @Suppress("UNCHECKED_CAST")
                    recent = vals[4] as List<Transaction>,
                    symbol = vals[5] as String
                )
            }.collect { _state.value = it }
        }
    }

    fun sync() = viewModelScope.launch {
        _state.update { it.copy(isSyncing = true) }
        val token = prefs.token.first()
        val dbId = prefs.dbId.first()
        val count = repo.syncPending(token, dbId)
        _state.update { it.copy(isSyncing = false, syncMsg = if (count > 0) "Synced $count items" else "All synced") }
    }

    fun clearMsg() = _state.update { it.copy(syncMsg = null) }
}

@Composable
fun DashboardScreen(
    onAdd: () -> Unit,
    onViewAll: () -> Unit,
    onReports: () -> Unit,
    onSettings: () -> Unit,
    vm: DashboardViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = Pearl,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onAdd() },
                containerColor = InkBlack,
                contentColor = PearlLight,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(24.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wealth", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (s.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp).align(Alignment.CenterVertically), strokeWidth = 2.dp, color = InkBlack)
                        }
                        IconButton(onClick = { vm.sync() }) {
                            Icon(Icons.Rounded.Sync, null, tint = InkLight, modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Rounded.Settings, null, tint = InkLight, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            // Balance Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(InkBlack)
                        .padding(28.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Total Balance", style = MaterialTheme.typography.bodySmall.copy(color = InkFaint))
                        Text(
                            "${s.symbol}${"%,.0f".format(s.balance)}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = PearlLight,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text("Income", style = MaterialTheme.typography.labelSmall.copy(color = InkMuted))
                                Text("+${s.symbol}${formatAmt(s.monthlyIncome)}", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF7AE89A), fontWeight = FontWeight.SemiBold))
                            }
                            Column {
                                Text("Spent", style = MaterialTheme.typography.labelSmall.copy(color = InkMuted))
                                Text("-${s.symbol}${formatAmt(s.monthlyExpense)}", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFE87A7A), fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Sync message
            s.syncMsg?.let { msg ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(10.dp)).background(PearlSurface).padding(12.dp, 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(msg, style = MaterialTheme.typography.bodySmall)
                        IconButton(onClick = vm::clearMsg, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Rounded.Close, null, modifier = Modifier.size(14.dp), tint = InkMuted)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionBtn(icon = Icons.Rounded.BarChart, label = "Reports", modifier = Modifier.weight(1f)) { onReports() }
                    QuickActionBtn(icon = Icons.Rounded.History, label = "History", modifier = Modifier.weight(1f)) { onViewAll() }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recent
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Recent")
                    TextButton(onClick = onViewAll) {
                        Text("See all", style = MaterialTheme.typography.labelMedium.copy(color = InkLight))
                    }
                }
            }

            if (s.recent.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) {
                        Text("No transactions yet", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(16.dp)).background(PearlLight).border(1.dp, PearlBorder, RoundedCornerShape(16.dp))
                    ) {
                        s.recent.forEachIndexed { i, txn ->
                            TransactionRow(txn = txn, symbol = s.symbol, showDivider = i < s.recent.size - 1)
                        }
                    }
                }
            }

            // Developer Credit
            item {
                Box(Modifier.fillMaxWidth().padding(20.dp, 32.dp), Alignment.Center) {
                    Text("Soorya × Claude", style = MaterialTheme.typography.labelSmall.copy(color = InkMuted, letterSpacing = 1.sp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PearlLight)
            .border(1.dp, PearlBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = InkBlack, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
