package validators.signature

import io.ethers.crypto.Secp256k1

interface SignatureValidator {
    fun isValid(
        publicKey: String,
        payload: String,
        signature: String
    ): Boolean
}