package com.soorya.wealthmanager.ui.screens.settings

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soorya.wealthmanager.data.repository.WealthRepository
import com.soorya.wealthmanager.ui.components.*
import com.soorya.wealthmanager.ui.theme.*
import com.soorya.wealthmanager.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val token: String = "",
    val dbId: String = "",
    val currency: String = "INR",
    val symbol: String = "₹",
    val testing: Boolean = false,
    val connected: Boolean? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val repo: WealthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(prefs.token, prefs.dbId, prefs.currency, prefs.symbol) { t, d, c, s ->
                SettingsState(token = t, dbId = d, currency = c, symbol = s)
            }.collect { _state.value = it }
        }
    }

    fun setToken(v: String) = _state.update { it.copy(token = v, connected = null) }
    fun setDbId(v: String) = _state.update { it.copy(dbId = v, connected = null) }

    fun saveAndTest() = viewModelScope.launch {
        val s = _state.value
        _state.update { it.copy(testing = true) }
        prefs.saveNotion(s.token.trim(), s.dbId.trim())
        val ok = repo.testNotion(s.dbId.trim())
        _state.update { it.copy(testing = false, connected = ok) }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showToken by remember { mutableStateOf(false) }

    Scaffold(containerColor = Pearl) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBackIosNew, null, tint = InkBlack, modifier = Modifier.size(20.dp))
                }
                Text("Settings", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Notion Section
                SectionLabel("Notion Integration")
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PearlLight).border(1.dp, PearlBorder, RoundedCornerShape(16.dp)).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Connect to your Notion database to sync every transaction automatically.", style = MaterialTheme.typography.bodySmall)

                    WInput(
                        value = s.token,
                        onValueChange = vm::setToken,
                        placeholder = "Integration token (secret_...)",
                        visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showToken = !showToken }) {
                                Icon(if (showToken) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = InkMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    )
                    WInput(value = s.dbId, onValueChange = vm::setDbId, placeholder = "Database ID (from Notion URL)")

                    // Status
                    s.connected?.let { ok ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(if (ok) MonoGreenBg else MonoRedBg).padding(12.dp)
                        ) {
                            Icon(
                                if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                                null,
                                tint = if (ok) MonoGreen else MonoRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                if (ok) "Connected to Notion" else "Connection failed. Check your credentials.",
                                style = MaterialTheme.typography.bodySmall.copy(color = if (ok) MonoGreen else MonoRed)
                            )
                        }
                    }

                    WButton(text = "Save & Test Connection", onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); vm.saveAndTest() }, loading = s.testing)
                }

                // How to guide
                SectionLabel("How to Setup")
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PearlLight).border(1.dp, PearlBorder, RoundedCornerShape(16.dp)).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        "Go to notion.so/my-integrations",
                        "Create new integration → copy token",
                        "Open your Notion DB → Share → connect integration",
                        "Copy DB ID from the URL (32-char string)"
                    ).forEachIndexed { i, step ->
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(22.dp).clip(CircleShape).background(PearlSurface).border(1.dp, PearlBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${i + 1}", style = MaterialTheme.typography.labelSmall.copy(color = InkBlack, fontWeight = FontWeight.Bold))
                            }
                            Text(step, style = MaterialTheme.typography.bodySmall.copy(color = InkLight))
                        }
                    }
                }

                // Notion DB columns needed
                SectionLabel("Required Notion Columns")
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PearlLight).border(1.dp, PearlBorder, RoundedCornerShape(16.dp)).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Name → Title", "Amount → Number", "Type → Select", "Category → Select", "Currency → Text", "Date → Date").forEach { col ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(col.substringBefore("→").trim(), style = MaterialTheme.typography.titleSmall)
                            Text(col.substringAfter("→").trim(), style = MaterialTheme.typography.bodySmall.copy(color = InkMuted))
                        }
                        if (col != "Date → Date") WDivider()
                    }
                }

                // About
                SectionLabel("About")
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(InkBlack).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("W", style = MaterialTheme.typography.displayMedium.copy(color = PearlLight, fontWeight = FontWeight.Bold))
                    Text("Wealth Manager", style = MaterialTheme.typography.titleLarge.copy(color = PearlLight, fontWeight = FontWeight.Bold))
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall.copy(color = InkMuted))
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(InkMedium))
                    Spacer(Modifier.height(8.dp))
                    Text("Developed by", style = MaterialTheme.typography.bodySmall.copy(color = InkMuted))
                    Text("Soorya × Claude", style = MaterialTheme.typography.titleMedium.copy(color = PearlLight, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp))
                    Text("Simple. Premium. Functional.", style = MaterialTheme.typography.bodySmall.copy(color = InkLight))
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
