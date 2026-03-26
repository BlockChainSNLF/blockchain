{-# LANGUAGE DeriveGeneric #-}

module Types.Block where

import GHC.Generics (Generic)
import Data.Aeson (ToJSON, FromJSON)
import Hashing.Hash (hashString)
import Hashing.Serialization (serializePreBlock)
import Types.PreBlock

data Block = Block
  { blockContent :: PreBlock
  , hashValue :: String
  } deriving (Show, Eq, Generic)

instance ToJSON Block
instance FromJSON Block

createBlock :: PreBlock -> Block
createBlock pb =
  Block
    { blockContent = pb
    , hashValue = hashString (serializePreBlock pb)
    }