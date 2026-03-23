package com.soorya.wealthmanager.ui.screens.addtransaction

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val title: String = "",
    val category: String = "",
    val note: String = "",
    val symbol: String = "₹",
    val currency: String = "INR",
    val suggestions: List<String> = emptyList(),
    val isSaving: Boolean = false
)

val EXPENSE_CATS = listOf("⛽ Petrol","🍽 Food","🛍 Shopping","🚌 Transport","💊 Health","🏠 Rent","📱 Bills","🎬 Entertainment","✈️ Travel","📚 Education","↔️ Transfer","💳 Other")
val INCOME_CATS = listOf("💼 Salary","💻 Freelance","📈 Investment","🎁 Gift","💰 Refund","🏦 Business","💳 Other")

@HiltViewModel
class AddViewModel @Inject constructor(
    private val repo: WealthRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(AddState())
    val state = _state.asStateFlow()
    private var suggJob: Job? = null

    init {
        viewModelScope.launch {
            combine(prefs.currency, prefs.symbol) { c, s -> c to s }
                .collect { (c, s) -> _state.update { it.copy(currency = c, symbol = s) } }
        }
    }

    fun setType(t: TransactionType) = _state.update { it.copy(type = t, category = "") }
    fun setAmount(v: String) = _state.update { it.copy(amount = v) }
    fun setTitle(v: String) = _state.update { it.copy(title = v) }
    fun setCategory(v: String) = _state.update { it.copy(category = v) }
    fun setNote(v: String) = _state.update { it.copy(note = v) }

    fun loadSuggestions(q: String) {
        suggJob?.cancel()
        if (q.length < 2) { _state.update { it.copy(suggestions = emptyList()) }; return }
        suggJob = viewModelScope.launch {
            delay(300)
            _state.update { it.copy(suggestions = repo.suggestions(q)) }
        }
    }

    fun applySuggestion(t: String) = viewModelScope.launch {
        val last = repo.lastByTitle(t)
        _state.update { s -> s.copy(title = t, category = last?.category ?: s.category, amount = if (last != null) last.amount.toString() else s.amount, suggestions = emptyList()) }
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        val amount = s.amount.toDoubleOrNull() ?: return
        if (s.category.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val token = prefs.token.first()
            val dbId = prefs.dbId.first()
            repo.add(Transaction(type = s.type, amount = amount, currency = s.currency, symbol = s.symbol, title = s.title.ifEmpty { s.category }, category = s.category, note = s.note), token, dbId)
            _state.update { it.copy(isSaving = false) }
            onDone()
        }
    }
}

@Composable
fun AddTransactionScreen(onDismiss: () -> Unit, vm: AddViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    val cats = if (s.type == TransactionType.EXPENSE) EXPENSE_CATS else INCOME_CATS

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)).clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(PearlLight)
                .clickable(enabled = false, onClick = {})
                .navigationBarsPadding().imePadding()
        ) {
            SheetHandle()
            SheetHeader("Add Transaction") { onDismiss() }

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PearlSurface).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TransactionType.values().forEach { type ->
                        val sel = s.type == type
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .background(if (sel) InkBlack else Color.Transparent)
                                .clickable { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.setType(type) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (type == TransactionType.EXPENSE) "↓  Expense" else "↑  Income",
                                style = MaterialTheme.typography.labelLarge.copy(color = if (sel) PearlLight else InkLight, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                            )
                        }
                    }
                }

                // Amount
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PearlSurface).padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.symbol, style = MaterialTheme.typography.headlineMedium.copy(color = InkMuted))
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = s.amount, onValueChange = vm::setAmount,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = { Text("0.00", style = MaterialTheme.typography.headlineMedium.copy(color = InkFaint)) },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(color = InkBlack, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
                        singleLine = true
                    )
                }

                WInput(value = s.title, onValueChange = { vm.setTitle(it); vm.loadSuggestions(it) }, placeholder = "What was this for?",
                    leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = InkMuted, modifier = Modifier.size(18.dp)) })

                if (s.suggestions.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.suggestions) { sugg ->
                            AssistChip(
                                onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.applySuggestion(sugg) },
                                label = { Text(sugg, style = MaterialTheme.typography.labelMedium) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = PearlSurface, labelColor = InkBlack),
                                border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = PearlBorder)
                            )
                        }
                    }
                }

                WInput(value = s.note, onValueChange = vm::setNote, placeholder = "Note (optional)",
                    leadingIcon = { Icon(Icons.Rounded.Notes, null, tint = InkMuted, modifier = Modifier.size(18.dp)) })

                Text("Category", style = MaterialTheme.typography.labelMedium.copy(color = InkMuted))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cats.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { cat ->
                                val sel = s.category == cat
                                Box(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                        .background(if (sel) InkBlack else PearlSurface)
                                        .border(1.dp, if (sel) InkBlack else PearlBorder, RoundedCornerShape(10.dp))
                                        .clickable { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.setCategory(cat) }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat, style = MaterialTheme.typography.labelSmall.copy(color = if (sel) PearlLight else InkLight), maxLines = 1)
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                WButton(text = "Save Transaction", onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); vm.save(onDismiss) }, loading = s.isSaving, enabled = s.amount.isNotEmpty() && s.category.isNotEmpty())
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
