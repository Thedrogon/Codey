sealed interface Step {
    val name: String
    fun shouldRun(ctx: RepoContext): Boolean
    suspend fun execute(ctx: RepoContext)
}

object InitGit : Step {
    override val name = "Initialize Repository"
    override fun shouldRun(ctx: RepoContext) = !ctx.isGitRepo
    override suspend fun execute(ctx: RepoContext) {
        logInfo("Initializing new git repository...")
        "git init".runCommand().getOrThrow()
    }
}

object CommitChanges : Step {
    override val name = "Commit Changes"
    override fun shouldRun(ctx: RepoContext) = ctx.hasUncommitted
    override suspend fun execute(ctx: RepoContext) {
        print("Enter commit message: ")
        val message = readlnOrNull()?.takeIf { it.isNotBlank() } ?: "Update project"
        
        logInfo("Committing changes...")
        "git add .".runCommand().getOrThrow()
        "git commit -m \"$message\"".runCommand().getOrThrow()
        ctx.hasUncommitted = false 
    }
}

object PushCode : Step {
    override val name = "Push to Remote"
    override fun shouldRun(ctx: RepoContext) = ctx.hasOriginRemote 
    override suspend fun execute(ctx: RepoContext) {
        logInfo("Pushing to origin/${ctx.currentBranch}...")
        "git push -u origin ${ctx.currentBranch}".runCommand().getOrThrow()
    }
}