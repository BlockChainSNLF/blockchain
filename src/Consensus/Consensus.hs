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
  | AdoptPeer Chain
  deriving (Show)

shouldReplace :: Int -> Chain -> Chain -> Bool
shouldReplace difficulty (Chain my) (Chain candidate) =
  isValidChain difficulty (Chain candidate) && length candidate > length my

consensusWithOne :: Int -> Chain -> Chain -> ConsensusResult
consensusWithOne difficulty myChain peerChain
  | shouldReplace difficulty myChain peerChain = AdoptPeer peerChain
  | otherwise = KeepMine