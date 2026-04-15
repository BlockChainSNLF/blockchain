package app

import mempool.Mempool
import mempool.MempoolManager
import network.DefaultNodeService
import network.NodeService
import network.bootstrap.BootstrapService
import network.broadcast.BroadcastService
import network.client.PeerClient
import network.server.startServer
import signing.EthersTransactionSigner
import ui.HttpTransactionSubmissionGateway
import ui.web.configureWebTransactionUi
import validators.signature.EthersSignatureValidator
import validators.transactions.TransactionValidator
import wallets.address.EthersAddressDerivator
import wallets.balanceService.BalanceService

class Node(
    private val config: AppConfig
) {
    fun start() {
        val peerClient = PeerClient()

        val transactionValidator = TransactionValidator(
            EthersSignatureValidator,
            EthersAddressDerivator,
            BalanceService
        )
        val mempool = Mempool(transactionValidator)
        val mempoolManager = MempoolManager(mempool)

        val nodeService: NodeService = DefaultNodeService(
            baseUrl = config.baseUrl,
            address = config.address,
            publicKey = config.publicKey,
            peerClient = peerClient,
            transactionValidator = transactionValidator,
            mempoolManager = mempoolManager,
            broadcastService = BroadcastService(peerClient),
            initialChain = emptyList(),
            initialPeers = config.seedPeers.toSet()
        )

        val bootstrapService = BootstrapService(
            peerClient = peerClient,
            nodeService = nodeService,
            myUrl = config.baseUrl,
            seedPeers = config.seedPeers
        )

        println("Starting node at ${config.baseUrl}")
        println("Node address: ${config.address}")

        bootstrapService.bootstrap()

        val transactionSigner = EthersTransactionSigner()
        val transactionSubmissionGateway = HttpTransactionSubmissionGateway(
            peerClient = peerClient,
            nodeUrl = config.baseUrl
        )

        startServer(
            host = config.bindHost,
            port = config.port,
            nodeService = nodeService,
            additionalRoutes = {
                configureWebTransactionUi(
                    signer = transactionSigner,
                    submissionGateway = transactionSubmissionGateway
                )
            }
        )
    }
}