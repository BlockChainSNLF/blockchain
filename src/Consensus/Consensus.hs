module Consensus.Consensus where

import Data.List (maximumBy)
import Data.Ord (comparing)
import Types.Block (Block)
import Validations.ChainValidation (isValidChain)
import Types.Chain (Chain(..))
import Storage.Storage (getChain)

resolveChain :: Chain -> [Chain] -> Chain
resolveChain myChain peerChains =
  let allChains = myChain : peerChains
      validChains = filter (isValidChain . getChain) allChains
   in case validChains of
        [] -> myChain
        _ -> maximumBy (comparing (length . getChain)) validChains

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