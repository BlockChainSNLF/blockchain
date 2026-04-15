package validators.block

import app.NetworkParams
import block.Block
import block.BlockHashing
import transactions.Coinbase
import transactions.CoinbaseTransaction
import transactions.TransferTransaction
import validators.transactions.Invalid as TransactionInvalid
import validators.transactions.TransactionValidator
import validators.transactions.Valid as TransactionValid
import wallets.balanceService.BalanceService

private const val ZERO_64 = "0000000000000000000000000000000000000000000000000000000000000000"

class BlockValidator(
    private val transactionValidator: TransactionValidator,
) {
    fun validateBlock(block: Block, previousBlock: Block? = null): BlockValidationResult {
        val errors = mutableListOf<String>()

        validateStructure(block, errors)

        val result = if (errors.isNotEmpty()) {
            Invalid(errors.toList())
        } else {
            validateCryptography(block, errors)
            validateState(block, previousBlock, errors)
            if (errors.isEmpty()) Valid else Invalid(errors.toList())
        }

        when (result) {
            Valid -> println("Block validation: VALID | index=${block.getIndex()} | hash=${block.getHash()} | msg=ok")
            is Invalid -> println(
                "Block validation: INVALID | index=${block.getIndex()} | hash=${block.getHash()} | msg=${result.messages.joinToString("; ")}"
            )
        }

        return result
    }

    private fun validateStructure(
        block: Block,
        errors: MutableList<String>,
    ) {
        if (block.getIndex() < 0) {
            errors.add("index must be greater than or equal to 0")
        }

        if (block.getTimestamp() <= 0) {
            errors.add("timestamp must be greater than 0")
        }

        if (block.getPreviousHash().isBlank()) {
            errors.add("previousHash is required")
        }

        if (block.getHash().isBlank()) {
            errors.add("hash is required")
        }

        if (block.getNonce() < 0) {
            errors.add("nonce must be greater than or equal to 0")
        }

        if (block.getIndex() == 0) {
            if (block.getPreviousHash() != "0") {
                errors.add("genesis block previousHash must be '0'")
            }

            if (block.getTransactions().isNotEmpty()) {
                errors.add("genesis block transactions must be []")
            }
        } else {
            val transactions = block.getTransactions()
            if (transactions.isEmpty()) {
                errors.add("transactions are required for non-genesis blocks")
                return
            }

            if (transactions.first().getType() != Coinbase) {
                errors.add("transactions[0] must be a COINBASE transaction")
            }

            val coinbaseTransactions = transactions.filterIsInstance<CoinbaseTransaction>()
            if (coinbaseTransactions.size != 1) {
                errors.add("there must be exactly one COINBASE transaction")
            } else {
                val coinbase = coinbaseTransactions.first()

                if (coinbase.getFrom() != "SYSTEM") {
                    errors.add("COINBASE from must be SYSTEM")
                }

                if (coinbase.getAmount() != NetworkParams.BLOCK_REWARD) {
                    errors.add("COINBASE amount must be ${NetworkParams.BLOCK_REWARD}")
                }

                if (!isValidAddressHex(coinbase.getTo())) {
                    errors.add("COINBASE to must be a valid address")
                }

                if (coinbase.getPublicKey() != ZERO_64) {
                    errors.add("COINBASE publicKey must be all zeros")
                }

                if (coinbase.getSignature() != ZERO_64) {
                    errors.add("COINBASE signature must be all zeros")
                }

                if (coinbase.getTimestamp() != block.getTimestamp()) {
                    errors.add("COINBASE timestamp must match block timestamp")
                }
            }
        }
    }

    private fun validateCryptography(
        block: Block,
        errors: MutableList<String>,
    ) {
        val recomputedHash = BlockHashing.recompute(block)
        if (block.getHash() != recomputedHash) {
            errors.add("hash must match the canonical recomputed hash")
        }

        if (!block.getHash().startsWith(NetworkParams.DIFFICULTY_PREFIX)) {
            errors.add("hash must start with ${NetworkParams.DIFFICULTY_PREFIX}")
        }
    }

    private fun validateState(
        block: Block,
        previousBlock: Block?,
        errors: MutableList<String>,
    ) {
        if (block.getIndex() == 0) {
            return
        }

        if (previousBlock == null) {
            errors.add("previous block is required when index > 0")
        } else {
            if (block.getPreviousHash() != previousBlock.getHash()) {
                errors.add("previousHash must match previous block hash")
            }


            if (block.getTimestamp() <= previousBlock.getTimestamp()) {
                errors.add("timestamp must be strictly greater than previous block timestamp")
            }
        }

        val balanceDeltas = mutableMapOf<String, Int>()

        block.getTransactions().forEachIndexed { txIndex, transaction ->
            when (val validation = transactionValidator.validateTransaction(transaction)) {
                is TransactionInvalid -> {
                    validation.messages.forEach { message ->
                        errors.add("transactions[$txIndex]: $message")
                    }
                }

                TransactionValid -> {
                    when (transaction) {
                        is CoinbaseTransaction -> {
                            balanceDeltas[transaction.getTo()] = balanceDeltas.getOrDefault(transaction.getTo(), 0) + transaction.getAmount()
                        }

                        is TransferTransaction -> {
                            val availableBalance = BalanceService.getBalance(transaction.getFrom()) + balanceDeltas.getOrDefault(transaction.getFrom(), 0)
                            if (availableBalance < transaction.getAmount()) {
                                errors.add("transactions[$txIndex]: sender has insufficient balance considering previous transactions in the block")
                            } else {
                                balanceDeltas[transaction.getFrom()] = balanceDeltas.getOrDefault(transaction.getFrom(), 0) - transaction.getAmount()
                                balanceDeltas[transaction.getTo()] = balanceDeltas.getOrDefault(transaction.getTo(), 0) + transaction.getAmount()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isValidAddressHex(address: String): Boolean {
        val clean = address.removePrefix("0x")

        if (clean.length != 40) return false
        if (!clean.matches(Regex("[0-9a-fA-F]+"))) return false

        return true
    }
}