package com.example.mobileapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import com.example.mobileapp.frontback.ServicesViewModel.ServiceItem

private val gson: Gson = GsonBuilder().create()

private const val SERVICES_FILE = "services.json"
private const val HISTORY_FILE = "history.json"

data class ServiceDto(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val master: String,
    val cost: Int,
    val date: String? = null,
    val time: String? = null,
    val status: Boolean = true
)

data class HistoryEntryDto(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val master: String,
    val cost: Int,
    val date: String,
    val time: String
)

class JsonStorage(val context: Context) {
    private val servicesType = object : TypeToken<List<ServiceDto>>() {}.type
    private val historyType = object : TypeToken<List<HistoryEntryDto>>() {}.type

    fun readServices(): List<ServiceDto> = readList(SERVICES_FILE, servicesType)
    fun writeServices(list: List<ServiceDto>) = writeList(SERVICES_FILE, list)
    fun readHistory(): List<HistoryEntryDto> = readList(HISTORY_FILE, historyType)
    fun writeHistory(list: List<HistoryEntryDto>) = writeList(HISTORY_FILE, list)

    private fun <T> readList(fileName: String, typeToken: java.lang.reflect.Type): List<T> {
        val file = resolveFile(fileName)
        if (!file.exists()) return emptyList()
        return FileReader(file).use { reader -> gson.fromJson(reader, typeToken) ?: emptyList() }
    }

    private fun <T> writeList(fileName: String, data: List<T>) {
        val file = resolveFile(fileName)
        ensureParent(file)
        FileWriter(file, false).use { writer -> gson.toJson(data, writer) }
    }

    private fun resolveFile(name: String): File = File(context.filesDir, name)
    private fun ensureParent(file: File) {
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
    }

    fun hasServicesData(): Boolean {
        val file = resolveFile(SERVICES_FILE)
        return file.exists() && file.length() > 0L
    }

    // Новая функция: удалить файл services.json, если нужно полностью заменить данные
    fun deleteServicesFile() {
        val file = resolveFile(SERVICES_FILE)
        if (file.exists()) {
            file.delete()
        }
    }
}

class ServiceRepository(private val storage: JsonStorage) {

    init {
        seedServicesIfNeeded()
    }

    fun getServices(): List<ServiceItem> = storage.readServices().map { it.toDomain() }

    fun saveServices(items: List<ServiceItem>) {
        storage.writeServices(items.map { it.toDto() })
    }

    fun getServicesAsJson(): String = gson.toJson(storage.readServices())

    // Новый публичный метод: удалить старые данные и записать дефолтные услуги заново
    fun resetServicesToDefaults() {
        storage.deleteServicesFile()
        storage.writeServices(defaultServices.map { it.toDto() })
    }

    private fun seedServicesIfNeeded() {
        if (!storage.hasServicesData()) {
            storage.writeServices(defaultServices.map { it.toDto() })
        }
    }

    fun addHistory(entry: ServiceItem) {
        val dto = entry.toHistoryDto()
        val current = storage.readHistory().toMutableList()
        current.add(dto)
        storage.writeHistory(current)
    }

