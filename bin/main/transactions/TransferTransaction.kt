package transactions

data class TransferTransaction(
        private val id: String,
        private val from: String,
        private val to: String,
        private val amount: Int,
        private val timestamp: Long,
        private val publicKey: String,
        private val signature: String
) : Transaction {
    private val type = Transfer

    override fun getId(): String = id
    override fun getType(): TransactionType = type
    override fun getFrom(): String = from
    override fun getTo(): String = to
    override fun getAmount(): Int = amount
    override fun getTimestamp(): Long = timestamp
    override fun getPublicKey(): String = publicKey
    override fun getSignature(): String = signature
}