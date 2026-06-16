import kotlinx.coroutines.*
import java.io.File

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