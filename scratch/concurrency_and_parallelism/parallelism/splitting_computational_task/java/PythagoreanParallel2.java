import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class PythagoreanParallel2 {
  
  static <R> Function<Integer, R> time(Function<Integer, R> f) {
    return t -> {
      long startTime = System.currentTimeMillis();
      R result = f.apply(t);
      long diff = System.currentTimeMillis() - startTime;
      System.out.println(String.format("Time Taken = %d(ms).", diff));
      return result;
    };
  }
  
  // with interchanged sides (3,4,5) and (4,3,5)
  static List<List<?>> pythagoreans(int start, int end) {
    List<List<?>> result = new ArrayList<>();
    for (int x = 1; x <= end; x++) {
      for (int y = 1; y <= end; y++) {
        for (int z = start; z <= end; z++) {
          if (x*x + y*y == z*z)
            result.add(Arrays.asList(x, y, z));
        }
      }
    }
    return result;
  }
  
  // without interchanged sides (3,4,5) and (4,3,5), only (3,4,5)
  // static List<List<?>> pythagoreans(int start, int end) {
  //   List<List<?>> result = new ArrayList<>();
  //   for (int x = 1; x <= end; x++) {
  //     for (int y = x; y <= end; y++) {
  //       for (int z = start; z <= end; z++) {
  //         if (x*x + y*y == z*z)
  //           result.add(Arrays.asList(x, y, z));
  //       }
  //     }
  //   }
  //   return result;
  // }
   
  static Stream<List<?>> makeChunks(int n) {
    // Have at least as many threads as the number of cores
    int numberOfCores = Runtime.getRuntime().availableProcessors();
    // System.out.println(String.format("number of cores = %d, input = %d", numberOfCores, n));
    // Having more threads than the number of cores does not help.
    // As this is an evenly distributed problem, we will create
    // number of chunks == number of cores.
    int possibleChunks = (n <= numberOfCores) ? 1 : numberOfCores;
    int evenlyDistributedChunkSize = (int) Math.ceil((float) n / possibleChunks);
    int numberOfChunks = (int) Math.ceil( (float) n / evenlyDistributedChunkSize);
    // System.out.println(String.format("number of chunks = %d, evenlyDistributedChunkSize = %d", numberOfChunks, evenlyDistributedChunkSize));
    // Having more chunks is better than having few, because more chunks help keep the cores busy
    // Also, after a certain number of large chunks, having more chunks have little benefit
    List<List<?>> chunks = new ArrayList<>();
    List<Integer> seed = List.of(1, evenlyDistributedChunkSize);
    for (int x = 2; x <= numberOfChunks; x++) {
      chunks.add(seed);
      int start = seed.get(0);
      int end = seed.get(1);
      int newStart = end + 1;
      int newEnd = newStart + evenlyDistributedChunkSize;
      if (x == numberOfChunks || newEnd >= n)
         seed = List.of(newStart, n);
      else
         seed = List.of(newStart, newEnd);
    }
    return chunks.stream().filter(chunk -> {
      int start = (int) chunk.get(0);
      int end = (int) chunk.get(1);
      return start <= n;
    });
  }
  
  static List<List<?>> pythagoreans(int n) {
    return makeChunks(n)
      .parallel()
      .flatMap(chunkBounds -> {
        int start = (int) chunkBounds.get(0);
        int end = (int) chunkBounds.get(1);
        return pythagoreans(start, end).stream();
      })
      .collect(Collectors.toList()); 
  }

  public static void main(String[] args) {
    System.out.println(time(PythagoreanParallel2::pythagoreans).apply(1500));
    System.out.println("DONE");
  }
}

//--------------------< SET I >---------------------------
// Parallelization (Results averaged over 5 runs)
// (without interchanged sides (3,4,5) and (4,3,5), only (3,4,5))
//=======================================================================+
//                                               | Using Parallel Stream |
//=======================================================================+
// println(time(pythagoreansParallel(10)))   //  |        21ms           |
// println(time(pythagoreansParallel(100)))  //  |        22.4ms         |
// println(time(pythagoreansParallel(250)))  //  |        47.2ms         |
// println(time(pythagoreansParallel(500)))  //  |        68.6ms         |
// println(time(pythagoreansParallel(750)))  //  |        112.6ms        |
// println(time(pythagoreansParallel(1000))) //  |        196.2ms        |
// println(time(pythagoreansParallel(1250))) //  |        322.8ms        |
// println(time(pythagoreansParallel(1500))) //  |        500.4ms        |
//=======================================================================+

// Sequential Pythagoreans (Results averaged over 5 runs)
// (without interchanged sides (3,4,5) and (4,3,5), only (3,4,5))
// ======================================================
// println(time(pythagoreans)(10))     // 0ms
// println(time(pythagoreans)(100))    // 3ms
// println(time(pythagoreans)(250))    // 11.8ms
// println(time(pythagoreans)(500))    // 53.4ms
// println(time(pythagoreans)(750))    // 149.8ms
// println(time(pythagoreans)(1000))   // 367.2ms
// println(time(pythagoreans)(1250))   // 684.8ms
// println(time(pythagoreans)(1500))   // 1232ms


// Parallelization (Results averaged over 5 runs)
// (with interchanged sides (3,4,5) and (4,3,5))
//=======================================================================+
//                                               | Using Parallel Stream |
//=======================================================================+
// println(time(pythagoreansParallel(10)))   //  |        20.6ms         |
// println(time(pythagoreansParallel(100)))  //  |        28.8ms         |
// println(time(pythagoreansParallel(250)))  //  |        56.4ms         |
// println(time(pythagoreansParallel(500)))  //  |        93ms           |
// println(time(pythagoreansParallel(750)))  //  |        182ms          |
// println(time(pythagoreansParallel(1000))) //  |        334ms          |
// println(time(pythagoreansParallel(1250))) //  |        609.2ms        |
// println(time(pythagoreansParallel(1500))) //  |        995.2ms        |
//=======================================================================+

// Sequential Pythagoreans (Results averaged over 5 runs)
// (with interchanged sides (3,4,5) and (4,3,5))
// ======================================================
// time(pythagoreans)(10)     // 0ms
// time(pythagoreans)(100)    // 6.6ms
// time(pythagoreans)(250)    // 36.4ms
// time(pythagoreans)(500)    // 251.8ms
// time(pythagoreans)(750)    // 848ms
// time(pythagoreans)(1000)   // 2020.6ms
// time(pythagoreans)(1250)   // 4093ms
// time(pythagoreans)(1500)   // 7163.2ms

