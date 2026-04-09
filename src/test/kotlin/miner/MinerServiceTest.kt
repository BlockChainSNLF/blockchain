package miner

import chain.BlockRejected
import chain.ChainManager
import mempool.Mempool
import mempool.MempoolManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import validators.block.BlockValidator
import validators.signature.SignatureValidator
import validators.transactions.TransactionValidator
import wallets.address.AddressDerivator
import wallets.balanceService.BalanceService

class MinerServiceTest {
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
    fun `chain starts with genesis and mining appends next blocks`() {
        val blockValidator = BlockValidator(transactionValidator)
        val chainManager = ChainManager(blockValidator)
        val mempoolManager = MempoolManager(Mempool(transactionValidator))
        val minerAddress = "0x${"b".repeat(40)}"

        val minerService = MinerService(
            chainManager = chainManager,
            mempoolManager = mempoolManager,
            minerAddress = minerAddress,
            nowProvider = { 1_700_000_000_000L },
        )

        assertEquals(1, chainManager.all().size)
        assertEquals(0, chainManager.all().first().getIndex())

        val genesisResult = minerService.mineManual()
        assertTrue(genesisResult is MinedBlock)
        assertEquals(2, chainManager.all().size)
        assertEquals(1, chainManager.all().last().getIndex())
        assertEquals(10, BalanceService.getBalance(minerAddress))

        val nextResult = minerService.mineManual()
        assertTrue(nextResult is MinedBlock)
        assertEquals(3, chainManager.all().size)
        assertEquals(2, chainManager.all().last().getIndex())
        assertEquals(20, BalanceService.getBalance(minerAddress))
    }

    @Test
    fun `chain manager rejects duplicate mined block`() {
        val blockValidator = BlockValidator(transactionValidator)
        val chainManager = ChainManager(blockValidator)
        val mempoolManager = MempoolManager(Mempool(transactionValidator))

        val minerService = MinerService(
            chainManager = chainManager,
            mempoolManager = mempoolManager,
            minerAddress = "0x${"c".repeat(40)}",
            nowProvider = { 1_700_000_000_500L },
        )

        val mined = minerService.mineManual()
        assertTrue(mined is MinedBlock)

        val append = chainManager.addBlock(chainManager.latest()!!)
        assertTrue(append is BlockRejected)
    }
}
