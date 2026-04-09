package wallets.factory

import io.ethers.crypto.Secp256k1
import java.security.SecureRandom
import wallets.Wallet
import wallets.address.EthersAddressDerivator

object EthersWalletFactory: WalletFActory {

    private val random = SecureRandom()

    override fun createWallet(): Wallet {
        val privateKeyBytes = ByteArray(32)
        random.nextBytes(privateKeyBytes)

        val privateKeyHex = privateKeyBytes.toHexString()

        val signingKey = Secp256k1.SigningKey(privateKeyBytes)

        val publicKeyHex = signingKey.publicKey.toHexString()

        val address = EthersAddressDerivator.deriveAddress(publicKeyHex)

        return Wallet(
            privateKey = privateKeyHex,
            publicKey = publicKeyHex,
            address = address
        )
    }
}