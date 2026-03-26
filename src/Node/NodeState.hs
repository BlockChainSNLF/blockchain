module Node.NodeState where

import Types.Chain
import Types.Mempool

data NodeState = NodeState
  { nodeChain :: Chain,
    nodeMempool :: Mempool,
    miningDifficulty :: Int
  }
  deriving (Show, Eq)