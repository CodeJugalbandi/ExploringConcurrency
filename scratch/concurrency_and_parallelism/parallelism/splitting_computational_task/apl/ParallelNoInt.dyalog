 ParallelNoInt←{
⍝ Distribute Pythagoras calculation to ⍺ processors

     ⍺←1111⌶⍬ ⍝ Default to # of processors
     Under←{⍵⍵⍣¯1 ⍺⍺ ⍵⍵ ⍵} ⍝ Will be primitive ⍢, possibly in Dyalog 18.0
     blksize←numprocs{2-/⌊0.5+⌽(⍺÷⍨0,⍳⍺)∘×Under(⊢×1∘+)⍵}⍵
     parts←⍵ DoBlock IÏ(+\¯1↓0,blksize),¨blksize
     ⊃⍪/parts ⍝ create a single 2-column matrix
 }
