package miner

import block.Block

sealed interface MineResult

data class MinedBlock(
    val block: Block,
    val chainLength: Int,
    val trigger: String,
) : MineResult

data class NotMined(
    val reason: String,
) : MineResult

