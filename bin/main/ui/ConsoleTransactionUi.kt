package ui

import signing.SignTransferRequest
import signing.TransactionSigner

class ConsoleTransactionUi(
    private val signer: TransactionSigner,
    private val submissionGateway: TransactionSubmissionGateway
) {
    fun startBlocking() {
        runLoop()
    }

    fun start() {
        val thread = Thread({ runLoop() }, "transaction-ui")
        thread.isDaemon = true
        thread.start()
    }

    private fun runLoop() {
        println("[tx-ui] ready. type 'tx' to send transaction, 'help' to show commands, 'exit' to close ui")

        while (true) {
            print("[tx-ui] > ")
            val command = readLine()?.trim()?.lowercase() ?: run {
                println("[tx-ui] input stream closed, transaction ui stopped")
                break
            }

            when (command) {
                "tx" -> submitTransaction()
                "help" -> printHelp()
                "exit", "quit" -> return
                "" -> Unit
                else -> println("[tx-ui] unknown command: $command")
            }
        }
    }

    private fun printHelp() {
        println("[tx-ui] commands:")
        println("[tx-ui]   tx   -> create, sign and submit a transfer transaction")
        println("[tx-ui]   exit -> close transaction ui")
    }

    private fun submitTransaction() {
        print("[tx-ui] private key (hex): ")
        val privateKey = readLine()?.trim().orEmpty()

        print("[tx-ui] to address: ")
        val to = readLine()?.trim().orEmpty()

        print("[tx-ui] amount: ")
        val amountInput = readLine()?.trim().orEmpty()
        val amount = amountInput.toIntOrNull()

        print("[tx-ui] timestamp ms (optional, enter for now): ")
        val timestampInput = readLine()?.trim().orEmpty()
        val timestamp = timestampInput.toLongOrNull()

        if (privateKey.isBlank() || to.isBlank() || amount == null || amount <= 0) {
            println("[tx-ui] invalid input. private key, to and positive amount are required")
            return
        }

        val request = SignTransferRequest(
            privateKey = privateKey,
            to = to,
            amount = amount,
            timestamp = timestamp
        )

        val signed = runCatching { signer.signTransfer(request) }
            .onFailure { error -> println("[tx-ui] failed to sign transaction: ${error.message}") }
            .getOrNull()
            ?: return

        val result = submissionGateway.submit(signed)
        println("[tx-ui] ${result.message}")
    }
}


