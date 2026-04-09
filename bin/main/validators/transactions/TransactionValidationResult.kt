package validators.transactions

sealed class TransactionValidationResult

data object Valid : TransactionValidationResult()
data class Invalid(val messages: List<String>) : TransactionValidationResult()

