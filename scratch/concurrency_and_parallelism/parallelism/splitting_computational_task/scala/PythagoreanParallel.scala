import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.Awaitable
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Success,Failure}
import scala.concurrent.ExecutionContext.Implicits.global

def pythagoreans(start:Int, end: Int) = 
  for {
    x <- start to end
    y <- start to end
    z <- start to end
    if (x*x + y*y == z*z)
  } yield (x,y,z)

def pythagoreansParallel(n: Int) = {
  // We need to have at least as many computational parts as the number of cores
  val numberOfChunks: Int = Runtime.getRuntime().availableProcessors()
  val evenlyDistributedChunkSize: Int = Math.floor(n.toFloat / numberOfChunks).toInt
  // println(s"number of chunks = ${numberOfChunks}, evenlyDistributedChunkSize = ${evenlyDistributedChunkSize}")
  val leftOvers: Int = n - evenlyDistributedChunkSize * numberOfChunks
  // println(s"Left overs = ${leftOvers}")
  // Having more chunks is better than having few, because more chunks help keep the cores busy
  // After a certain number of large chunks, having more chunks has little benefit
  val futures = (2 to numberOfChunks).scanLeft((1, evenlyDistributedChunkSize)) { (acc, elem) =>
    val (start, end) = acc
    val newStart = end + 1
    val newEnd = newStart + evenlyDistributedChunkSize
    if (elem == numberOfChunks) 
      (newStart, n) 
    else 
      (newStart, newEnd)
  } map { case (start, end) =>
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
  val result = Await.result(f, 10 seconds)
  val diff = System.currentTimeMillis - startTime
  println(s"Time Taken = $diff(ms).")
  result
} 

// Parallelization using Futures (Results averaged over 5 runs)
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
// ======================================================
// println(time(pythagoreans)(10))     // 11.6ms
// println(time(pythagoreans)(100))    // 88ms
// println(time(pythagoreans)(250))    // 275.5ms
// println(time(pythagoreans)(500))    // 1753.2ms
// println(time(pythagoreans)(750))    // 5127ms
// println(time(pythagoreans)(1000))   // 13506.6ms
// println(time(pythagoreans)(1250))   // 18704ms
// println(time(pythagoreans)(1500))   // 39720.4ms
