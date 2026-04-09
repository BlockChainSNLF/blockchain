package transactions

sealed interface Transaction {
    fun getId(): String
    fun getType():TransactionType
    fun getFrom(): String
    fun getTo(): String
    fun getAmount(): Int
    fun getTimestamp(): Long
    fun getPublicKey(): String
    fun getSignature(): String
}