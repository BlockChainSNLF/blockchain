package mempool

import transactions.TransferTransaction
import transactions.Transaction
import validators.transactions.Invalid
import validators.transactions.TransactionValidator
import validators.transactions.Valid
import wallets.balanceService.BalanceService

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
            }else if (transaction is TransferTransaction && !hasSufficientBalanceIncludingPending(transaction)) {
                Rejected(Invalid(listOf("sender has insufficient balance considering pending transactions")))
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

    private fun hasSufficientBalanceIncludingPending(transaction: TransferTransaction): Boolean {
        val pendingSpent = transactions
            .filterIsInstance<TransferTransaction>()
            .filter { it.getFrom() == transaction.getFrom() }
            .sumOf { it.getAmount() }

        val available = BalanceService.getBalance(transaction.getFrom()) - pendingSpent
        return available >= transaction.getAmount()
    }
}