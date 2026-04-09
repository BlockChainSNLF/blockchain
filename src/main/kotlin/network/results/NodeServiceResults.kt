package network.results

import network.dto.BlockDto

sealed interface ReceiveBlockResult
data class BlockAccepted(val chainLength: Int) : ReceiveBlockResult
data class BlockRejected(val reasons: List<String>) : ReceiveBlockResult

sealed interface MineResult
data class MinedSuccessfully(val block: BlockDto, val trigger: String) : MineResult
data class MineFailed(val reason: String) : MineResult