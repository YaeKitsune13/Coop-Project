package com.example.mobileapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobileapp.MainActivity
import com.example.mobileapp.frontback.ServicesViewModel
import com.example.mobileapp.ui.elements.HistoryServiceCard

@Preview
@Composable
fun PreviewHistoryScreen(){
    val navController = rememberNavController()
    HistoryScreen(navController)
}

@Composable
fun HistoryScreen(
    navController: NavController
){
    val context = LocalContext.current
    val activity = context as? MainActivity
    val vmServices = remember { ServicesViewModel.create(context) }
    // Список услуг (пока захардкожен, потом можно подменить данными с бэкенда)
    var services by remember {
        mutableStateOf(vmServices.getServiceHistory())
    }


    Box(modifier = Modifier.fillMaxSize()){
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Заголовок
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "История услуг",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item{
                Text(
                    text = "Кол-во услуг ${services.size}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                )
            }

            // Список отфильтрованных услуг
            items(services, key = { it.hashCode() }) { service ->
                val dateLabel = service.date?.toString()?.takeUnless { it.isBlank() }
                val timeLabel = service.time?.toString()?.takeUnless { it.isBlank() }
                val dateTimeLabel = listOfNotNull(dateLabel, timeLabel).joinToString(" • ")

                HistoryServiceCard(
                    title = service.title,
                    description = buildString {
                        append("Мастер ${service.master}")
                        if (dateTimeLabel.isNotEmpty()) {
                            append("\n")
                            append(dateTimeLabel)
                        }
                    },
                    cost = service.cost,
                    onClick = {
                        val title = Uri.encode(service.title)
                        val master = Uri.encode(service.master)
                        val cost = service.cost
                        navController.navigate("item/$title/$master/$cost/${true}")
                    },
                    onDelete = {
                        // 1. Вызываем метод для бэкенд-логики
                        vmServices.removeServiceInHistory(service)
                        // 2. Обновляем UI, удаляя элемент из локального списка
                        services = vmServices.getServiceHistory()
                        activity?.displayMessage("Услуга удалена из истории")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}