package com

package object tsys {
  import scala.util.control._
  def using[T <: AutoCloseable, U](resource: T)(fn: T => U): U = {
    require(resource != null, "Must supply a resource, can't be null!")
    var problem: Throwable = null
    try {
      fn(resource)
    } catch {
      case NonFatal(e) =>
        problem = e
        throw e
    } finally {
      try {
        resource.close()
      } catch {
        case NonFatal(suppressed) =>
          if (problem != null)
            problem.addSuppressed(suppressed)
      }
    }
  }
}