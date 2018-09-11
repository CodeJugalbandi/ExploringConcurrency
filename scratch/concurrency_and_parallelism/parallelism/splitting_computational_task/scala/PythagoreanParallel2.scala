def pythagoreans(start:Int, end: Int) = 
  for {
    x <- start to end
    y <- start to end
    z <- start to end
    if (x*x + y*y == z*z)
  } yield (x,y,z)

def pythagoreansParallel2(n: Int) = {
  val maxChunkSize: Int = 500
  val numberOfChunks: Int = Math.ceil(n.toFloat / maxChunkSize).toInt
  val evenlyDistributedChunkSize = n / numberOfChunks
  // println(s"number of chunks = ${numberOfChunks}, evenlyDistributedChunkSize = ${evenlyDistributedChunkSize}")
  val leftOvers: Int = n - evenlyDistributedChunkSize * numberOfChunks
  // println(s"Left overs = ${leftOvers}")
  (2 to numberOfChunks).scanLeft((1, evenlyDistributedChunkSize)) { (acc, elem) =>
    val (start, end) = acc
    val newStart = end + 1
    val newEnd = newStart + evenlyDistributedChunkSize
    if (elem == numberOfChunks) 
      (newStart, n) 
    else 
      (newStart, newEnd)
  }
  .par
  .map { case (start, end) => pythagoreans(start, end) }
  .foldLeft(List[(Int, Int, Int)]()) { case (acc, list) => acc ++ list }
}

def time[T, R](f: Function[T, R]): Function[T, R] = {
  return t => {
    val startTime = System.currentTimeMillis  
    val result = f(t)
    val diff = System.currentTimeMillis - startTime
    println(s"Time Taken = $diff(ms).")
    result
  }
} 

// Parallelization using Parallel Lists (Results averaged over 5 runs)
// ===================================================================
println(time(pythagoreansParallel2)(10))   // 65.6ms
// println(time(pythagoreansParallel2)(100))  // 119ms
// println(time(pythagoreansParallel2)(250))  // 246.4ms
// println(time(pythagoreansParallel2)(500))  // 1471ms
// println(time(pythagoreansParallel2)(750))  // 1307.4ms
// println(time(pythagoreansParallel2)(1000)) // 2128.6ms
// println(time(pythagoreansParallel2)(1250)) // 1711.6ms
// println(time(pythagoreansParallel2)(1500)) // 2389.2ms


// Parallelization using Futures (Results averaged over 5 runs)
// ============================================================
// println(time(pythagoreansParallel(10)))   // 31.8ms
// println(time(pythagoreansParallel(100)))  // 108.8ms
// println(time(pythagoreansParallel(250)))  // 323.2ms
// println(time(pythagoreansParallel(500)))  // 1345ms
// println(time(pythagoreansParallel(750)))  // 1095.8ms
// println(time(pythagoreansParallel(1000))) // 1890.6ms
// println(time(pythagoreansParallel(1250))) // 1422.5ms
// println(time(pythagoreansParallel(1500))) // 2194ms

// Sequential Pythagoreans (Results averaged over 5 runs)
// ======================================================
// println(time(pythagoreans)(10))     // 11.6ms
// println(time(pythagoreans)(100))    // 88ms
// println(time(pythagoreans)(250))    // 275.5ms
// println(time(pythagoreans)(500))    // 1753.2ms
// println(time(pythagoreans)(750))    // 5127ms
// println(time(pythagoreans)(1000))   // 13506.6ms
// println(time(pythagoreans)(1250))   // 18704ms
// println(time(pythagoreans)(1500))   // 39720.4ms
