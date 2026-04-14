package wallets.balanceService

import block.Block
import transactions.CoinbaseTransaction
import transactions.TransferTransaction
import transactions.Transaction

object BalanceService {
    private val balances: MutableMap<String, Int> = linkedMapOf()

    fun reset() {
        balances.clear()
    }

    fun rebuildFromChain(chain: List<Block>) {
        reset()
        chain.forEach { applyBlock(it) }
    }

    fun applyBlock(block: Block) {
        block.getTransactions().forEach { applyTransaction(it) }
    }

    fun hasSufficientBalance(address: String, amount: Int): Boolean {
        return getBalance(address) >= amount
    }

    fun getBalance(address: String): Int {
        return balances[address] ?: 0
    }

    private fun applyTransaction(transaction: Transaction) {
        when (transaction) {
            is CoinbaseTransaction -> credit(transaction.getTo(), transaction.getAmount())
            is TransferTransaction -> {
                debit(transaction.getFrom(), transaction.getAmount())
                credit(transaction.getTo(), transaction.getAmount())
            }
        }
    }

    private fun credit(address: String, amount: Int) {
        balances[address] = getBalance(address) + amount
    }

    private fun debit(address: String, amount: Int) {
        balances[address] = getBalance(address) - amount
    }
}