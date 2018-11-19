 DoBlock←{
⍝ Sequential without interchanged sides
     (n start size)←⍵
     r←⌽i←(start-1)+⍳size   ⍝ r is reversed count
     ∘∘∘ ⍝ This is WIP - not working yet
     m←(2÷⍨⍵×⍵+1)⍴1  ⍝ +/⍳n ←→ (n(n+1))/2
     m[1++\t]←2-t←¯1↓r ⍝ +\m to get y
     xy←(r/i),⍪+\m   ⍝ x,⍪y

     ((⌊=⊢)0.5*⍨+/2*⍨xy)⌿xy ⍝ select where z is integer
⍝ v18.0 : ((⌊=⊢)(+/⍢*∘2)xy)⌿xy
 }
