package wallets.address

interface AddressDerivator {
    fun deriveAddress(publicKey: String): String
}