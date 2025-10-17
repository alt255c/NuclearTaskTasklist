package tech.alt255.nd1.Task6

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.io.File

data class TaskJson(
    val id: Int,
    val lines: List<String>,
    val dueDate: String,
    val dueTime: String,
    val priority: String
)

data class Task(
    val id: Int,
    val lines: List<String>,
    val dueDate: LocalDate,
    val dueTime: LocalTime,
    val priority: String
)

data class TaskData(
    val priority: String,
    val date: LocalDate,
    val time: LocalTime,
    val lines: List<String>
)

interface TaskPersistence {
    fun saveTasks(tasks: List<Task>)
    fun loadTasks(): List<Task>
}

interface ActionInputHandler {
    fun readAction(): String
}

interface TaskInputHandler {
    fun readTaskLines(): List<String>
}

interface OutputHandler {
    fun printTasks(tasks: List<Task>)
    fun printMessage(message: String)
}

interface PriorityInputHandler {
    fun readPriority(): String
}

interface DateTimeInputHandler {
    fun readDate(): LocalDate
    fun readTime(): LocalTime
}

interface TaskNumberInputHandler {
    fun readTaskNumber(maxNumber: Int): Int
}

interface EditFieldInputHandler {
    fun readEditField(): String
}

class JsonFilePersistence : TaskPersistence {
    private val jsonFile = File("tasklist.json")
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val taskListAdapter = moshi.adapter<List<TaskJson>>(Types.newParameterizedType(List::class.java, TaskJson::class.java))

    override fun saveTasks(tasks: List<Task>) {
        val taskJsonList = tasks.map { task ->
            TaskJson(
                id = task.id,
                lines = task.lines,
                dueDate = task.dueDate.toString(),
                dueTime = String.format("%02d:%02d", task.dueTime.hour, task.dueTime.minute),
                priority = task.priority
            )
        }
        jsonFile.writeText(taskListAdapter.toJson(taskJsonList))
    }

