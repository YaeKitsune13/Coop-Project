package com.example.mobileapp.frontback

import android.content.Context
import com.example.mobileapp.data.ServiceRepository
import com.example.mobileapp.data.ServiceRepositoryProvider
import java.time.LocalDate
import java.time.LocalTime

class ServicesViewModel(private val repository: ServiceRepository) {
    data class ServiceItem(
        val title: String,
        val master: String,
        val cost: Int,
        val date: LocalDate? = null,
        val time: LocalTime? = null,
        val status: Boolean = true
    )

    companion object {
        fun create(context: Context): ServicesViewModel {
            val repository = ServiceRepositoryProvider.get(context)
            return ServicesViewModel(repository)
        }
    }

    fun getAllService(): List<ServiceItem> = repository.getServices()

    fun bookService(title: String, master: String, date: LocalDate, time: LocalTime): Boolean {
        val serviceTemplate = repository.getServices().find { it.title == title && it.master == master }
        return if (serviceTemplate != null) {
            val bookedService = serviceTemplate.copy(date = date, time = time)
            putServiceToHistory(bookedService)
        } else {
            false
        }
    }

    fun postServiceToHistory(title: String, master: String, date: LocalDate, time: LocalTime): Boolean {
        val currentHistory = repository.getHistory().toMutableList()
        val index = currentHistory.indexOfFirst { it.title == title && it.master == master }
        return if (index != -1) {
            currentHistory[index] = currentHistory[index].copy(date = date, time = time)
            repository.saveHistory(currentHistory)
            true
        } else {
            false
        }
    }

    fun putServiceToHistory(service: ServiceItem): Boolean {
        repository.addHistory(service)
        return true
    }

    fun removeServiceInHistory(service: ServiceItem) {
        repository.removeHistory(service)
    }

    fun getServiceHistory(): List<ServiceItem> = repository.getHistory()

    fun filterServices(status: Boolean): List<ServiceItem> = repository.filterByStatus(status)

    fun searchServiceByName(query: String): List<ServiceItem> = repository.searchByName(query)

    fun sortDataAlphabetically(): List<ServiceItem> = repository.sortAlphabetically()

    fun exportData(postfix: String) = repository.exportDataToFile(postfix)

    fun createBackup() = repository.createDataBackup()

    fun resetServices() = repository.resetServicesToDefaults()

    fun getServicesJson(): String = repository.getServicesAsJson()

    fun getHistoryJson(): String = repository.getHistoryAsJson()
}