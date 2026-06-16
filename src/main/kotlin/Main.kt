import com.sun.tools.javac.comp.Todo
import kotlinx.coroutines.*
import java.io.File

const val BLUE = "\u001b[1;33m"
const val RESET = "\u001b[0m"

suspend fun gatherState(): RepoContext = coroutineScope {
    val isRepo = async(Dispatchers.IO) { "git rev-parse --is-inside-work-tree".runCommand().isSuccess }
    val uncommitted = async(Dispatchers.IO) { "git status --porcelain".runCommand().getOrDefault("").isNotEmpty() }
    val branch = async(Dispatchers.IO) { "git branch --show-current".runCommand().getOrDefault("main") }
    val remote = async(Dispatchers.IO) { "git remote get-url origin".runCommand().isSuccess }

    RepoContext(isRepo.await(), uncommitted.await(), branch.await(), remote.await())
}

fun main(args: Array<String>) = runBlocking {
    val command = args.firstOrNull() ?: "push"

    when (command) {
        "upgrade" -> upgradeCodey()
        "push" -> executePushPipeline()
        "commit" -> TODO() //TODO
        "uncommit" -> TODO() //TODO
        "--version", "-v"-> versioning()
        
        else -> logError("Unknown command: $command. Use 'push' or 'upgrade'.")
    }
}

fun upgradeCodey() {
    logInfo("Upgrading Codey...")
    // Assumes you run this command from inside the codey repository directory
    val result = "./gradlew installDist".runCommand()
    
    result.onSuccess {
        logSuccess("Codey upgraded successfully! Symlink automatically points to the new build.")
    }.onFailure {
        logError("Build failed: ${it.message}")
    }
}

fun versioning() {
    println()
    println("${BLUE}CODEY!${RESET}")
    println("script: 0.0.1+Beta")
    println("native: 0.0.1+Beta (linux x86_64)")
    println()
}

suspend fun executePushPipeline() {
    val context = gatherState()
    val pipeline = listOf(InitGit, CommitChanges, PushCode)
    
    pipeline.forEach { step ->
        if (step.shouldRun(context)) {
            runCatching { step.execute(context) }.onFailure { error ->
                logError("Failed at '${step.name}': ${error.message}")
                return
            }
        }
    }
    logSuccess("Push sequence complete.")
}