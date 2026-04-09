package network.results

sealed interface SubmitTransactionResult

data class Accepted(val txId: String) : SubmitTransactionResult

data class RejectedSubmission(
    val code: String,
    val message: String
) : SubmitTransactionResult