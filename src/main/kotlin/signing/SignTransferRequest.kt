package signing

data class SignTransferRequest(
    val privateKey: String,
    val to: String,
    val amount: Int,
    val timestamp: Long? = null
)

