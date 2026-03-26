module Mempool.Mempool where

import Data.List (sortBy)
import Data.Ord (comparing, Down (..))
import Types.Mempool (Mempool (..))
import Types.Transaction (Transaction, tip)

addTransaction :: Transaction -> Mempool -> Mempool
addTransaction tx (Mempool txs) = Mempool (txs ++ [tx])

getTransactions :: Mempool -> Int -> [Transaction]
getTransactions (Mempool txs) n = take n txs

removeTransactions :: [Transaction] -> Mempool -> Mempool
removeTransactions mined (Mempool txs) =
  Mempool (filter (`notElem` mined) txs)


reorderTransactions :: Mempool -> Mempool
reorderTransactions (Mempool txs) =
  Mempool (sortBy compareByTip txs)

compareByTip :: Transaction -> Transaction -> Ordering
compareByTip tx1 tx2 = compare (tip tx2) (tip tx1)

initializeMempool :: Mempool
initializeMempool = Mempool []

reorderByTip :: Mempool -> Mempool
reorderByTip (Mempool txs) =
  Mempool (sortBy (comparing (Down . tip)) txs)

