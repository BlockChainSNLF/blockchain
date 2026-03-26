module Mempool.Mempool where

import Types.Transaction
import Types.Mempool
import Data.List (sortBy)

addTransaction :: Transaction -> Mempool -> Mempool
addTransaction tx (Mempool txs) = Mempool (txs ++ [tx])

getTransactions :: Mempool -> [Transaction]
getTransactions (Mempool txs) = txs

removeTransactions :: [Transaction] -> Mempool -> Mempool
removeTransactions mined (Mempool txs) =
  Mempool (filter (`notElem` mined) txs)

reorderByTip :: Mempool -> Mempool
reorderByTip (Mempool txs) =
  Mempool (sortBy (comparing (Down . tip)) txs)