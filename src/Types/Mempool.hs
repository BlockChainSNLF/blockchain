module Types.Mempool where

import Types.Transaction


newtype Mempool = Mempool [Transaction]
    deriving (Show, Eq)