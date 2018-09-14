// with interchanged sides (3,4,5) and (4,3,5)
// def pythagoreans(start:Int, end: Int) = for {
//   x <- 1 to end
//   y <- 1 to end
//   z <- start to end
//   if (x*x + y*y == z*z)
// } yield (x,y,z)

// without interchanged sides (3,4,5) and (4,3,5), only (3,4,5)
def pythagoreans(start:Int, end: Int) = for {
  x <- 1 to end
  y <- x to end
  z <- start to end
  if (x*x + y*y == z*z)
} yield (x,y,z)


def pythagoreansParallel2(n: Int) = {
  // Have at least as many threads as the number of cores
  val numberOfCores = Runtime.getRuntime().availableProcessors()
  // println(s"number of cores = ${numberOfCores}, input = ${n}")
  // Having more threads than the number of cores does not help.  
  // As this is an evenly distributed problem, we will create 
  // number of chunks == number of cores.
  val possibleChunks = if (n <= numberOfCores) 1 else numberOfCores
  val evenlyDistributedChunkSize = Math.ceil(n.toFloat / possibleChunks).toInt
  val numberOfChunks = Math.ceil(n.toFloat / evenlyDistributedChunkSize).toInt
  // println(s"number of chunks = ${numberOfChunks}, evenlyDistributedChunkSize = ${evenlyDistributedChunkSize}")
  // Having more chunks is better than having few, because more chunks help keep the cores busy
  // Also, after a certain number of large chunks, having more chunks have little benefit
  (2 to numberOfChunks).scanLeft((1, evenlyDistributedChunkSize)) { (acc, elem) =>
    val (start, end) = acc
    val newStart = end + 1
    val newEnd = newStart + evenlyDistributedChunkSize
    if (elem == numberOfChunks || newEnd >= n) 
      (newStart, n) 
    else 
      (newStart, newEnd)
  }
  .filter { case (start, end) => start <= n }
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

//--------------------< SET I >---------------------------
// Parallelization using Parallel Lists (Results averaged over 5 runs)
// (without interchanged sides (3,4,5) and (4,3,5), only (3,4,5))
// ===================================================================
// println(time(pythagoreansParallel2)(10))   
// println(time(pythagoreansParallel2)(100))  
// println(time(pythagoreansParallel2)(250))  
// println(time(pythagoreansParallel2)(500))  
// println(time(pythagoreansParallel2)(750))  
// println(time(pythagoreansParallel2)(1000)) 
// println(time(pythagoreansParallel2)(1250)) 
// println(time(pythagoreansParallel2)(1500)) 

// Parallelization (Results averaged over 5 runs)
// (without interchanged sides (3,4,5) and (4,3,5), only (3,4,5))
//==================================================================================+
//                                               |  Using Futures | Using Par List  |
//==================================================================================+
// println(time(pythagoreansParallel(10)))   //  |    37.2ms      |     68.6ms      |
// println(time(pythagoreansParallel(100)))  //  |    112ms       |     143.8ms     |
// println(time(pythagoreansParallel(250)))  //  |    274.4ms     |     366.6ms     |
// println(time(pythagoreansParallel(500)))  //  |    635.6ms     |     759ms       |
// println(time(pythagoreansParallel(750)))  //  |    1234.2ms    |     759ms       |
// println(time(pythagoreansParallel(1000))) //  |    1766ms      |     1924.8ms    |
// println(time(pythagoreansParallel(1250))) //  |    2121.4ms    |     2649.4ms    |
// println(time(pythagoreansParallel(1500))) //  |    3655.6ms    |     3815.2ms    |
//==================================================================================+

// Sequential Pythagoreans (Results averaged over 5 runs)
// (without interchanged sides (3,4,5) and (4,3,5), only (3,4,5))
// ======================================================
// println(time(pythagoreans)(10))     // 10.4ms
// println(time(pythagoreans)(100))    // 44.8ms
// println(time(pythagoreans)(250))    // 183ms
// println(time(pythagoreans)(500))    // 469ms
// println(time(pythagoreans)(750))    // 1332.8ms
// println(time(pythagoreans)(1000))   // 2649.6ms
// println(time(pythagoreans)(1250))   // 4917.8ms
// println(time(pythagoreans)(1500))   // 8180.2ms


// Parallelization (Results averaged over 5 runs)
// (with interchanged sides (3,4,5) and (4,3,5))
//==================================================================================+
//                                               |  Using Futures | Using Par List  |
//==================================================================================+
// println(time(pythagoreansParallel(10)))   //  |    31.8ms      |   65.6ms        |
// println(time(pythagoreansParallel(100)))  //  |    108.8ms     |   119ms         |
// println(time(pythagoreansParallel(250)))  //  |    323.2ms     |   246.4ms       |
// println(time(pythagoreansParallel(539)))  //  |    1345ms      |   1471ms        |
// println(time(pythagoreansParallel(750)))  //  |    1095.8ms    |   1307.4ms      |
// println(time(pythagoreansParallel(1000))) //  |    1890.6ms    |   2128.6ms      |
// println(time(pythagoreansParallel(1250))) //  |    1422.5ms    |   1711.6ms      |
// println(time(pythagoreansParallel(1500))) //  |    2194ms      |   2389.2ms      |
//==================================================================================+

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

println("DONE")