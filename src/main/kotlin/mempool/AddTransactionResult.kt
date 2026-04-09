package mempool

import validators.transactions.Invalid

sealed class AddTransactionResult

data class Added(val mempool: Mempool) : AddTransactionResult()
data class Rejected(val validationResult: Invalid) : AddTransactionResult()
data object Duplicate : AddTransactionResult()