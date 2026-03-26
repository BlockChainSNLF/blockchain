{-# LANGUAGE DeriveGeneric #-}

module Types.PreBlock where

import GHC.Generics (Generic)
import Data.Aeson (ToJSON, FromJSON)

import Types.Transaction

data PreBlock = PreBlock
  { index :: Int
  , timestamp :: Int
  , transactions :: [Transaction]
  , previousHash :: String
  , nonce :: Int
  } deriving (Show, Eq, Generic)

instance ToJSON PreBlock
instance FromJSON PreBlock