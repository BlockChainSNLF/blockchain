module Mempool.Mempool where

import Data.List (sortBy)
import Data.Ord (comparing, Down (..))
import Types.Mempool (Mempool (..))
import Types.Transaction (Transaction, tip)
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