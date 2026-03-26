module Consensus.Consensus where

import Types.Chain (Chain (..))
import Validations.ChainValidation (isValidChain)

data ConsensusResult
  = KeepMine
  | AdoptPeer Chain
  deriving (Show)

shouldReplace :: Int -> Chain -> Chain -> Bool
shouldReplace difficulty (Chain my) (Chain candidate) =
  isValidChain difficulty (Chain candidate) && length candidate > length my

consensusWithOne :: Int -> Chain -> Chain -> ConsensusResult
consensusWithOne difficulty myChain peerChain
  | shouldReplace difficulty myChain peerChain = AdoptPeer peerChain
  | otherwise = KeepMine