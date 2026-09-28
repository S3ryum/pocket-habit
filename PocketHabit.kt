import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.util.Base64

data class Habit(val id: Long, val name: String, val completedOn: LocalDate?)
private val dataFile = Path.of("habits.tsv")

private fun encode(value: String): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))
private fun decode(value: String): String =
    String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)

private fun loadHabits(): MutableList<Habit> {
    if (!Files.exists(dataFile)) return mutableListOf()
    return Files.readAllLines(dataFile).filter { it.isNotBlank() }.map { line ->
        val fields = line.split('\t')
        require(fields.size == 3) { "Invalid data in habits.tsv." }
        Habit(fields[0].toLong(), decode(fields[1]),
            fields[2].takeIf { it.isNotBlank() }?.let(LocalDate::parse))
    }.toMutableList()
}

private fun saveHabits(habits: List<Habit>) {
    val temporary = dataFile.resolveSibling("habits.tsv.tmp")
    val lines = habits.map { habit ->
        habit.id.toString() + "\t" + encode(habit.name) + "\t" + (habit.completedOn?.toString() ?: "")
    }
    Files.write(temporary, lines, Charsets.UTF_8)
    try {
        Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    } catch (_: Exception) {
        Files.move(temporary, dataFile, StandardCopyOption.REPLACE_EXISTING)
    }
}

private fun usage() {
    println("Pocket Habit - offline habit tracker")
    println("  add <habit name>")
    println("  list")
    println("  done <id>")
    println("  remove <id>")
}

fun main(args: Array<String>) {
    if (args.isEmpty() || args[0] == "help") { usage(); return }
    try {
        val habits = loadHabits()
        when (args[0]) {
            "add" -> {
                val name = args.drop(1).joinToString(" ").trim()
                require(name.isNotBlank()) { "Please provide a habit name." }
                val id = (habits.maxOfOrNull { it.id } ?: 0L) + 1L
                habits.add(Habit(id, name, null))
                saveHabits(habits)
                println("Added habit " + id + ": " + name)
            }
            "list" -> {
                if (habits.isEmpty()) println("No habits yet. Add one with: add <habit name>")
                habits.forEach { habit ->
                    val status = if (habit.completedOn == LocalDate.now()) "done today" else "not done today"
                    println(habit.id.toString() + ". " + habit.name + " | " + status)
                }
                println("Data file: " + dataFile.toAbsolutePath())
            }
            "done" -> {
                require(args.size == 2) { "Usage: done <id>" }
                val id = args[1].toLongOrNull() ?: error("Habit id must be a number.")
                val index = habits.indexOfFirst { it.id == id }
                require(index >= 0) { "No habit found with id " + id + "." }
                habits[index] = habits[index].copy(completedOn = LocalDate.now())
                saveHabits(habits)
                println("Marked as done today: " + habits[index].name)
            }
            "remove" -> {
                require(args.size == 2) { "Usage: remove <id>" }
                val id = args[1].toLongOrNull() ?: error("Habit id must be a number.")
                val removed = habits.removeIf { it.id == id }
                require(removed) { "No habit found with id " + id + "." }
                saveHabits(habits)
                println("Removed habit " + id + ".")
            }
            else -> { usage(); error("Unknown command: " + args[0]) }
        }
    } catch (error: Exception) {
        System.err.println("Error: " + (error.message ?: "unexpected failure"))
        kotlin.system.exitProcess(1)
    }
}
