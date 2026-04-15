package mempool

import block.Block
import block.PreBlock
import transactions.CoinbaseTransaction
import transactions.TransferTransaction
import validators.signature.SignatureValidator
import validators.transactions.TransactionValidator
import wallets.address.AddressDerivator
import wallets.balanceService.BalanceService
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class MempoolTest {
    private val sender = "0x${"b".repeat(40)}"
    private val receiver = "0x${"c".repeat(40)}"
    private val publicKey = "04${"a".repeat(128)}"

    private val signatureValidator = object : SignatureValidator {
        override fun isValid(publicKey: String, payload: String, signature: String): Boolean = true
    }

    private val addressDerivator = object : AddressDerivator {
        override fun deriveAddress(publicKey: String): String = sender
    }

    private val transactionValidator = TransactionValidator(
        signatureValidator = signatureValidator,
        addressDerivator = addressDerivator,
        balanceService = BalanceService
    )

    @BeforeTest
    fun setup() {
        BalanceService.reset()
        BalanceService.rebuildFromChain(
            listOf(
                Block(
                    preBlock = PreBlock(
                        index = 0,
                        timestamp = 1L,
                        transactions = listOf(
                            CoinbaseTransaction(
                                id = UUID.randomUUID().toString(),
                                to = sender,
                                amount = 10,
                                timestamp = 1L
                            )
                        ),
                        previousHash = "0",
                        nonce = 0L
                    ),
                    hash = "test-hash"
                )
            )
        )
    }

    @Test
    fun `rejects transfer that overspends with pending transactions`() {
        val mempool = Mempool(transactionValidator)

        val first = mempool.addTransaction(newTransfer(amount = 10, timestamp = 2L))
        assertTrue(first is Added)

        val second = first.mempool.addTransaction(newTransfer(amount = 1, timestamp = 3L))
        assertTrue(second is Rejected)
        assertTrue(second.validationResult.messages.any { it.contains("pending") })
    }

    private fun newTransfer(amount: Int, timestamp: Long): TransferTransaction {
        return TransferTransaction(
            id = UUID.randomUUID().toString(),
            from = sender,
            to = receiver,
            amount = amount,
            timestamp = timestamp,
            publicKey = publicKey,
            signature = "dummy-signature"
        )
    }
}

