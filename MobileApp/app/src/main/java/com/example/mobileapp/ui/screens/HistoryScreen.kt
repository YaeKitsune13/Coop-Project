package com.example.mobileapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
    val vmServives = ServicesViewModel();
    // Список услуг (пока захардкожен, потом можно подменить данными с бэкенда)
    var services by remember {
        mutableStateOf(vmServives.getServiceHistory())
    }

    Box(modifier = Modifier.fillMaxSize()){
        LazyColumn (modifier = Modifier.padding(25.dp)) {

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
                HistoryServiceCard(
                    title = service.title,
                    description = "Мастер ${service.master}",
                    cost = service.cost,
                    onClick = {
                        val title = Uri.encode(service.title)
                        val master = Uri.encode(service.master)
                        val cost = service.cost
                        navController.navigate("item/$title/$master/$cost/${true}")
                    },
                    onDelete = {
                        // 1. Вызываем метод для бэкенд-логики
                        vmServives.removeServiceInHistory(service)
                        // 2. Обновляем UI, удаляя элемент из локального списка
                        services = vmServives.getServiceHistory()
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}