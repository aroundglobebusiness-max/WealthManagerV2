package com.soorya.wealthmanager.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.action.clickable
import com.soorya.wealthmanager.MainActivity

class WealthWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent(context) }
    }

    @Composable
    fun WidgetContent(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
                .appWidgetBackground()
                .cornerRadius(20.dp)
                .clickable(actionStartActivity(intent))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("W", style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold))
            Spacer(GlanceModifier.height(4.dp))
            Text("Wealth", style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 11.sp))
        }
    }
}

class WealthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WealthWidget()
}
