package com.example.mobileapp.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapp.MainActivity
import com.example.mobileapp.frontback.ServicesViewModel
import androidx.core.content.getSystemService

// --------- КНОПКА-ПЕРЕКЛЮЧАТЕЛЬ ---------

@Composable
fun ThemeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Фон переключателя: зеленый, если включено, серый, если выключено (как на макете)
    // Или используем ваши цвета из темы
    val trackColor = if (checked) Color(0xFF3E5B21) else Color(0xFFE0E0E0)

    Box(
        modifier = modifier
            .width(64.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor)
    ) {
        // «ползунок»
        Box(
            modifier = Modifier
                .padding(2.dp)
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
        // невидимая область для клика
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Transparent)
                .noRippleClickable { onCheckedChange(!checked) }
        )
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        indication = null,
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    ) { onClick() }
}

// --------- CARD "Сменить тему" ---------

@Composable
fun ThemeSwitchCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHighest // светлый фон карточки
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = "Темная тема",
                tint = Color(0xFF3E5B21),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Сменить тему",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            ThemeSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

// --------- ЭКРАН НАСТРОЕК ---------

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onThemeChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val clipboard = context.getSystemService<ClipboardManager>()
    val servicesViewModel = remember { ServicesViewModel.create(context) }

    fun copyJsonToClipboard() {
        val json = servicesViewModel.getServicesJson()
        clipboard?.setPrimaryClip(ClipData.newPlainText("services_json", json))
        Toast.makeText(context, "JSON услуг скопирован", Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.BOTTOM, 0, 120)
        }.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Настройки",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        ThemeSwitchCard(
            checked = isDarkTheme,
            onCheckedChange = { newValue ->
                activity?.displayMessage("Темная тема: ${if (newValue) "включена" else "выключена"}")
                onThemeChange(newValue)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = ::copyJsonToClipboard,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Скопировать JSON услуг",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}