module Consensus.Consensus where

import Types.Chain (Chain (..))
import Validations.ChainValidation (isValidChain)

data ConsensusResult
  = KeepMine
  | AdoptPeer Chain
  deriving (Show)

shouldReplace :: Chain -> Chain -> Bool
shouldReplace (Chain my) (Chain candidate) =
  isValidChain (Chain candidate) && length candidate > length my

consensusWithOne :: Chain -> Chain -> ConsensusResult
consensusWithOne myChain peerChain
  | shouldReplace myChain peerChain = AdoptPeer peerChain
  | otherwise = KeepMine