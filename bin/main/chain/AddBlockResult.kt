package chain

import block.Block

sealed interface AddBlockResult

data class BlockAppended(
    val block: Block,
    val chainLength: Int,
) : AddBlockResult

data class BlockRejected(
    val errors: List<String>,
) : AddBlockResult

