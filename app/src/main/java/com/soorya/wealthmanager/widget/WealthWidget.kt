package com.soorya.wealthmanager.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.*
import com.soorya.wealthmanager.MainActivity

class WealthWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent() }
    }

    @Composable
    fun WidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
                .appWidgetBackground()
                .cornerRadius(20.dp)
                .clickable(actionStartActivity<MainActivity>())
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("W", style = TextStyle(color = ColorProvider(Color(0xFFFAFAF8)), fontSize = 24.sp, fontWeight = FontWeight.Bold))
            Spacer(GlanceModifier.height(4.dp))
            Text("Wealth", style = TextStyle(color = ColorProvider(Color(0xFFAAAAAA)), fontSize = 11.sp))
            Spacer(GlanceModifier.height(2.dp))
            Text("Tap to open", style = TextStyle(color = ColorProvider(Color(0xFF666666)), fontSize = 9.sp))
        }
    }
}

class WealthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WealthWidget()
}
