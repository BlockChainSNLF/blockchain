package ui

import network.dto.TransactionDto

data class TransactionSubmissionResult(
    val success: Boolean,
    val message: String
)

interface TransactionSubmissionGateway {
    fun submit(transaction: TransactionDto): TransactionSubmissionResult
}

