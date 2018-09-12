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

// Sequential Pythagoreans (Results averaged over 5 runs)
// ======================================================
println(time(pythagoreans)(10))     // 11.6ms
// println(time(pythagoreans)(100))    // 88ms
// println(time(pythagoreans)(250))    // 275.5ms
// println(time(pythagoreans)(500))    // 1753.2ms
// println(time(pythagoreans)(750))    // 5127ms
// println(time(pythagoreans)(1000))   // 13506.6ms
// println(time(pythagoreans)(1250))   // 18704ms
// println(time(pythagoreans)(1500))   // 39720.4ms
