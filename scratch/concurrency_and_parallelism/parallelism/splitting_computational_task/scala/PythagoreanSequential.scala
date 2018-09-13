// with interchanged sides (3,4,5) and (4,3,5)
// def pythagoreans(start:Int, end: Int) = for {
//   x <- 1 to end
//   y <- 1 to end
//   z <- 1 to end
//   if (x*x + y*y == z*z)
// } yield (x,y,z)

// without interchanged sides (3,4,5) and (4,3,5), only (3,4,5)
def pythagoreans(n: Int) = for {
  x <- 1 to n
  y <- x to n
  z <- y to n
  if (x*x + y*y == z*z)
} yield (x,y,z)

def time[T, R](f: Function[T, R]): Function[T, R] = {
  return t => {
    val startTime = System.currentTimeMillis  
    val result = f(t)
    val diff = System.currentTimeMillis - startTime
    println(s"Time Taken = $diff(ms).")
    result
  }
} 

//--------------------< SET I >---------------------------
// Sequential Pythagoreans (Results averaged over 5 runs)
// (with interchanged sides (3,4,5) and (4,3,5))
// ======================================================
// println(time(pythagoreans)(10))     // 11.6ms
// println(time(pythagoreans)(100))    // 88ms
// println(time(pythagoreans)(250))    // 275.5ms
// println(time(pythagoreans)(500))    // 1753.2ms
// println(time(pythagoreans)(750))    // 5127ms
// println(time(pythagoreans)(1000))   // 13506.6ms
// println(time(pythagoreans)(1250))   // 18704ms
// println(time(pythagoreans)(1500))   // 39720.4ms

//--------------------< SET II >---------------------------
// Sequential Pythagoreans (Results averaged over 5 runs)
// (without interchanged sides (3,4,5) and (4,3,5), only (3,4,5))
// ======================================================
println(time(pythagoreans)(10))     // 10.4ms
// println(time(pythagoreans)(100))    // 44.8ms
// println(time(pythagoreans)(250))    // 183ms
// println(time(pythagoreans)(500))    // 469ms
// println(time(pythagoreans)(750))    // 1332.8ms
// println(time(pythagoreans)(1000))   // 2649.6ms
// println(time(pythagoreans)(1250))   // 4917.8ms
// println(time(pythagoreans)(1500))   // 8180.2ms
println("DONE")