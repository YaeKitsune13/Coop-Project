package com.example.mobileapp.frontback

import java.time.LocalDate
import java.time.LocalTime

class ServicesViewModel{
    //! ЗАМЕНИ НА СВОЮ МОДЕЛЬ!
    data class ServiceItem(
        val title: String,      // Название услуги
        val master: String,     // Имя мастера
        val cost: Int,          // Цена услуги
        val date: LocalDate? = null,
        val time: LocalTime? = null
    ) // ! ЗАМЕНИ НА СВОЮ МОДЕЛЬ

    fun getAllService() : List<ServiceItem>{
        // Инициализируем список услуг (текущие данные - примеры для тестирования)
        val services = // Вставь сюда выдачу всех нынешних услуг (нижнее можешь удалить)
            listOf(
                ServiceItem("Мужская стрижка", "Анна", 2500),    // Первая услуга
                ServiceItem("Стрижка бороды", "Иван", 1200),     // Вторая услуга
                ServiceItem("Детская стрижка", "Анна", 1800)     // Третья услуга
            )
        return services  // Возвращаем полученный список
    }

    fun bookService(title: String, master: String, date: LocalDate, time: LocalTime): Boolean {
        val serviceTemplate = getAllService().find { it.title == title && it.master == master }
        return if (serviceTemplate != null) {
            val bookedService = serviceTemplate.copy(date = date, time = time)
            putServiceToHistory(bookedService)
        } else {
            false
        }
    }

    fun postServiceToHistory(title: String, master: String, date: LocalDate, time: LocalTime): Boolean {
        val serviceTemplate = getAllService().find { it.title == title && it.master == master }
        return if (serviceTemplate != null) {
            val updatedService = serviceTemplate.copy(date = date, time = time)
            // TODO: Перекинь себе для обнавления историй изменяется только дата и время
            println("Service updated: $updatedService")
            true
        } else {
            false
        }
    }

    fun putServiceToHistory(service : ServiceItem) : Boolean{
        val serviceTemplate = getAllService().find { it.title == service.title && it.master == service.master }
        return if (serviceTemplate != null) {
            // TODO: Перекинь себе для добавления историй
            println("Service added to history: $service")
            true
        } else {
            false
        }
    }


    fun removeServiceInHistory(service : ServiceItem){
        // TODO: Перекинь себе для удаления сервиса из историй
        // backend.removeServiceFromHistory(service.id)
    }

    fun getServiceHistory(): List<ServiceItem> {
        // TODO: Добавь чтение историй
        return listOf(
            ServiceItem("Мужская стрижка", "Анна", 2500, LocalDate.now().minusDays(10), LocalTime.of(12, 30)),
            ServiceItem("Стрижка бороды", "Иван", 1200, LocalDate.now().minusDays(4), LocalTime.of(15, 0)),
            ServiceItem("Детская стрижка", "Анна", 1800, LocalDate.now().minusDays(1), LocalTime.of(11, 0))
        )
    }
}