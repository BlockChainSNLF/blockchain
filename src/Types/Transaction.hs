module Types.Transaction where

data Transaction = Transaction{
  from :: String,
  to :: String,
  amount :: Int,
  sig :: String
}deriving (Show, Eq)