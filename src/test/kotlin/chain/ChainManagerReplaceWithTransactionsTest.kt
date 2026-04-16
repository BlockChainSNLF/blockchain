package chain

import app.NetworkParams
import block.Block
import block.BlockHashing
import block.PreBlock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import transactions.CoinbaseTransaction
import transactions.Transaction
import transactions.TransferTransaction
import validators.block.BlockValidator
import validators.signature.SignatureValidator
import validators.transactions.TransactionValidator
import wallets.address.AddressDerivator
import wallets.balanceService.BalanceService

class ChainManagerReplaceWithTransactionsTest {
    private val signatureValidator = object : SignatureValidator {
        override fun isValid(publicKey: String, payload: String, signature: String): Boolean = true
    }

    private val addressDerivator = object : AddressDerivator {
        override fun deriveAddress(publicKey: String): String = "0x${"a".repeat(40)}"
    }

    private val transactionValidator = TransactionValidator(
        signatureValidator = signatureValidator,
        addressDerivator = addressDerivator,
        balanceService = BalanceService,
    )

    @BeforeTest
    fun resetLedger() {
        BalanceService.reset()
    }

    @Test
    fun `replace longer candidate with transfers validates against candidate state`() {
        val chainManager = ChainManager(BlockValidator(transactionValidator))

        val minerA = "0x${"a".repeat(40)}"
        val minerB = "0x${"b".repeat(40)}"
        val publicKey = "04${"1".repeat(128)}"

        val genesis = GenesisBlockFactory.createGenesis()

        val block1 = mineBlock(
            index = 1,
            timestamp = 2L,
            previousHash = genesis.getHash(),
            transactions = listOf(
                CoinbaseTransaction(
                    id = "7f9c03b6-ae8d-4b74-9d34-9fd566f6d34e",
                    to = minerA,
                    amount = NetworkParams.BLOCK_REWARD,
                    timestamp = 2L,
                ),
            ),
        )

        val block2 = mineBlock(
            index = 2,
            timestamp = 3L,
            previousHash = block1.getHash(),
            transactions = listOf(
                CoinbaseTransaction(
                    id = "ee3a8e8f-8904-4f95-b2e1-e4f4ecdb95ae",
                    to = minerB,
                    amount = NetworkParams.BLOCK_REWARD,
                    timestamp = 3L,
                ),
                TransferTransaction(
                    id = "8f10bb4e-cb25-4d4e-9d35-8991895c0e6e",
                    from = minerA,
                    to = minerB,
                    amount = 5,
                    timestamp = 3L,
                    publicKey = publicKey,
                    signature = "signature",
                ),
            ),
        )

        val result = chainManager.replaceIfLongerValid(listOf(genesis, block1, block2))

        assertTrue(result is ChainReplaced)
        assertEquals(3, chainManager.all().size)
        assertEquals(5, BalanceService.getBalance(minerA))
        assertEquals(15, BalanceService.getBalance(minerB))
    }

    private fun mineBlock(
        index: Int,
        timestamp: Long,
        previousHash: String,
        transactions: List<Transaction>,
    ): Block {
        var nonce = 0L
        val txIds = transactions.map { it.getId() }

        while (true) {
            val payload = BlockHashing.canonicalPayload(
                index = index,
                timestamp = timestamp,
                previousHash = previousHash,
                nonce = nonce,
                transactionIds = txIds,
            )
            val hash = BlockHashing.hashHex(payload)
            if (hash.startsWith(NetworkParams.DIFFICULTY_PREFIX)) {
                return Block(
                    preBlock = PreBlock(
                        index = index,
                        timestamp = timestamp,
                        transactions = transactions,
                        previousHash = previousHash,
                        nonce = nonce,
                    ),
                    hash = hash,
                )
            }

            nonce++
        }
    }
}

