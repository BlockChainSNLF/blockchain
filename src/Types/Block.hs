module Types.Block where

import Types.PreBlock

data Block = Block
  { blockContent :: PreBlock,
    hashValue :: String
  }
  deriving (Show, Eq)

createBlock :: PreBlock -> String -> Block
createBlock pb hash =
  Block
    { blockContent = pb,
      hashValue = hash
    }