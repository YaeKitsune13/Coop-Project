package com.example.mobileapp.frontback

class ServicesViewModel{
    //! ЗАМЕНИ НА СВОЮ МОДЕЛЬ!
    data class ServiceItem(
        val title: String,      // Название услуги
        val master: String,     // Имя мастера
        val cost: Int           // Цена услуги
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


    fun putServiceToHistory(service : ServiceItem) : Boolean{
        // TODO: Перекинь себе для добавления сервисов из историй
        // backend.addServiceToHistory(service)
        return true
    }


    fun removeServiceInHistory(service : ServiceItem){
        // TODO: Перекинь себе для удаления сервиса из историй
        // backend.removeServiceFromHistory(service.id)
    }

    fun getServiceHistory(): List<ServiceItem> {
        // TODO: Добавь чтение историй
        return listOf(
            ServiceItem("Мужская стрижка", "Анна", 2500),    // Первая услуга
            ServiceItem("Стрижка бороды", "Иван", 1200),     // Вторая услуга
            ServiceItem("Детская стрижка", "Анна", 1800)     // Третья услуга
        )
    }
}