    fun removeHistory(entry: ServiceItem) {
        val current = storage.readHistory().toMutableList()
        val iterator = current.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.title == entry.title && item.master == entry.master && item.date == entry.date?.toString()) {
                iterator.remove()
            }
        }
        storage.writeHistory(current)
    }

    fun getHistory(): List<ServiceItem> = storage.readHistory().map { it.toDomain() }

    fun saveHistory(items: List<ServiceItem>) {
        storage.writeHistory(items.map { it.toHistoryDto() })
    }

    fun getHistoryAsJson(): String = gson.toJson(storage.readHistory())

    fun filterByStatus(status: Boolean): List<ServiceItem> = getServices().filter { it.status == status }

    fun searchByName(query: String): List<ServiceItem> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return getServices()
        return getServices().filter { it.title.lowercase().contains(normalized) }
    }

    fun sortAlphabetically(): List<ServiceItem> {
        val sorted = getServices().sortedBy { it.title.lowercase() }
        saveServices(sorted)
        return sorted
    }

    fun exportDataToFile(postfix: String): File {
        val fileName = "services_$postfix.json"
        val file = File(storage.context.filesDir, fileName)
        FileWriter(file, false).use { writer -> gson.toJson(storage.readServices(), writer) }
        return file
    }

    fun createDataBackup(): File {
        val backupName = "services_backup_${System.currentTimeMillis()}.json"
        val file = File(storage.context.filesDir, backupName)
        FileWriter(file, false).use { writer -> gson.toJson(storage.readServices(), writer) }
        return file
    }

    private fun ServiceDto.toDomain() = ServiceItem(
        title = title,
        master = master,
        cost = cost,
        date = date?.let(LocalDate::parse),
        time = time?.let(LocalTime::parse),
        status = status
    )

    private fun ServiceItem.toDto() = ServiceDto(
        title = title,
        master = master,
        cost = cost,
        date = date?.toString(),
        time = time?.toString(),
        status = status
    )

    private fun ServiceItem.toHistoryDto(): HistoryEntryDto {
        val date = requireNotNull(date) { "date is required" }
        val time = requireNotNull(time) { "time is required" }
        return HistoryEntryDto(
            title = title,
            master = master,
            cost = cost,
            date = date.toString(),
            time = time.toString()
        )
    }

    private fun HistoryEntryDto.toDomain() = ServiceItem(
        title = title,
        master = master,
        cost = cost,
        date = LocalDate.parse(date),
        time = LocalTime.parse(time)
    )

    companion object {
        private val defaultServices = listOf(
            ServiceItem("Фейд-стрижка", "Анна", 2400, LocalDate.now(), LocalTime.now(), true),
            ServiceItem("Классическая стрижка", "Иван", 2200, LocalDate.now().plusDays(1), LocalTime.of(10, 0), true),
            ServiceItem("Коррекция бороды", "Сергей", 1600, LocalDate.now().plusDays(2), LocalTime.of(11, 30), true),
            ServiceItem("Горячее полотенце", "Мария", 1300, LocalDate.now().plusDays(3), LocalTime.of(12, 0), true),
            ServiceItem("Детская стрижка", "Антон", 1800, LocalDate.now().plusDays(4), LocalTime.of(13, 0), true),
            ServiceItem("Блеск цвета", "Елена", 3200, LocalDate.now().plusDays(5), LocalTime.of(14, 30), true),
            ServiceItem("Укладка", "Ольга", 1500, LocalDate.now().plusDays(6), LocalTime.of(15, 0), true),
            ServiceItem("SPA-мытьё", "Дмитрий", 900, LocalDate.now().plusDays(7), LocalTime.of(9, 30), true),
            ServiceItem("Королевское бритьё", "Кирилл", 2600, LocalDate.now().plusDays(8), LocalTime.of(16, 0), true),
            ServiceItem("Коррекция бровей", "Наталья", 1100, LocalDate.now().plusDays(9), LocalTime.of(10, 30), true),
            ServiceItem("Массаж головы", "Виктор", 1400, LocalDate.now().plusDays(10), LocalTime.of(11, 0), true),
            ServiceItem("Экспресс-стрижка", "Олег", 2000, LocalDate.now().plusDays(11), LocalTime.of(12, 30), true),
            ServiceItem("Свадебный набор", "Светлана", 4500, LocalDate.now().plusDays(12), LocalTime.of(13, 30), true),
            ServiceItem("Детокс-процедура", "Роман", 2800, LocalDate.now().plusDays(13), LocalTime.of(14, 0), true),
            ServiceItem("Премиум-комбо", "Полина", 5100, LocalDate.now().plusDays(14), LocalTime.of(15, 30), true)
        )
    }
}

// TODO: Replace with actual DI setup
object ServiceRepositoryProvider {
    private var repository: ServiceRepository? = null

    fun get(context: Context): ServiceRepository {
        return repository ?: synchronized(this) {
            repository ?: ServiceRepository(JsonStorage(context)).also { repository = it }
        }
    }
}
