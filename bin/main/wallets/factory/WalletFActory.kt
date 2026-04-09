package wallets.factory

import wallets.Wallet


interface WalletFActory {
    fun createWallet(): Wallet
}