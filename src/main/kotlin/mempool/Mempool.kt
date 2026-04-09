package mempool

import transactions.Transaction
import validators.transactions.Invalid
import validators.transactions.TransactionValidator
import validators.transactions.Valid

class Mempool private constructor(
    private val transactions: List<Transaction>,
    private val validator: TransactionValidator
) {

    constructor(
        validator: TransactionValidator
    ) : this(
        emptyList<Transaction>(),
        validator
    )


    fun all(): List<Transaction> = transactions


    fun addTransaction(transaction: Transaction): AddTransactionResult {

        return when (val result = validator.validateTransaction(transaction)) {
            is Invalid -> Rejected(result)
            Valid -> if (isDuplicate(transaction)){
                Duplicate
            }else{
                Added(Mempool(transactions + transaction, validator))
            }
        }
    }


    fun removeByIds(ids: List<String>): Mempool {
        return Mempool(transactions.filter { it.getId() !in ids }, validator)
    }

    fun isDuplicate(transaction: Transaction): Boolean {
        return transactions.any { it.getId() == transaction.getId() }
    }
}