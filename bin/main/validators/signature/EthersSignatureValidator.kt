package validators.signature



import common.hexToBytes
import io.ethers.crypto.Hashing
import io.ethers.crypto.Secp256k1
import java.math.BigInteger
import java.util.Base64

object EthersSignatureValidator : SignatureValidator {

    override fun isValid(
        publicKey: String,
        payload: String,
        signature: String
    ): Boolean {
        return try {
            val expectedPublicKey = hexToBytes(publicKey)

            val messageHash = Hashing.keccak256(
                payload.toByteArray(Charsets.UTF_8)
            )

            val signatureBytes = Base64.getDecoder().decode(signature)
            if (signatureBytes.size != 65) return false

            val r = BigInteger(1, signatureBytes.copyOfRange(0, 32))
            val s = BigInteger(1, signatureBytes.copyOfRange(32, 64))
            val v = signatureBytes[64].toLong()

            val recoveredPublicKey = Secp256k1.recoverPublicKey(
                messageHash,
                r,
                s,
                v
            )

            expectedPublicKey.contentEquals(recoveredPublicKey)
        } catch (_: Exception) {
            false
        }
    }
}