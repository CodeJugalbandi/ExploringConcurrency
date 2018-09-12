import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.Awaitable
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Success,Failure}
import scala.concurrent.ExecutionContext.Implicits.global

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

def pythagoreansParallel(n: Int) = {
  // Have at least as many computational chunks as the number of cores
  val numberOfCores = Runtime.getRuntime().availableProcessors()
  println(s"number of cores = ${numberOfCores}, input = ${n}")
  val possibleChunks = if (n <= numberOfCores) 1 else numberOfCores
  val evenlyDistributedChunkSize = Math.ceil(n.toFloat / possibleChunks).toInt
  val numberOfChunks = Math.ceil(n.toFloat / evenlyDistributedChunkSize).toInt
  println(s"number of chunks = ${numberOfChunks}, evenlyDistributedChunkSize = ${evenlyDistributedChunkSize}")
  // Having more chunks is better than having few, because more chunks help keep the cores busy
  // Also, after a certain number of large chunks, having more chunks has little benefit
  val futures = (2 to numberOfChunks)
    .scanLeft((1, evenlyDistributedChunkSize)) { (acc, elem) =>
      val (start, end) = acc
      val newStart = end + 1
      val newEnd = newStart + evenlyDistributedChunkSize
      if (elem == numberOfChunks || newEnd >= n) 
        (newStart, n) 
      else 
        (newStart, newEnd)
    }
    .filter { case (start, end) => start <= n }
    .map { case (start, end) =>
	    // Having more threads than the number of cores does not help
      // println((start, end))
      Future { pythagoreans(start, end).toList }
    }
  Future.foldLeft(futures) (List[(Int, Int, Int)]()) { case (acc, list) =>
    acc ++ list
  }
}

def time[T](f: Future[T]) = {
  val startTime = System.currentTimeMillis  
  val result = Await.result(f, 15 seconds)
  val diff = System.currentTimeMillis - startTime
  println(s"Time Taken = $diff(ms).")
  result
} 

// Parallelization using Futures (Results averaged over 5 runs)
// (with interchanged sides (3,4,5) and (4,3,5))
// ============================================================
println(time(pythagoreansParallel(10)))   // 31.8ms
// println(time(pythagoreansParallel(100)))  // 108.8ms
// println(time(pythagoreansParallel(250)))  // 323.2ms
// println(time(pythagoreansParallel(539)))  // 1345ms
// println(time(pythagoreansParallel(750)))  // 1095.8ms
// println(time(pythagoreansParallel(1000))) // 1890.6ms
// println(time(pythagoreansParallel(1250))) // 1422.5ms
// println(time(pythagoreansParallel(1500))) // 2194ms

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