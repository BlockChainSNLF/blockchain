module Mempool.Mempool where
  
import Types.Transaction
import Types.Mempool
import Data.List (sortBy)



addTransaction :: Transaction -> Mempool -> Mempool
addTransaction tx (Mempool txs) = Mempool (txs ++ [tx])

getTransactions :: Mempool -> Int -> [Transaction]
getTransactions (Mempool txs) n = take n txs

removeTransactions :: [Transaction] -> Mempool -> Mempool
removeTransactions txs (Mempool mempoolTxs) =
  Mempool (filter (`notElem` txs) mempoolTxs)

reorderTransactions :: Mempool -> Mempool
reorderTransactions (Mempool txs) =
  Mempool (sortBy compareByTip txs)

compareByTip :: Transaction -> Transaction -> Ordering
compareByTip tx1 tx2 = compare (tip tx2) (tip tx1)

initializeMempool :: Mempool
initializeMempool = Mempool []