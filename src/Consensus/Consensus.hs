module Consensus.Consensus where

import Data.List (maximumBy)
import Data.Ord (comparing)
import Types.Block (Block)
import Validations.ChainValidation (isValidChain)

resolveChain :: [Block] -> [[Block]] -> [Block]
resolveChain myChain peerChains =
  let allChains = myChain : peerChains
      validChains = filter isValidChain allChains
   in case validChains of
        [] -> myChain
        _ -> maximumBy (comparing length) validChains

shouldReplace :: [Block] -> [Block] -> Bool
shouldReplace myChain candidate =
  isValidChain candidate && length candidate > length myChain

data ConsensusResult
  = KeepMine
  | AdoptPeer [Block]
  deriving (Show)

consensusWithOne :: [Block] -> [Block] -> ConsensusResult
consensusWithOne myChain peerChain
  | shouldReplace myChain peerChain = AdoptPeer peerChain
  | otherwise = KeepMine