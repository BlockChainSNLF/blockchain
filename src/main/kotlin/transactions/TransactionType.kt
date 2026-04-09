package transactions

sealed class TransactionType

data object Transfer: TransactionType()
data object Coinbase: TransactionType()