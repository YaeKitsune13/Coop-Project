# Coop-Project: Android Mobile App

## Анализ реализованных функций

Ниже представлен анализ требуемых функций и их аналогов в проекте.

### Обязательные функции (1-10)

| № | Требуемая функция | Аналог в проекте | Местоположение | Описание реализации |
|---|------------------|------------------|----------------|---------------------|
| 1 | `initializeAllViews()` | **Нет прямого аналога** | - | Проект использует Jetpack Compose, где нет необходимости в ручной инициализации View-элементов. Compose автоматически управляет UI-элементами через `@Composable` функции. |
| 2 | `setupAllClickListeners()` | **Нет прямого аналога** | - | В Jetpack Compose слушатели устанавливаются декларативно через `onClick` параметры в composable-функциях (например, `Button(onClick = {})`, `clickable { }`). |
| 3 | `addNewItem()` | **Частично: `addHistory()`** | `dataworker.kt:108-113` | Метод `addHistory()` в `ServiceRepository` добавляет новую запись в историю услуг. Для услуг используется предзаполнение данных через `seedServicesIfNeeded()` (только при первом запуске). |
| 4 | `editSelectedItem()` | **Да: `postServiceToHistory()`** | `Services.kt:38-48` | Метод обновляет выбранный элемент в истории забронированных услуг по названию и мастеру. Изменяет дату и время записи (не основной каталог услуг). |
| 5 | `removeSelectedItem()` | **Да: `removeHistory()` / `removeServiceInHistory()`** | `dataworker.kt:115-125`, `Services.kt:55-57` | Метод удаляет запись из истории услуг, сохраняет файл без удаленного элемента. |
| 6 | `displayAllData()` | **Да: `getAllService()` / `getServiceHistory()`** | `Services.kt:26`, `Services.kt:59` | Загружает все данные из JSON-файла. UI отображение через `HomeScreen.kt` и `HistoryScreen.kt` с использованием `LazyColumn`. |
| 7 | `loadJsonFromFile()` | **Да: `readList()` / `readServices()` / `readHistory()`** | `dataworker.kt:43-51` | Метод `readList()` читает JSON-файл, проверяет существование, возвращает список или пустой массив. |
| 8 | `saveJsonToFile()` | **Да: `writeList()` / `writeServices()` / `writeHistory()`** | `dataworker.kt:53-58` | Метод `writeList()` преобразует данные в JSON и записывает в файл через `FileWriter`. |
| 9 | `resetFields()` | **Частично** | `HomeScreen.kt:99-102` | Функция `onResetFilter()` сбрасывает поле поиска и перезагружает список. Полная очистка форм не требуется в текущей архитектуре. |
| 10 | `displayMessage()` | **Да: `displayMessage()`** | `MainActivity.kt:85-87` | Метод показывает Toast-сообщение с переданным текстом. Полностью соответствует требованиям. |

### Дополнительные функции (11-14)

| № | Требуемая функция | Аналог в проекте | Местоположение | Описание реализации |
|---|------------------|------------------|----------------|---------------------|
| 11 | `filterDataByStatus()` | **Да: `filterByStatus()` / `filterServices()`** | `dataworker.kt:135`, `Services.kt:61` | Фильтрует услуги по статусу (true/false). Отдельно от отображения. |
| 12 | `searchDataByName()` | **Да: `searchByName()` / `searchServiceByName()`** | `dataworker.kt:137-141`, `Services.kt:63` | Принимает строку поиска, возвращает отфильтрованный список по названию. |
| 13 | `sortDataAlphabetically()` | **Да: `sortAlphabetically()` / `sortDataAlphabetically()`** | `dataworker.kt:143-147`, `Services.kt:65` | Сортирует данные по названию, сохраняет и возвращает отсортированный список. |
| 14 | `exportDataToFile()` | **Да: `exportDataToFile()` / `exportData()`** | `dataworker.kt:149-154`, `Services.kt:67` | Экспортирует данные в отдельный файл с заданным постфиксом. |
| 14+ | `createDataBackup()` | **Да: `createDataBackup()` / `createBackup()`** | `dataworker.kt:156-160`, `Services.kt:69` | Создает резервную копию данных с временной меткой. |

---

## Детальное описание реализованных аналогов

### 1. Работа с JSON (loadJsonFromFile / saveJsonToFile)

```kotlin
// dataworker.kt
class JsonStorage(val context: Context) {
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
}
```

### 2. Отображение сообщений (displayMessage)

```kotlin
// MainActivity.kt
fun displayMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
```

### 3. Фильтрация по статусу (filterDataByStatus)

```kotlin
// dataworker.kt
fun filterByStatus(status: Boolean): List<ServiceItem> = 
    getServices().filter { it.status == status }

// Services.kt
fun filterServices(status: Boolean): List<ServiceItem> = 
    repository.filterByStatus(status)
```

### 4. Поиск по названию (searchDataByName)

```kotlin
// dataworker.kt
fun searchByName(query: String): List<ServiceItem> {
    val normalized = query.trim().lowercase()
    if (normalized.isEmpty()) return getServices()
    return getServices().filter { it.title.lowercase().contains(normalized) }
}
```

### 5. Сортировка по алфавиту (sortDataAlphabetically)

```kotlin
// dataworker.kt
fun sortAlphabetically(): List<ServiceItem> {
    val sorted = getServices().sortedBy { it.title.lowercase() }
    saveServices(sorted)
    return sorted
}
```

### 6. Экспорт и резервное копирование

```kotlin
// dataworker.kt
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
```

---

## Архитектурные особенности

Проект использует **Jetpack Compose** вместо традиционного XML-based UI, поэтому:

1. **initializeAllViews()** - не требуется, так как Compose автоматически управляет UI
2. **setupAllClickListeners()** - реализуется декларативно через `onClick` параметры

Архитектура проекта:
- `ui/screens/` - Composable экраны (HomeScreen, HistoryScreen, SettingsScreen)
- `frontback/Services.kt` - ViewModel для связи UI и данных
- `data/dataworker.kt` - Репозиторий и работа с JSON-файлами

## Резюме

| Категория | Реализовано | Частично | Не применимо |
|-----------|-------------|----------|--------------|
| Обязательные (1-10) | 6 | 2 | 2 |
| Дополнительные (11-14+) | 5 | 0 | 0 |
| **Итого** | **11** | **2** | **2** |

**Полностью реализованы:** displayMessage, loadJsonFromFile, saveJsonToFile, displayAllData, removeSelectedItem, filterDataByStatus, searchDataByName, sortDataAlphabetically, exportDataToFile, createDataBackup, editSelectedItem

**Частично реализованы:** addNewItem (только для истории), resetFields (только для поиска)

**Не применимы в Compose:** initializeAllViews, setupAllClickListeners
