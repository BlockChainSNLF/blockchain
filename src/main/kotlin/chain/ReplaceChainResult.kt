package chain

sealed interface ReplaceChainResult

data class ChainReplaced(
    val chainLength: Int,
) : ReplaceChainResult

data class ChainNotReplaced(
    val reason: String,
) : ReplaceChainResult

