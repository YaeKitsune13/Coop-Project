package com.example.mobileapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapp.MainActivity
import com.example.mobileapp.frontback.ServicesViewModel
import com.example.mobileapp.ui.elements.AppLabeledTextField
import com.example.mobileapp.ui.elements.ServiceCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Preview
@Composable
fun PreviewHomeScreen(){
    val navController = rememberNavController()
    HomeScreen(navController)
}

@Composable
fun HomeScreen(
    navController: NavController
){
    val context = LocalContext.current
    val activity = context as? MainActivity
    val scope = rememberCoroutineScope()
    val vmServices = remember { ServicesViewModel.create(context) }
    val services = remember { mutableStateListOf<ServicesViewModel.ServiceItem>() }
    var finderText by remember { mutableStateOf("") }

    suspend fun updateServices(loader: () -> List<ServicesViewModel.ServiceItem>) {
        val data = withContext(Dispatchers.IO) { loader() }
        services.clear()
        services.addAll(data)
    }

    LaunchedEffect(Unit) {
        updateServices { vmServices.getAllService() }
    }

    fun reloadServices() {
        scope.launch { updateServices { vmServices.getAllService() } }
    }

    val filteredServices by remember {
        derivedStateOf {
            val query = finderText.trim().lowercase()
            if (query.isEmpty()) services.toList()
            else services.filter {
                it.title.lowercase().contains(query) ||
                it.master.lowercase().contains(query)
            }
        }
    }

    fun onSort() {
        scope.launch {
            withContext(Dispatchers.IO) { vmServices.sortDataAlphabetically() }
            updateServices { vmServices.getAllService() }
        }
    }

    fun onFilterActive(active: Boolean) {
        scope.launch {
            updateServices { vmServices.filterServices(active) }
        }
    }

    fun onResetFilter() {
        finderText = ""
        reloadServices()
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
                    text = "Наши услуги",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Поле поиска
            item {
                AppLabeledTextField(
                    label = "Поиск услуг",
                    value = finderText,
                    onValueChange = { finderText = it }
                )
            }

            item {
                Column() {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onFilterActive(true) }) { Text("Активные") }
                        Button(onClick = { onFilterActive(false) }) { Text("Неактивные") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = ::onSort) { Text("Сортировать") }
                        OutlinedButton(onClick = ::onResetFilter) { Text("Сброс") }
                    }
                }
            }

            item {
                Text(
                    text = "Кол-во услуг ${filteredServices.size}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                )
            }

            // Список отфильтрованных услуг
            items(filteredServices) { service ->
                ServiceCard(
                    title = service.title,
                    description = "Мастер ${service.master}",
                    cost = service.cost,
                    onClick = {
                        activity?.displayMessage("Открываем ${service.title}")
                        val title = Uri.encode(service.title)
                        val master = Uri.encode(service.master)
                        val cost = service.cost
                        navController.navigate("item/$title/$master/$cost/${false}")
                    }
                )
            }
        }
    }
}