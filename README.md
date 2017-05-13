# COMP
AST:
Module
 Id : Test	
 DeclarationList
  Assign : =
   Id : a
   Integer : 2
  Id : c
 Function
  Return
   Id : a
   Id : main
  Parameters
   Id : N
   Array : B
  Assign : =
   Id : a
   Integer : 3
  Assign : =
   Id : i
   Integer : 0
  Assign : =
   Id : b
   ArraySize
    Integer : 20
  Assign : =
   Array
    Id : b
    ArrayAccess : []
   Integer : 10
  If
   Exprtest : >
    Id : N
    Integer : 30
   Assign : =
    Id : N
    Integer : 30
   Else
    Assign : =
     Id : N
     Integer : 15
  While
   Exprtest : <
    Id : i
    Id : b
   Assign : =
    Id : a
    Arith : *
     Id : N
     Array
      Id : b
      ArrayAccess : [i]
   Assign : =
    Id : i
    Arith : +
     Id : i
     Integer : 1
  Assign : =
   Id : a
   Call
    Id : io
    Id : test
    ArgumentList
     Integer : 8
     Integer : 10
