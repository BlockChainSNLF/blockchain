module Node.MiningState where

import Types.PreBlock

data MiningState = MiningState
  { candidateBlock :: PreBlock,
    currentNonce :: Int,
    startedAtEpoch :: Int
  }
  deriving (Show, Eq)