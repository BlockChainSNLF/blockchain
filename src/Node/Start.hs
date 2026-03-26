module Node.Start where

import Control.Concurrent (forkIO, threadDelay)
import Node.BlockLoop
import Node.MiningLoop
import Node.Runtime
import Node.TransactionLoop

startNode :: IO ()
startNode = do
  runtime <- initializeRuntime
  let stateVar = runtimeStateVar runtime
  let interruptVar = runtimeInterruptVar runtime
  let channels = runtimeChannels runtime

  _ <- forkIO (transactionLoop stateVar channels)
  _ <- forkIO (blockLoop stateVar interruptVar channels)
  _ <- forkIO (miningLoop stateVar interruptVar channels)

  putStrLn "Node started"
  keepAlive
  where
    keepAlive = threadDelay 1000000 >> keepAlive