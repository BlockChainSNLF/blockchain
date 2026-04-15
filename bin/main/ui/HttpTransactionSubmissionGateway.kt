package ui

import kotlinx.coroutines.runBlocking
import network.client.PeerClient
import network.client.SubmitTransactionAccepted
import network.client.SubmitTransactionFailed
import network.client.SubmitTransactionRejected
import network.dto.TransactionDto

class HttpTransactionSubmissionGateway(
    private val peerClient: PeerClient,
    private val nodeUrl: String
) : TransactionSubmissionGateway {
    override fun submit(transaction: TransactionDto): TransactionSubmissionResult = runBlocking {
        return@runBlocking when (val result = peerClient.submitTransaction(nodeUrl, transaction)) {
            is SubmitTransactionAccepted -> TransactionSubmissionResult(
                success = true,
                message = "Transaction accepted: ${result.txId}"
            )

            is SubmitTransactionRejected -> TransactionSubmissionResult(
                success = false,
                message = "${result.code}: ${result.message}"
            )

            is SubmitTransactionFailed -> TransactionSubmissionResult(
                success = false,
                message = result.message
            )
        }
    }
}



