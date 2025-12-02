package com.example.mobileapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapp.MainActivity
import com.example.mobileapp.frontback.ServicesViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Preview
@Composable
fun PreviewService() {
    OpenedServiceCardScreen(
        title = "Мужская стрижка",
        master = "Анна",
        onBooked = {}
    )
}

@Composable
fun OpenedServiceCardScreen(
    title: String,
    master: String,
    timeSlots: List<LocalTime> = listOf(
        LocalTime.of(9, 0),
        LocalTime.of(11, 30),
        LocalTime.of(12, 0),
        LocalTime.of(17, 30)
    ),
    onBooked: () -> Unit = {},
    onCancel: () -> Unit = {},
    redactMode: Boolean = false
) {
    val context = LocalContext.current
    val servicesViewModel = remember { ServicesViewModel.create(context) }
    val activity = context as? MainActivity
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
    ) {
        item { InfoCard(title = title, master = master) }

        item {
            // Используем стандартный DatePicker
            NativeDatePickerCard(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                timeSlots = timeSlots,
                selectedTime = selectedTime,
                onTimeSelected = { selectedTime = it }
            )
        }

        item {
            SummaryCard(
                master = master,
                dateText = selectedDate.format(dateFormatter),
                timeText = selectedTime?.format(timeFormatter)
            )
        }

        item {
            ActionButtons(
                enabled = selectedTime != null,
                onBook = {
                    selectedTime?.let { time ->
                        if (redactMode){
                            servicesViewModel.postServiceToHistory(title, master, selectedDate, time)
                            onBooked()
                            onCancel()
                        }
                        else{
                            servicesViewModel.bookService(title, master, selectedDate, time)
                            onBooked()
                            onCancel()
                        }
                        activity?.displayMessage(
                            "Бронь на ${selectedDate.format(dateFormatter)} в ${time.format(timeFormatter)}"
                        )
                    }
                },
                onCancel = {
                    activity?.displayMessage("Выбор времени отменён")
                    onCancel()
                }
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, master: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Услуга: $title",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Мастер: $master",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NativeDatePickerCard(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    timeSlots: List<LocalTime>,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
) {
    // Состояние стандартного календаря
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    // Следим за изменением даты в календаре
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            onDateSelected(date)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Выберите дату и время",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // ВСТРОЕННЫЙ КАЛЕНДАРЬ GOOGLE
            // title = null убирает верхний заголовок "Select date", чтобы было компактнее
            DatePicker(
                state = datePickerState,
                title = null,
                headline = null,
                showModeToggle = false, // Убираем кнопку переключения на ввод текста
                colors = DatePickerDefaults.colors(
                    containerColor = Color.Transparent // Прозрачный фон, чтобы сливался с карточкой
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Доступное время:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TimeSlotsRow(
                    timeSlots = timeSlots,
                    selected = selectedTime,
                    onSelect = onTimeSelected
                )
            }
        }
    }
}

@Composable
private fun TimeSlotsRow(
    timeSlots: List<LocalTime>,
    selected: LocalTime?,
    onSelect: (LocalTime) -> Unit
) {
    // Прокручиваемый ряд для времени, если слотов много
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        timeSlots.forEach { time ->
            val isSelected = time == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 4.dp)
                    .wrapContentSize(Alignment.Center)
                    .clickableNoRipple { onSelect(time) }
            ) {
                Text(
                    text = "%02d:%02d".format(time.hour, time.minute),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    master: String,
    dateText: String,
    timeText: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Итог",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (timeText == null) {
                Text(
                    text = "Выберите время",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "$master в $dateText в $timeText",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    enabled: Boolean,
    onBook: () -> Unit,
    onCancel: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onBook,
            enabled = enabled,
            modifier = Modifier.weight(1f).height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Записаться", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Отменить", fontSize = 15.sp)
        }
    }
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) { onClick() }
