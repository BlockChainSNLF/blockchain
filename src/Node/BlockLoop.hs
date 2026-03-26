module Node.BlockLoop where

import Control.Concurrent
import Node.NodeState

blockLoop :: MVar NodeState -> IO ()
blockLoop stateVar = do
  putStrLn "Block loop started"
  threadDelay 1000000
  blockLoop stateVar