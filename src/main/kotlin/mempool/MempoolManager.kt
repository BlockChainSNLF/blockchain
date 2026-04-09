package mempool

import transactions.Transaction

class MempoolManager(
    initial: Mempool
) {
    private var current: Mempool = initial

    fun get(): Mempool = current

    fun addTransaction(transaction: Transaction): AddTransactionResult {

        return when (val result = current.addTransaction(transaction)) {

            is Added -> {
                current = result.mempool
                result
            }

            Duplicate -> result

            is Rejected -> result
        }
    }

    fun removeByIds(ids: List<String>) {
        current = current.removeByIds(ids)
    }
}