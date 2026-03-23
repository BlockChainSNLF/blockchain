module Main where

import Blockchain.Genesis (genesisBlock)

main :: IO ()
main = print genesisBlock
