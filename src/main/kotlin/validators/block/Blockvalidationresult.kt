package validators.block

sealed class BlockValidationResult

data object ValidBlock : BlockValidationResult()
data class InvalidBlock(val reasons: List<String>) : BlockValidationResult()
