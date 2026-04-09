package chain

import block.Block
import wallets.balanceService.BalanceService
import validators.block.BlockValidator
import validators.block.Invalid
import validators.block.Valid

class ChainManager(
    private val blockValidator: BlockValidator,
    initialChain: List<Block>? = null,
) {
    private var chain: List<Block> = initialChain ?: listOf(GenesisBlockFactory.createGenesis())

    init {
        BalanceService.rebuildFromChain(chain)
    }

    fun all(): List<Block> = chain

    fun latest(): Block? = chain.lastOrNull()

    fun addBlock(block: Block): AddBlockResult {
        val expectedIndex = latest()?.getIndex()?.plus(1) ?: 0
        if (block.getIndex() != expectedIndex) {
            return BlockRejected(listOf("index must be $expectedIndex"))
        }

        val previousBlock = latest()
        return when (val validation = blockValidator.validateBlock(block, previousBlock)) {
            Valid -> {
                chain = chain + block
                BalanceService.rebuildFromChain(chain)
                BlockAppended(block = block, chainLength = chain.size)
            }

            is Invalid -> BlockRejected(validation.messages)
        }
    }

    fun replaceIfLongerValid(candidate: List<Block>): ReplaceChainResult {
        if (candidate.size <= chain.size) {
            return ChainNotReplaced("candidate chain must be longer")
        }

        if (!isValidChain(candidate)) {
            return ChainNotReplaced("candidate chain is invalid")
        }

        chain = candidate
        BalanceService.rebuildFromChain(chain)
        return ChainReplaced(chainLength = chain.size)
    }

    private fun isValidChain(candidate: List<Block>): Boolean {
        candidate.forEachIndexed { index, block ->
            val previous = if (index == 0) null else candidate[index - 1]
            val validation = blockValidator.validateBlock(block, previous)
            if (validation is Invalid) {
                return false
            }
        }

        return true
    }
}