    override fun loadTasks(): List<Task> {
        if (!jsonFile.exists()) return emptyList()

        return try {
            val jsonString = jsonFile.readText()
            val taskJsonList = taskListAdapter.fromJson(jsonString) ?: return emptyList()

            taskJsonList.map { taskJson ->
                Task(
                    id = taskJson.id,
                    lines = taskJson.lines,
                    dueDate = LocalDate.parse(taskJson.dueDate),
                    dueTime = LocalTime.parse(taskJson.dueTime),
                    priority = taskJson.priority
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class ConsolePriorityInputHandler : PriorityInputHandler {
    override fun readPriority(): String {
        while (true) {
            print("Input the task priority (C, H, N, L): ")
            val input = readlnOrNull()?.trim()?.uppercase() ?: ""
            when (input) {
                "C", "H", "N", "L" -> return input
                else -> continue
            }
        }
    }
}

class ConsoleDateTimeInputHandler : DateTimeInputHandler {
    override fun readDate(): LocalDate {
        while (true) {
            print("Input the date (yyyy-mm-dd): ")
            val input = readlnOrNull()?.trim() ?: ""
            try {
                val parts = input.split("-").map { it.toInt() }
                return LocalDate(parts[0], parts[1], parts[2])
            } catch (e: Exception) {
                println("The input date is invalid")
            }
        }
    }

    override fun readTime(): LocalTime {
        while (true) {
            print("Input the time (hh:mm): ")
            val input = readlnOrNull()?.trim() ?: ""
            try {
                val parts = input.split(":").map { it.toInt() }
                return LocalTime(parts[0], parts[1])
            } catch (e: Exception) {
                println("The input time is invalid")
            }
        }
    }
}

class ConsoleTaskNumberInputHandler : TaskNumberInputHandler {
    override fun readTaskNumber(maxNumber: Int): Int {
        while (true) {
            print("Input the task number (1-$maxNumber): ")
            val input = readlnOrNull()?.trim() ?: ""
            val number = input.toInt()
            if (number in 1..maxNumber) {
                return number
            }
            println("Invalid task number")
        }
    }
}

class ConsoleEditFieldInputHandler : EditFieldInputHandler {
    override fun readEditField(): String {
        while (true) {
            print("Input a field to edit (priority, date, time, task): ")
            val input = readlnOrNull()?.trim()?.lowercase() ?: ""
            when (input) {
                "priority", "date", "time", "task" -> return input
                else -> println("Invalid field")
            }
        }
    }
}

class ConsoleTaskDataInputHandler(
    private val priorityInputHandler: PriorityInputHandler,
    private val dateTimeInputHandler: DateTimeInputHandler
) : TaskInputHandler {
    override fun readTaskLines(): List<String> {
        println("Input a new task (enter a blank line to end):")
        val lines = mutableListOf<String>()

        while (true) {
            print("> ")
            val line = readlnOrNull()?.trim() ?: ""
            if (line.isEmpty()) {
                break
            }
            lines.add(line)
        }

        return lines
    }

    fun readTaskData(): TaskData? {
        val priority = priorityInputHandler.readPriority()
        val date = dateTimeInputHandler.readDate()
        val time = dateTimeInputHandler.readTime()
        val lines = readTaskLines()

        return if (lines.isEmpty()) {
            null
        } else {
            TaskData(priority, date, time, lines)
        }
    }
}

class ConsoleActionInputHandler : ActionInputHandler {
    override fun readAction(): String {
        print("Input an action (add, print, edit, delete, end): ")
        return readlnOrNull()?.trim()?.lowercase() ?: ""
    }
}

class ConsoleTaskInputHandler : TaskInputHandler {
    override fun readTaskLines(): List<String> {
        println("Input a new task (enter a blank line to end):")
        val lines = mutableListOf<String>()

        while (true) {
            print("> ")
            val line = readlnOrNull()?.trim() ?: ""
            if (line.isEmpty()) {
                break
            }
            lines.add(line)
        }

        return lines
    }
}

class ConsoleOutputHandler : OutputHandler {
    private val nWidth = 4
    private val dateWidth = 12
    private val timeWidth = 7
    private val pWidth = 3
    private val dWidth = 3
    private val taskWidth = 44

    private fun getPriorityColor(priority: String): String {
        return when (priority) {
            "C" -> "\u001B[101m \u001B[0m"
            "H" -> "\u001B[103m \u001B[0m"
            "N" -> "\u001B[102m \u001B[0m"
            "L" -> "\u001B[104m \u001B[0m"
            else -> " "
        }
    }

    private fun getDueTagColor(dueTag: String): String {
        return when (dueTag) {
            "I" -> "\u001B[102m \u001B[0m"
            "T" -> "\u001B[103m \u001B[0m"
            "O" -> "\u001B[101m \u001B[0m"
            else -> " "
        }
    }

    private fun formatNumber(id: Int): String {
        return if (id < 10) {
            " $id  "
        } else {
            " $id "
        }
    }

    private fun formatDate(date: LocalDate): String {
        return " ${date.toString()} "
    }

    private fun formatTime(time: LocalTime): String {
        val formatted = String.format("%02d:%02d", time.hour, time.minute)
        return " $formatted "
    }

    private fun splitToLines(text: String, width: Int): List<String> {
        val lines = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = minOf(start + width, text.length)
            var line = text.substring(start, end)
            if (line.length < width) {
                line = line.padEnd(width, ' ')
            }
            lines.add(line)
            start = end
        }
        return lines
    }

    private fun splitTaskLines(taskLines: List<String>, width: Int): List<String> {
        val result = mutableListOf<String>()
        for (line in taskLines) {
            result.addAll(splitToLines(line, width))
        }
        return result
    }

    private fun printLine() {
        println("+" +
                "-".repeat(nWidth) + "+" +
                "-".repeat(dateWidth) + "+" +
                "-".repeat(timeWidth) + "+" +
                "-".repeat(pWidth) + "+" +
                "-".repeat(dWidth) + "+" +
                "-".repeat(taskWidth) + "+")
    }

    private fun printHeader() {
        println("|" +
                " N  ".padEnd(nWidth) + "|" +
                "    Date    ".padEnd(dateWidth) + "|" +
                " Time ".padEnd(timeWidth) + "|" +
                " P ".padEnd(pWidth) + "|" +
                " D ".padEnd(dWidth) + "|" +
                "                    Task                    ".padEnd(taskWidth) + "|")
    }

    private fun printTaskFirstLine(task: Task, dueTag: String, firstTaskLine: String) {
        val nString = formatNumber(task.id)
        val dateString = formatDate(task.dueDate)
        val timeString = formatTime(task.dueTime)
        val priorityColor = getPriorityColor(task.priority)
        val dueTagColor = getDueTagColor(dueTag)

        println("|" +
                nString.padEnd(nWidth) + "|" +
                dateString.padEnd(dateWidth) + "|" +
                timeString.padEnd(timeWidth) + "|" +
                " $priorityColor ".padEnd(pWidth) + "|" +
                " $dueTagColor ".padEnd(dWidth) + "|" +
                firstTaskLine.padEnd(taskWidth) + "|")
    }

    private fun printTaskLineWithoutData(taskLine: String) {
        println("|" +
                " ".repeat(nWidth) + "|" +
                " ".repeat(dateWidth) + "|" +
                " ".repeat(timeWidth) + "|" +
                " ".repeat(pWidth) + "|" +
                " ".repeat(dWidth) + "|" +
                taskLine.padEnd(taskWidth) + "|")
    }

    private fun calculateDueTag(currentDate: LocalDate, dueDate: LocalDate): String {
        val daysUntil = currentDate.daysUntil(dueDate)
        return when {
            daysUntil == 0 -> "T"
            daysUntil > 0 -> "I"
            else -> "O"
        }
    }

    override fun printTasks(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            printMessage("No tasks have been input")
            return
        }

        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date

        printLine()
        printHeader()
        printLine()

        for (task in tasks) {
            val dueTag = calculateDueTag(currentDate, task.dueDate)
            val taskLines = splitTaskLines(task.lines, taskWidth)

            if (taskLines.isNotEmpty()) {
                printTaskFirstLine(task, dueTag, taskLines.first())

                for (i in 1 until taskLines.size) {
                    printTaskLineWithoutData(taskLines[i])
                }

                printLine()
            }
        }
    }

    override fun printMessage(message: String) {
        println(message)
    }
}

class TaskManager(
    private val actionInputHandler: ActionInputHandler,
    private val taskDataInputHandler: ConsoleTaskDataInputHandler,
    private val outputHandler: OutputHandler,
    private val taskNumberInputHandler: TaskNumberInputHandler,
    private val editFieldInputHandler: EditFieldInputHandler,
    private val priorityInputHandler: PriorityInputHandler,
    private val dateTimeInputHandler: DateTimeInputHandler,
    private val taskInputHandler: TaskInputHandler,
    private val persistence: TaskPersistence
) {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    init {
        loadTasks()
        setupShutdownHook()
    }

    private fun setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread {
            saveTasks()
        })
    }

    fun processActions() {
        while (true) {
            when (val action = actionInputHandler.readAction()) {
                "add" -> addTask()
                "print" -> printTasks()
                "edit" -> editTask()
                "delete" -> deleteTask()
                "end" -> {
                    saveTasks()
                    outputHandler.printMessage("Tasklist exiting!")
                    return
                }
                else -> outputHandler.printMessage("The input action is invalid")
            }
        }
    }

    private fun loadTasks() {
        val loadedTasks = persistence.loadTasks()
        tasks.clear()
        tasks.addAll(loadedTasks)
        nextId = if (tasks.isNotEmpty()) tasks.maxOf { it.id } + 1 else 1
    }

    private fun saveTasks() {
        persistence.saveTasks(tasks)
    }

    private fun addTask() {
        val taskData = taskDataInputHandler.readTaskData()

        if (taskData == null) {
            outputHandler.printMessage("The task is blank")
            return
        }

        tasks.add(Task(nextId, taskData.lines, taskData.date, taskData.time, taskData.priority))
        nextId++
    }

    private fun printTasks() {
        outputHandler.printTasks(tasks)
    }

    private fun deleteTask() {
        if (tasks.isEmpty()) {
            outputHandler.printMessage("No tasks have been input")
            return
        }

        outputHandler.printTasks(tasks)
        val taskNumber = taskNumberInputHandler.readTaskNumber(tasks.size)
        tasks.removeAt(taskNumber - 1)

        tasks.forEachIndexed { index, task ->
            tasks[index] = task.copy(id = index + 1)
        }
        nextId = tasks.size + 1
        outputHandler.printMessage("The task is deleted")
    }

    private fun editTask() {
        if (tasks.isEmpty()) {
            outputHandler.printMessage("No tasks have been input")
            return
        }

        outputHandler.printTasks(tasks)
        val taskNumber = taskNumberInputHandler.readTaskNumber(tasks.size)
        val field = editFieldInputHandler.readEditField()

        val oldTask = tasks[taskNumber - 1]
        val updatedTask = when (field) {
            "priority" -> {
                val newPriority = priorityInputHandler.readPriority()
                oldTask.copy(priority = newPriority)
            }
            "date" -> {
                val newDate = dateTimeInputHandler.readDate()
                oldTask.copy(dueDate = newDate)
            }
            "time" -> {
                val newTime = dateTimeInputHandler.readTime()
                oldTask.copy(dueTime = newTime)
            }
            "task" -> {
                val newLines = taskInputHandler.readTaskLines()
                if (newLines.isEmpty()) {
                    outputHandler.printMessage("The task is blank")
                    return
                }
                oldTask.copy(lines = newLines)
            }
            else -> oldTask
        }

        tasks[taskNumber - 1] = updatedTask
        outputHandler.printMessage("The task is changed")
    }
}

class TaskApplication {
    fun run() {
        val actionInputHandler = ConsoleActionInputHandler()
        val priorityInputHandler = ConsolePriorityInputHandler()
        val dateTimeInputHandler = ConsoleDateTimeInputHandler()
        val taskDataInputHandler = ConsoleTaskDataInputHandler(priorityInputHandler, dateTimeInputHandler)
        val outputHandler = ConsoleOutputHandler()
        val taskNumberInputHandler = ConsoleTaskNumberInputHandler()
        val editFieldInputHandler = ConsoleEditFieldInputHandler()
        val taskInputHandler = ConsoleTaskInputHandler()
        val persistence = JsonFilePersistence()

        val taskManager = TaskManager(
            actionInputHandler,
            taskDataInputHandler,
            outputHandler,
            taskNumberInputHandler,
            editFieldInputHandler,
            priorityInputHandler,
            dateTimeInputHandler,
            taskInputHandler,
            persistence
        )

        taskManager.processActions()
    }
}

fun main() {
    // Final stage, eee
    TaskApplication().run()
}