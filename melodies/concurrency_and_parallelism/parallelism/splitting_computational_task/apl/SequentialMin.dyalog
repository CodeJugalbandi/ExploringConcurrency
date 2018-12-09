 SequentialMin←{
⍝ Sequential without "unnecessary" computations
⍝  (only compute from x to 2*⍨n-x)
     ∘∘∘ ⍝ Idea - not working yet
     r←⌽i←⍳⍵         ⍝ r is reversed count
     m←(2÷⍨⍵×⍵+1)⍴1  ⍝ +/⍳n ←→ (n(n+1))/2
     m[1++\t]←2-t←¯1↓r ⍝ +\m to get y
     xy←(r/i),⍪+\m   ⍝ x,⍪y

     ((⌊=⊢)0.5*⍨+/2*⍨xy)⌿xy
⍝ v18.0 : ((⌊=⊢)(+/⍢*∘2)xy)⌿xy
 }
