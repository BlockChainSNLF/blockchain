package wallets

data class Wallet(
    val privateKey: String,
    val publicKey: String,
    val address: String
) {
}