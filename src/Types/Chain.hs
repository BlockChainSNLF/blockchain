module Types.Chain where

import Types.Block

newtype Chain = Chain [Block]
    deriving (Show, Eq)

