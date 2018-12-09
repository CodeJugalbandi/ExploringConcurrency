import java.util.*;
import java.util.function.*;

public class PythagoreanSequential {
  
  static <T, R> Function<T, R> time(Function<T, R> f) {
    return t -> {
      long startTime = System.currentTimeMillis();
      R result = f.apply(t);
      long diff = System.currentTimeMillis() - startTime;
      System.out.println(String.format("Time Taken = %d(ms).", diff));
      return result;
    };
  }
  
  // with interchanged sides (3,4,5) and (4,3,5)
  // static List<List<?>> pythagoreans(int n) {
  //   List<List<?>> result = new ArrayList<>();
  //   for (int x = 1; x <= n; x++) {
  //     for (int y = 1; y <= n; y++) {
  //       for (int z = 1; z <= n; z++) {
  //         if (x*x + y*y == z*z)
  //           result.add(List.of(x, y, z));
  //       }
  //     }
  //   }
  //   return result;
  // }
  
  // without interchanged sides (3,4,5) and (4,3,5), only (3,4,5)
  static List<List<?>> pythagoreans(int n) {
    List<List<?>> result = new ArrayList<>();
    for (int x = 1; x <= n; x++) {
      for (int y = x; y <= n; y++) {
        for (int z = y; z <= n; z++) {
          if (x*x + y*y == z*z)
            result.add(List.of(x, y, z));
        }
      }
    }
    return result;
  }
  
  public static void main(String[] args) {
    System.out.println(time(PythagoreanSequential::pythagoreans).apply(10));
    System.out.println("DONE");
  }
}


//--------------------< SET I >---------------------------
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

//--------------------< SET II >---------------------------
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
