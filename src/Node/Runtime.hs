module Node.Runtime where

import Control.Concurrent
import Control.Concurrent.STM
import Mempool.Mempool
import Node.Channels
import Node.NodeState
import Storage.Storage


initializeNodeState :: NodeState
initializeNodeState =
  NodeState
    { nodeChain = initializeStorage,
      nodeMempool = initializeMempool,
      miningDifficulty = 0
    }

data NodeRuntime = NodeRuntime
  { runtimeStateVar :: MVar NodeState,
    runtimeInterruptVar :: TVar Bool,
    runtimeChannels :: NodeChannels
  }

initializeRuntime :: IO NodeRuntime
initializeRuntime = do
  stateVar <- newMVar initializeNodeState
  interruptVar <- newTVarIO False
  channels <- initializeChannels

  return
    NodeRuntime
      { runtimeStateVar = stateVar,
        runtimeInterruptVar = interruptVar,
        runtimeChannels = channels
      }