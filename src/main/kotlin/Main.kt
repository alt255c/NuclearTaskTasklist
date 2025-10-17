package tech.alt255.nd1

data class Task(val id: Int,
                val lines: List<String>)

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

class ConsoleActionInputHandler : ActionInputHandler {
    override fun readAction(): String {
        print("Input an action (add, print, end): ")
        return readLine()?.trim()?.lowercase() ?: ""
    }
}
class ConsoleTaskInputHandler : TaskInputHandler {
    override fun readTaskLines(): List<String> {
        println("Input a new task (enter a blank line to end):")
        val lines = mutableListOf<String>()

        while (true) {
            print("> ")
            val line = readLine()?.trim() ?: ""
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

        tasks.forEachIndexed { index, task ->
            val numberSpacing = if (task.id < 10) "  " else " "
            println("${task.id}$numberSpacing${task.lines.first()}")

            task.lines.drop(1).forEach { line ->
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
    private val taskInputHandler: TaskInputHandler,
    private val outputHandler: OutputHandler
) {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    fun processActions() {
        while (true) {
            when (val action = actionInputHandler.readAction()) {
                "add" -> addTask()
                "print" -> printTasks()
                "end" -> {
                    outputHandler.printMessage("Tasklist exiting!")
                    return
                }
                else -> outputHandler.printMessage("The input action is invalid")
            }
        }
    }

    private fun addTask() {
        val taskLines = taskInputHandler.readTaskLines()

        if (taskLines.isEmpty()) {
            outputHandler.printMessage("The task is blank")
            return
        }

        tasks.add(Task(nextId, taskLines))
        nextId++
    }

    private fun printTasks() {
        outputHandler.printTasks(tasks)
    }
}

class TaskApplication {
    fun run() {
        val actionInputHandler = ConsoleActionInputHandler()
        val taskInputHandler = ConsoleTaskInputHandler()
        val outputHandler = ConsoleOutputHandler()
        val taskManager = TaskManager(actionInputHandler, taskInputHandler, outputHandler)

        taskManager.processActions()
    }
}

fun main() {
    TaskApplication().run()
}