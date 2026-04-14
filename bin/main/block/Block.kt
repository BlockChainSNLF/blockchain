package block

data class Block(
    private val preBlock: PreBlock,
    private val hash: String,
) {
    fun getIndex(): Int = preBlock.getIndex()
    fun getTimestamp(): Long = preBlock.getTimestamp()
    fun getTransactions() = preBlock.getTransactions()
    fun getPreviousHash(): String = preBlock.getPreviousHash()
    fun getNonce(): Long = preBlock.getNonce()
    fun getHash(): String = hash
}
