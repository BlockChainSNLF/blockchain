package validators.transactions

import transactions.*
import validators.signature.SignatureValidator
import wallets.address.AddressDerivator
import wallets.balanceService.BalanceService

import java.util.UUID

class TransactionValidator(
    private val signatureValidator: SignatureValidator,
    private val addressDerivator: AddressDerivator,
    private val balanceService: BalanceService
) {
    fun validateTransaction(transaction: Transaction): TransactionValidationResult {
        return when (transaction) {
            is TransferTransaction -> validateTransfer(transaction)
            is CoinbaseTransaction -> validateCoinbase(transaction)
        }
    }

    private fun validateCoinbase(transaction: CoinbaseTransaction): TransactionValidationResult {
        return Invalid(listOf("Coinbase Not Supported yet, Must be transfer only"))
    }

    private fun validateTransfer(transaction: TransferTransaction): TransactionValidationResult {
        val errors = mutableListOf<String>()

        validateStructure(transaction, errors)

        if (errors.isNotEmpty()) {
            return Invalid(errors)
        }

        validateCryptography(transaction, errors)
        validateState(transaction, errors)

        return if (errors.isEmpty()) Valid else Invalid(errors)
    }

    private fun validateStructure(
            transaction: TransferTransaction,
            errors: MutableList<String>
    ) {
        if (!isValidUuid(transaction.getId())) {
            errors.add("id must be a valid UUID")
        }

        if (transaction.getFrom().isBlank()) {
            errors.add("from is required")
        }else if (!isValidAddressHex(transaction.getFrom())){
            errors.add("from is invalid")
        }

        if (transaction.getTo().isBlank()) {
            errors.add("to is required")
        }else if (!isValidAddressHex(transaction.getFrom())) {
            errors.add("from to invalid")
        }

        if (transaction.getFrom() == transaction.getTo()) {
            errors.add("from and to must be different")
        }

        if (transaction.getAmount() <= 0) {
            errors.add("amount must be greater than 0")
        }

        if (transaction.getTimestamp() <= 0) {
            errors.add("timestamp must be greater than 0")
        }

        if (transaction.getPublicKey().isBlank()) {
            errors.add("publicKey is required")
        }else if (!isValidPublicKeyHex(transaction.getPublicKey())) {
            errors.add("publicKey is Invalid")
        }

        if (transaction.getSignature().isBlank()) {
            errors.add("signature is required")
        }
    }

    private fun validateCryptography(
            transaction: TransferTransaction,
            errors: MutableList<String>
    ) {
        val derivedAddress = addressDerivator.deriveAddress(transaction.getPublicKey())

        if (derivedAddress != transaction.getFrom()) {
            errors.add("from must match the address derived from publicKey")
        }

        val canonicalPayload =
                "TRANSFER|${transaction.getFrom()}|${transaction.getTo()}|${transaction.getAmount()}|${transaction.getTimestamp()}"

        if (
                !signatureValidator.isValid(
                        publicKey = transaction.getPublicKey(),
                        payload = canonicalPayload,
                        signature = transaction.getSignature()
                )
        ) {
            errors.add("Signature Invalid")
        }
    }

    private fun validateState(
            transaction: TransferTransaction,
            errors: MutableList<String>
    ) {
        //if (!balanceService.hasSufficientBalance(transaction.getFrom(), transaction.getAmount())) {
        //    errors.add("sender has insufficient balance")
        //}
    }

    private fun isValidUuid(value: String): Boolean {
        return try {
            UUID.fromString(value)
            true
        } catch (_: IllegalArgumentException) {
            false
        }
    }
    private fun isValidPublicKeyHex(publicKey: String): Boolean {
        val clean = publicKey.removePrefix("0x")

        if (clean.length != 130) return false
        if (!clean.matches(Regex("[0-9a-fA-F]+"))) return false
        if (!clean.startsWith("04")) return false

        return true
    }
    private fun isValidAddressHex(address: String): Boolean {
        val clean = address.removePrefix("0x")

        if (clean.length != 40) return false
        if (!clean.matches(Regex("[0-9a-fA-F]+"))) return false

        return true
    }
}