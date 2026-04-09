package chain

import block.BlockHashing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import validators.block.Valid
import validators.block.BlockValidator
import validators.signature.SignatureValidator
import validators.transactions.TransactionValidator
import wallets.address.AddressDerivator
import wallets.balanceService.BalanceService

class GenesisBlockFactoryTest {
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

    @Test
    fun `genesis block is canonical and valid`() {
        BalanceService.reset()

        val genesis = GenesisBlockFactory.createGenesis()
        val validator = BlockValidator(transactionValidator)

        assertEquals(0, genesis.getIndex())
        assertEquals(1L, genesis.getTimestamp())
        assertEquals("0", genesis.getPreviousHash())
        assertTrue(genesis.getTransactions().isEmpty())
        assertTrue(genesis.getHash().startsWith("0000"))
        assertEquals(genesis.getHash(), BlockHashing.recompute(genesis))
        assertTrue(validator.validateBlock(genesis) is Valid)
    }
}


