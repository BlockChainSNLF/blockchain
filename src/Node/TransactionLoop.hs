module Node.TransactionLoop where

import Control.Concurrent
import Node.NodeState

transactionLoop :: MVar NodeState -> IO ()
transactionLoop stateVar = do
  putStrLn "Transaction loop started"
  threadDelay 1000000
  transactionLoop stateVar