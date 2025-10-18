package tech.alt255.nd1.Task4

import kotlinx.datetime.*

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
    override fun printTasks(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            printMessage("No tasks have been input")
            return
        }

        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date

        tasks.forEachIndexed { index, task ->
            val numberSpacing = if (task.id < 10) "  " else " "
            val formattedDate = task.dueDate.toString()
            val formattedTime = String.format("%02d:%02d", task.dueTime.hour, task.dueTime.minute)

            val daysUntil = currentDate.daysUntil(task.dueDate)
            val dueTag = when {
                daysUntil == 0 -> "T"
                daysUntil > 0 -> "I"
                else -> "O"
            }

            println("${task.id}$numberSpacing$formattedDate $formattedTime ${task.priority} $dueTag")

            task.lines.forEach { line ->
                println("   $line")
            }

            if (index < tasks.lastIndex) {
                println()
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
    private val taskInputHandler: TaskInputHandler
) {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    fun processActions() {
        while (true) {
            when (val action = actionInputHandler.readAction()) {
                "add" -> addTask()
                "print" -> printTasks()
                "edit" -> editTask()
                "delete" -> deleteTask()
                "end" -> {
                    outputHandler.printMessage("Tasklist exiting!")
                    return
                }
                else -> outputHandler.printMessage("The input action is invalid")
            }
        }
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

        val taskManager = TaskManager(
            actionInputHandler,
            taskDataInputHandler,
            outputHandler,
            taskNumberInputHandler,
            editFieldInputHandler,
            priorityInputHandler,
            dateTimeInputHandler,
            taskInputHandler
        )

        taskManager.processActions()
    }
}

fun main() {
    // Помогите, зачем я выбрал эту задачу?
    TaskApplication().run()
}