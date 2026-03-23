package com.soorya.wealthmanager.ui.screens.reports

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
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

data class ReportState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryBreakdown: Map<String, Double> = emptyMap(),
    val symbol: String = "₹"
) {
    val balance get() = totalIncome - totalExpense
    val savingsRate get() = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100).toInt() else 0
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repo: WealthRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val end = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            val start = cal.timeInMillis

            combine(
                repo.incomeInRange(start, end),
                repo.expenseInRange(start, end),
                repo.getAll(),
                prefs.symbol
            ) { arr ->
                val income = arr[0] as Double
                val expense = arr[1] as Double
                @Suppress("UNCHECKED_CAST")
                val all = arr[2] as List<Transaction>
                val sym = arr[3] as String
                val breakdown = all
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.category }
                    .mapValues { (_, t) -> t.sumOf { it.amount } }
                    .entries.sortedByDescending { it.value }
                    .take(6)
                    .associate { it.key to it.value }
                ReportState(totalIncome = income, totalExpense = expense, categoryBreakdown = breakdown, symbol = sym)
            }.collect { _state.value = it }
        }
    }
}

@Composable
fun ReportsScreen(onBack: () -> Unit, vm: ReportsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    val total = s.categoryBreakdown.values.sum()

    Scaffold(containerColor = Pearl) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBackIosNew, null, tint = InkBlack, modifier = Modifier.size(20.dp))
                }
                Text("Reports", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionLabel("This Month")
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(InkBlack).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Balance", style = MaterialTheme.typography.bodySmall.copy(color = InkFaint))
                            Text("${s.symbol}${formatAmt(s.balance)}", style = MaterialTheme.typography.headlineMedium.copy(color = PearlLight, fontWeight = FontWeight.Bold))
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(if (s.savingsRate >= 0) Color(0xFF1A4A2A) else Color(0xFF4A1A1A))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("${if (s.savingsRate >= 0) "+" else ""}${s.savingsRate}% savings",
                                style = MaterialTheme.typography.labelMedium.copy(color = if (s.savingsRate >= 0) Color(0xFF7AE89A) else Color(0xFFE87A7A)))
                        }
                    }
                    WDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column {
                            Text("Income", style = MaterialTheme.typography.labelSmall.copy(color = InkMuted))
                            Text("+${s.symbol}${formatAmt(s.totalIncome)}", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF7AE89A), fontWeight = FontWeight.SemiBold))
                        }
                        Column {
                            Text("Spent", style = MaterialTheme.typography.labelSmall.copy(color = InkMuted))
                            Text("-${s.symbol}${formatAmt(s.totalExpense)}", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFE87A7A), fontWeight = FontWeight.SemiBold))
                        }
                    }
                }

                if (s.categoryBreakdown.isNotEmpty()) {
                    SectionLabel("Spending by Category")
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PearlLight).border(1.dp, PearlBorder, RoundedCornerShape(16.dp))) {
                        s.categoryBreakdown.entries.forEachIndexed { i, (cat, amount) ->
                            val pct = if (total > 0) (amount / total).toFloat() else 0f
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(getCategoryEmoji(cat), style = MaterialTheme.typography.titleMedium)
                                        Column {
                                            Text(cat.replace(Regex("^[^\\w\\s]+\\s*"), ""), style = MaterialTheme.typography.titleSmall)
                                            Box(modifier = Modifier.width(120.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(PearlBorder)) {
                                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(pct).background(InkBlack))
                                            }
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("-${s.symbol}${formatAmt(amount)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = MonoRed))
                                        Text("${(pct * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                if (i < s.categoryBreakdown.size - 1) WDivider(Modifier.padding(horizontal = 20.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
