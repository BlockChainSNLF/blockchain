package transactions

data class CoinbaseTransaction(
        private val id: String,
        private val to: String,
        private val amount: Int,
        private val timestamp: Long
) : Transaction {
    private val type = Coinbase

    override fun getId(): String = id
    override fun getType(): TransactionType = type
    override fun getFrom(): String = "SYSTEM"
    override fun getTo(): String = to
    override fun getAmount(): Int = amount
    override fun getTimestamp(): Long = timestamp
    override fun getPublicKey(): String = "0000000000000000000000000000000000000000000000000000000000000000"
    override fun getSignature(): String = "0000000000000000000000000000000000000000000000000000000000000000"
}