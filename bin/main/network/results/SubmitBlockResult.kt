package network.results


sealed interface SubmitBlockResultResult

data class AcceptedBlock(val chainLength: Int) : SubmitBlockResultResult

data class RejectedBlockSubmission(
    val code: String,
    val message: String
) : SubmitBlockResultResult