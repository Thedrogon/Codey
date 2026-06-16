import java.io.File

fun logInfo(msg: String) = println("\u001B[34mℹ $msg\u001B[0m")
fun logSuccess(msg: String) = println("\u001B[32m✔ $msg\u001B[0m")
fun logError(msg: String) = System.err.println("\u001B[31m✖ $msg\u001B[0m")

fun String.runCommand(workingDir: File = File(".")): Result<String> = runCatching {
    val process = ProcessBuilder(this.split(" "))
        .directory(workingDir)
        .redirectErrorStream(true)
        .start()

    val output = process.inputStream.bufferedReader().readText().trim()
    val exitCode = process.waitFor()

    if (exitCode != 0) error(output.ifEmpty { "Command failed with code $exitCode" })
    output
}
