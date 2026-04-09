package validators.block

sealed class BlockValidationResult

data object Valid : BlockValidationResult()
data class Invalid(val messages: List<String>) : BlockValidationResult()

