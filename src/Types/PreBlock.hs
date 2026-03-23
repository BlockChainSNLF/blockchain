module Types.PreBlock where

import Types.Transaction

data PreBlock = PreBlock
  { index :: Int,
    timestamp :: Int,
    transactions :: [Transaction],
    previousHash :: String,
    nonce :: Int
  }
  deriving (Show, Eq)