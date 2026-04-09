package app

import consensus.ConsensusEngine
import consensus.Miner
import mempool.Mempool
import mempool.MempoolManager
import network.DefaultNodeService
import network.NodeService
import network.bootstrap.BootstrapService
import network.broadcast.BroadcastService
import network.client.PeerClient
import network.server.startServer
import validators.block.BlockValidator
import validators.signature.EthersSignatureValidator
import validators.transactions.TransactionValidator
import wallets.address.EthersAddressDerivator
import wallets.balanceService.BalanceService

class Node(private val config: AppConfig) {

    fun start() {
        val peerClient = PeerClient()

        // validadores
        val transactionValidator = TransactionValidator(
            EthersSignatureValidator,
            EthersAddressDerivator,
            BalanceService
        )

        // mempool
        val mempool = Mempool(transactionValidator)
        val mempoolManager = MempoolManager(mempool)

        // broadcast
        val broadcastService = BroadcastService(peerClient)

        // miner y consensus engine
        val miner = Miner(minerAddress = config.address)

        // getPeers es una lambda para evitar dependencia circular entre
        // ConsensusEngine (necesita peers para broadcast) y
        // DefaultNodeService (necesita el engine para existir)
        lateinit var nodeService: NodeService

        val consensusEngine = ConsensusEngine(
            miner = miner,
            transactionValidator = transactionValidator,
            mempoolManager = mempoolManager,
            broadcastService = broadcastService,
            getPeers = { nodeService.getPeers().peers }
        )

        nodeService = DefaultNodeService(
            baseUrl = config.baseUrl,
            address = config.address,
            publicKey = config.publicKey,
            mempoolManager = mempoolManager,
            consensusEngine = consensusEngine,
            initialPeers = config.seedPeers.toSet()
        )

        val bootstrapService = BootstrapService(
            peerClient = peerClient,
            nodeService = nodeService,
            myUrl = config.baseUrl,
            seedPeers = config.seedPeers
        )

        println("Starting node at ${config.baseUrl}")
        println("Node address:  ${config.address}")

        bootstrapService.bootstrap()

        // Si después del bootstrap la chain sigue vacía,
        // este es el primer nodo → minamos el genesis
        if (consensusEngine.chainLength() == 0) {
            println("Empty chain — mining genesis block...")
            consensusEngine.mineGenesisBlock()
        }

        startServer(host = config.bindHost, port = config.port, nodeService = nodeService)
    }
}
