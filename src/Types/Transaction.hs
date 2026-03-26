{-# LANGUAGE DeriveGeneric #-}

module Types.Transaction where

import GHC.Generics (Generic)
import Data.Aeson (ToJSON, FromJSON)

data Transaction = Transaction{
  from :: String,
  to :: String,
  amount :: Int,
  sig :: String,
  tip :: Int
}deriving (Show, Eq, Generic)

instance ToJSON Transaction
instance FromJSON Transaction