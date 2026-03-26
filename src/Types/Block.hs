{-# LANGUAGE DeriveGeneric #-}

module Types.Block where

import Data.Aeson (FromJSON, ToJSON)
import GHC.Generics (Generic)
import Types.PreBlock

data Block = Block
  { blockContent :: PreBlock,
    hashValue :: String
  }
  deriving (Show, Eq, Generic)

instance ToJSON Block

instance FromJSON Block

createBlock :: PreBlock -> String -> Block
createBlock pb hash =
  Block
    { blockContent = pb,
      hashValue = hash
    }