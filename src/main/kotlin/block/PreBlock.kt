package block

import transactions.Transaction

data class PreBlock(
    private val index: Int,
    private val timestamp: Long,
    private val transactions: List<Transaction>,
    private val previousHash: String,
    private val nonce: Long,
) {
    fun getIndex(): Int = index
    fun getTimestamp(): Long = timestamp
    fun getTransactions(): List<Transaction> = transactions
    fun getPreviousHash(): String = previousHash
    fun getNonce(): Long = nonce
}
