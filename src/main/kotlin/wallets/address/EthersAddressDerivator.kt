package wallets.address

import common.hexToBytes
import io.ethers.crypto.Secp256k1


object EthersAddressDerivator : AddressDerivator {
    override fun deriveAddress(publicKey: String): String {
        return Secp256k1.publicKeyToAddress(hexToBytes(publicKey)).toString()
    }



}