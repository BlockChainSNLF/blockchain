package validators.block

import common.HashUtils
import common.NetworkConfig
import network.dto.BlockDto
import transactions.TransactionMapper
import validators.transactions.Invalid
import validators.transactions.TransactionValidator
import validators.transactions.Valid

object BlockValidator {

    fun validate(
        block: BlockDto,
        previousBlock: BlockDto?,
        transactionValidator: TransactionValidator,
        balances: Map<String, Long> = emptyMap()
    ): BlockValidationResult {

        val errors = mutableListOf<String>()

        checkRequiredFields(block, errors)
        if (errors.isNotEmpty()) return InvalidBlock(errors)

        if (block.index == 0) {
            checkGenesis(block, errors)
        } else {
            checkChaining(block, previousBlock, errors)
            if (errors.isNotEmpty()) return InvalidBlock(errors)

            checkCoinbase(block, errors)
            if (errors.isNotEmpty()) return InvalidBlock(errors)

            checkTransfers(block, transactionValidator, balances, errors)
        }

        if (errors.isNotEmpty()) return InvalidBlock(errors)

        checkCanonicalHash(block, errors)
        checkProofOfWork(block, errors)

        return if (errors.isEmpty()) ValidBlock else InvalidBlock(errors)
    }

    // ── 1. campos obligatorios ────────────────────────────────────────────────

    private fun checkRequiredFields(block: BlockDto, errors: MutableList<String>) {
        if (block.index < 0)
            errors.add("index must be >= 0")
        if (block.timestamp <= 0)
            errors.add("timestamp must be > 0")
        if (block.previousHash.isBlank())
            errors.add("previousHash is required")
        if (block.hash.isBlank())
            errors.add("hash is required")
        if (block.nonce.toLongOrNull()?.let { it < 0 } != false)
            errors.add("nonce must be a non-negative number")
    }

    // ── 2. genesis ────────────────────────────────────────────────────────────

    private fun checkGenesis(block: BlockDto, errors: MutableList<String>) {
        if (block.previousHash != NetworkConfig.GENESIS_PREVIOUS_HASH)
            errors.add("genesis previousHash must be \"${NetworkConfig.GENESIS_PREVIOUS_HASH}\"")
        if (block.transactions.isNotEmpty())
            errors.add("genesis block must have no transactions")
    }

    // ── 3. encadenamiento ─────────────────────────────────────────────────────

    private fun checkChaining(
        block: BlockDto,
        previousBlock: BlockDto?,
        errors: MutableList<String>
    ) {
        if (previousBlock == null) {
            errors.add("previousBlock required for non-genesis block")
            return
        }
        if (block.index != previousBlock.index + 1)
            errors.add("index must be ${previousBlock.index + 1}, got ${block.index}")
        if (block.timestamp <= previousBlock.timestamp)
            errors.add("timestamp must be greater than previous block timestamp")
        if (block.previousHash != previousBlock.hash)
            errors.add("previousHash does not match previous block hash")
    }

    // ── 4. coinbase ───────────────────────────────────────────────────────────

    private fun checkCoinbase(block: BlockDto, errors: MutableList<String>) {
        val coinbases = block.transactions.filter { it.type == "COINBASE" }

        if (coinbases.size != 1) {
            errors.add("block must have exactly one COINBASE (found ${coinbases.size})")
            return
        }
        if (block.transactions.first().type != "COINBASE")
            errors.add("COINBASE must be the first transaction")

        val coinbase = coinbases.first()

        if (coinbase.from != NetworkConfig.COINBASE_FROM)
            errors.add("COINBASE from must be \"${NetworkConfig.COINBASE_FROM}\"")
        if (coinbase.amount != NetworkConfig.BLOCK_REWARD)
            errors.add("COINBASE amount must be ${NetworkConfig.BLOCK_REWARD}, got ${coinbase.amount}")
        if (coinbase.timestamp != block.timestamp)
            errors.add("COINBASE timestamp must match block timestamp")
        if (coinbase.publicKey != NetworkConfig.COINBASE_ZERO)
            errors.add("COINBASE publicKey must be all zeros")
        if (coinbase.signature != NetworkConfig.COINBASE_ZERO)
            errors.add("COINBASE signature must be all zeros")
    }

    // ── 5. transfers ──────────────────────────────────────────────────────────

    private fun checkTransfers(
        block: BlockDto,
        transactionValidator: TransactionValidator,
        balances: Map<String, Long>,
        errors: MutableList<String>
    ) {
        block.transactions
            .filter { it.type == "TRANSFER" }
            .forEach { dto ->
                val tx = try {
                    TransactionMapper.fromDto(dto)
                } catch (e: Exception) {
                    errors.add("could not parse tx ${dto.id}: ${e.message}")
                    return@forEach
                }
                when (val r = transactionValidator.validateTransaction(tx)) {
                    is Invalid -> errors.addAll(r.messages.map { "tx ${dto.id}: $it" })
                    Valid -> Unit
                }
            }
    }

    // ── 6. hash canónico ──────────────────────────────────────────────────────

    private fun checkCanonicalHash(block: BlockDto, errors: MutableList<String>) {
        val txIds = block.transactions.joinToString(",") { it.id }
        val payload = "${block.index}|${block.timestamp}|${block.previousHash}|${block.nonce}|$txIds"
        val recomputed = HashUtils.sha256Hex(payload)
        if (recomputed != block.hash)
            errors.add("hash does not match canonical recomputed hash")
    }

    // ── 7. proof of work ──────────────────────────────────────────────────────

    private fun checkProofOfWork(block: BlockDto, errors: MutableList<String>) {
        if (!block.hash.startsWith(NetworkConfig.REQUIRED_PREFIX))
            errors.add("hash must start with \"${NetworkConfig.REQUIRED_PREFIX}\" (difficulty ${NetworkConfig.DIFFICULTY})")
    }
}
