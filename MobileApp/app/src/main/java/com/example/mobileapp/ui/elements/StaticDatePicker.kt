package com.example.mobileapp.ui.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth


@Composable
fun CompactCalendar(
    currentMonth: YearMonth = YearMonth.now(),
    onDaySelected: (LocalDate) -> Unit
) {
    val days = remember(currentMonth) {
        val firstDay = currentMonth.atDay(1)
        val lastDay = currentMonth.atEndOfMonth()

        val startOffset = firstDay.dayOfWeek.value % 7
        val total = startOffset + lastDay.dayOfMonth

        List(total) { index ->
            if (index < startOffset) null
            else firstDay.plusDays((index - startOffset).toLong())
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = currentMonth.month.name.lowercase().replaceFirstChar { it.titlecase() },
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        // Сетка календаря
        LazyVerticalGrid(columns = GridCells.Fixed(7)) {
            items(days) { day ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(enabled = day != null) {
                            day?.let { onDaySelected(it) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(day?.dayOfMonth?.toString() ?: "")
                }
            }
        }
    }
}
