# Codey 🚀

Codey is an unbloated Git client built to streamline development workflows. Written entirely in Kotlin, it eliminates the verbosity of Git and GitHub CLI (`gh`) commands through concurrent state gathering and an intelligent execution pipeline.

Designed natively on and for Linux, it operates as a standalone JVM binary optimized for rapid iteration.

---

## 🏗️ System Architecture

### 1. The Immutable State Machine (`State.kt`)
At the core of the tool is the `RepoContext` data class. Instead of querying the system repeatedly, Codey takes a single, immutable snapshot of the repository's current state (e.g., `isGitRepo`, `hasUncommitted`, `hasOriginRemote`).

### 2. Concurrent State Gathering (`Main.kt`)
To achieve peak efficiency, Codey leverages Kotlin's **Structured Concurrency**. Using `coroutineScope` and `async(Dispatchers.IO)`, system checks are fired off simultaneously rather than sequentially. Gathering the context takes only as long as the single slowest Git command.

### 3. The Pipeline Execution Pattern (`Steps.kt`)
Business logic is decoupled into a sequence of isolated implementations of a sealed `Step` interface.
* Each step has a `shouldRun(ctx: RepoContext): Boolean` function.
* The router iterates through the pipeline, strictly executing only the necessary operations based on the gathered context. 
* This makes Codey **idempotent**—running `codey push` multiple times safely does nothing if the state hasn't changed.

---

## 🛠️ Currently Implemented

* **`codey push` (The Core Engine)**
  Automatically detects repository status. If uninitialized, it runs `git init`. If files are modified, it prompts for a clean commit message and executes `git add .` and `git commit`. Finally, it pushes to the current branch.
* **`codey upgrade` (Self-Dogfooding)**
  Codey builds itself. By invoking Gradle's `installDist` under the hood, this command recompiles the Kotlin source code and instantly updates the global `/usr/local/bin/codey` symlink.

---

## 🗺️ What's Next (The Roadmap)

As development continues, Codey will expand to handle complex, verbose Git operations with single-word commands:

### Phase 1: Local History Management
* **`codey commit`**: Take a local snapshot of the work without pushing to a remote.
* **`codey uncommit`**: The "Oops" button. Executes `git reset --soft HEAD~1` to undo the last commit while keeping working directory changes intact.

### Phase 2: Visibility and Search
* **`codey log`**: Renders a clean, color-coded, one-line graphical tree of the project history.
* **`codey log --me`**: Dynamically filters the git log to only show commits authored by the current user.
* **`codey search "<term>"`**: Greps the entire commit history for specific bug numbers or keywords.

### Phase 3: GitHub CLI (`gh`) Integration
* Implement the `CreateRemote` step in the pipeline.
* Automatically detect if `hasOriginRemote` is false.
* Interactively prompt the user for "Public" or "Private" repository creation via `gh repo create` before executing the push phase.

---

## ⚙️ Development Setup

**Prerequisites:**
* Java Virtual Machine (JVM)
* Gradle

**Building from Source:**
1. Clone the repository.
2. Build the distribution: `./gradlew installDist`
3. Create the global symlink: `sudo ln -s $(pwd)/build/install/codey/bin/codey /usr/local/bin/codey`

**Ignoring Artifacts:**
Ensure your `.gitignore` strictly excludes `/build/`, `/.gradle/`, and `/.kotlin/` to prevent committing heavy JVM build artifacts to the source tree.

---
*Maintained by Sayan.*
