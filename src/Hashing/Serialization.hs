module Hashing.Serialization where

import Data.List (intercalate)
import Types.PreBlock
import Types.Transaction

serializePreBlock :: PreBlock -> String
serializePreBlock pb =
  show (index pb)
    ++ "#"
    ++ show (timestamp pb)
    ++ "#"
    ++ serializeTransactions (transactions pb)
    ++ "#"
    ++ previousHash pb
    ++ "#"
    ++ show (nonce pb)

serializeTransactions :: [Transaction] -> String
serializeTransactions txs =
  intercalate ";" (map serializeTransaction txs)

serializeTransaction :: Transaction -> String
serializeTransaction tx =
  from tx
    ++ "|"
    ++ to tx
    ++ "|"
    ++ show (amount tx)
    ++ "|"
    ++ sig tx