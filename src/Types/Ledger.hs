module Types.Ledger
  ( Ledger
  , buildLedger
  , applyTx
  , isValidTx
  ) where

import qualified Data.Map as Map
import Data.Map (Map)

import Types.Chain (Chain(..))
import Types.Block
import Types.PreBlock
import Types.Transaction

type Ledger = Map String Int

buildLedger :: Chain -> Ledger
buildLedger (Chain blocks) =
  foldl applyBlock Map.empty blocks

applyBlock :: Ledger -> Block -> Ledger
applyBlock ledger block =
  foldl applyTx ledger (transactions (blockContent block))

applyTx :: Ledger -> Transaction -> Ledger
applyTx ledger tx =
  let fromBal = Map.findWithDefault 0 (from tx) ledger
      toBal   = Map.findWithDefault 0 (to tx) ledger
  in Map.insert (to tx) (toBal + amount tx) $
     Map.insert (from tx) (fromBal - amount tx) ledger

isValidTx :: Ledger -> Transaction -> Bool
isValidTx ledger tx =
  let balance = Map.findWithDefault 0 (from tx) ledger
  in balance >= amount tx && amount tx > 0