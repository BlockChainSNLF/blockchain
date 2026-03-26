{-# LANGUAGE DeriveGeneric #-}

module Types.Block where

import GHC.Generics (Generic)
import Data.Aeson (ToJSON, FromJSON)

import Types.PreBlock

data Block = Block
  { blockContent :: PreBlock
  , hashValue :: String
  } deriving (Show, Eq, Generic)

instance ToJSON Block
instance FromJSON Block

createBlock :: PreBlock -> String -> Block
createBlock pb hashVal =
  Block
    { blockContent = pb
    , hashValue = hashVal
    }