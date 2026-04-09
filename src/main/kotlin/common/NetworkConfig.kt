package common

object NetworkConfig {
    const val DIFFICULTY        = 4
    const val BLOCK_REWARD      = 10
    const val AUTO_MINE_THRESHOLD = 3

    val REQUIRED_PREFIX: String = "0".repeat(DIFFICULTY)

    const val COINBASE_FROM = "SYSTEM"
    const val COINBASE_ZERO = "0000000000000000000000000000000000000000000000000000000000000000"
    const val GENESIS_PREVIOUS_HASH = "0"
}
