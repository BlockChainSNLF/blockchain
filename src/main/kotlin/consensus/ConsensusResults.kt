package consensus

import network.dto.BlockDto

// ── appendBlock ───────────────────────────────────────────────────────────────

sealed class AppendBlockResult

data class Appended(val block: BlockDto, val chainLength: Int) : AppendBlockResult()
data class RejectedBlock(val reasons: List<String>) : AppendBlockResult()

// ── replaceChain ──────────────────────────────────────────────────────────────

sealed class ReplaceChainResult

data object Replaced : ReplaceChainResult()
data object IgnoredTooShort : ReplaceChainResult()
data class InvalidChain(val reasons: List<String>) : ReplaceChainResult()

// ── mineBlock ─────────────────────────────────────────────────────────────────

sealed class MineBlockResult

data class Mined(val block: BlockDto, val trigger: String) : MineBlockResult()
data class MiningFailed(val reason: String) : MineBlockResult()
