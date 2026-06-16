data class RepoContext(
    val isGitRepo: Boolean,
    var hasUncommitted: Boolean, 
    val currentBranch: String,
    val hasOriginRemote: Boolean
)