package signing

import common.hexToBytes
import io.ethers.crypto.Secp256k1
import network.dto.TransactionDto
import wallets.address.EthersAddressDerivator
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID

class EthersTransactionSigner : TransactionSigner {
    override fun signTransfer(request: SignTransferRequest): TransactionDto {
        val privateKeyBytes = hexToBytes(request.privateKey)
        val signingKey = Secp256k1.SigningKey(privateKeyBytes)
        val publicKey = signingKey.publicKey.toHexString()
        val from = EthersAddressDerivator.deriveAddress(publicKey)
        val timestamp = request.timestamp ?: System.currentTimeMillis()

        val payload = "TRANSFER|$from|${request.to}|${request.amount}|$timestamp"
        val payloadHash = MessageDigest
            .getInstance("SHA-256")
            .digest(payload.toByteArray(Charsets.UTF_8))

        val signature = signingKey.signHash(payloadHash)
        val rawSignature = ByteArray(65)
        to32Bytes(signature.r).copyInto(rawSignature, 0)
        to32Bytes(signature.s).copyInto(rawSignature, 32)
        rawSignature[64] = signature.v.toByte()

        return TransactionDto(
            id = UUID.randomUUID().toString(),
            type = "TRANSFER",
            from = from,
            to = request.to,
            amount = request.amount,
            timestamp = timestamp,
            publicKey = publicKey,
            signature = Base64.getEncoder().encodeToString(rawSignature)
        )
    }

    private fun to32Bytes(value: BigInteger): ByteArray {
        val bytes = value.toByteArray()

        if (bytes.size == 32) {
            return bytes
        }

        if (bytes.size == 33 && bytes[0] == 0.toByte()) {
            return bytes.copyOfRange(1, 33)
        }

        require(bytes.size < 32) { "Signature component too large" }

        val result = ByteArray(32)
        bytes.copyInto(result, destinationOffset = 32 - bytes.size)
        return result
    }
}

