package tech.alt255.nd1

data class Task(val id: Int,
                val content: String)


interface InputHandler {
    fun readTasks(): List<String>
}
interface OutputHandler {
    fun printTasks(tasks: List<Task>)
    fun printMessage(message: String)
}

class ConsoleInputHandler : InputHandler {
    override fun readTasks(): List<String> {
        println("Input the tasks (enter a blank line to end):")
        val tasks = mutableListOf<String>()

        while (true) {
            print("> ")
            val input = readLine()?.trim() ?: ""
            if (input.isEmpty()) {
                break
            }
            tasks.add(input)
        }

        return tasks
    }
}

class ConsoleOutputHandler : OutputHandler {
    override fun printTasks(tasks: List<Task>) {
        tasks.forEach { task ->
            val spacing = if (task.id < 10) "  " else " "
            println("${task.id}.$spacing${task.content}")
        }
    }

    override fun printMessage(message: String) {
        println(message)
    }
}


class TaskManager(
    private val inputHandler: InputHandler,
    private val outputHandler: OutputHandler
) {
    fun processTasks() {
        val taskInputs = inputHandler.readTasks()

        if (taskInputs.isEmpty()) {
            outputHandler.printMessage("No tasks have been input")
            return
        }

        val tasks = taskInputs.mapIndexed { index, content ->
            Task(id = index + 1, content = content)
        }

        outputHandler.printTasks(tasks)
    }
}

class TaskApplication {
    fun run() {
        val inputHandler = ConsoleInputHandler()
        val outputHandler = ConsoleOutputHandler()
        val taskManager = TaskManager(inputHandler, outputHandler)

        taskManager.processTasks()
    }
}

fun main() {
    TaskApplication().run()
}