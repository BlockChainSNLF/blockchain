package signing

import network.dto.TransactionDto

interface TransactionSigner {
    fun signTransfer(request: SignTransferRequest): TransactionDto
}

