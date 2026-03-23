package com.soorya.wealthmanager.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.soorya.wealthmanager.domain.model.*
import com.soorya.wealthmanager.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ── Premium Card ──
@Composable
fun WCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val mod = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Column(
        modifier = mod
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PearlLight)
            .border(1.dp, PearlBorder, RoundedCornerShape(16.dp))
            .padding(20.dp),
        content = content
    )
}

// ── Section Label ──
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, color = InkMuted),
        modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

// ── Divider ──
@Composable
fun WDivider(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(1.dp).background(PearlBorder))
}

// ── Transaction Row ──
@Composable
fun TransactionRow(
    txn: com.soorya.wealthmanager.domain.model.Transaction,
    symbol: String,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val isIncome = txn.type == TransactionType.INCOME
    val fmt = SimpleDateFormat("MMM d", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isIncome) MonoGreenBg else MonoRedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        getCategoryEmoji(txn.category),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(txn.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${txn.category} • ${fmt.format(Date(txn.date))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${if (isIncome) "+" else "-"}$symbol${formatAmt(txn.amount)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isIncome) MonoGreen else MonoRed
                    )
                )
                if (txn.syncedToNotion) {
                    Text("synced", style = MaterialTheme.typography.labelSmall.copy(color = InkMuted))
                }
            }
        }
        if (showDivider) WDivider(Modifier.padding(horizontal = 20.dp))
    }
}

// ── Primary Button ──
@Composable
fun WButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = InkBlack,
            contentColor = PearlLight,
            disabledContainerColor = InkFaint
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PearlLight, strokeWidth = 2.dp)
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, color = PearlLight))
        }
    }
}

// ── Outlined Input ──
@Composable
fun WInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InkBlack,
            unfocusedBorderColor = PearlBorder,
            focusedContainerColor = PearlLight,
            unfocusedContainerColor = PearlLight,
            cursorColor = InkBlack
        ),
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodyLarge
    )
}

// ── Bottom Sheet Handle ──
@Composable
fun SheetHandle() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(width = 36.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(InkFaint))
    }
}

// ── Sheet Header ──
@Composable
fun SheetHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                androidx.compose.material.icons.Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                tint = InkBlack,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

fun formatAmt(amount: Double): String = when {
    amount >= 1_00_000 -> "%.1fL".format(amount / 1_00_000)
    amount >= 1_000 -> "%.1fK".format(amount / 1_000)
    else -> "%.0f".format(amount)
}

fun getCategoryEmoji(cat: String): String = when (cat.lowercase()) {
    "petrol", "fuel" -> "⛽"
    "food", "dining" -> "🍽"
    "shopping" -> "🛍"
    "transport" -> "🚌"
    "health", "medical" -> "💊"
    "rent", "housing" -> "🏠"
    "bills", "utilities" -> "📱"
    "entertainment" -> "🎬"
    "salary" -> "💼"
    "investment" -> "📈"
    "travel" -> "✈️"
    "education" -> "📚"
    "transfer" -> "↔️"
    else -> "💳"
}
