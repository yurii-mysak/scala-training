package com.scala_training.cats.streaming

import cats.effect.Sync
import fs2.Stream

import scala.compiletime.{constValue, erasedValue}

class ReflectiveStreamProcessor {

  inline def processWithReflection[F[_]: Sync, A](stream: Stream[F, A])(
    using m: scala.deriving.Mirror.Of[A]
  ): Stream[F, String] = stream.map { element =>
    inline m match {
      case p: scala.deriving.Mirror.ProductOf[A] =>
        val fieldNames  = constValueTuple[p.MirroredElemLabels]
        val productElem = element.asInstanceOf[Product]
        fieldNames
          .zip(productElem.productIterator.toList)
          .map { case (label, value) => s"$label: $value" }
          .mkString(", ")
      case _                                     => element.toString
    }
  }

  private inline def constValueTuple[T <: Tuple]: List[String] = inline erasedValue[T] match {
    case _: EmptyTuple     => Nil
    case _: (head *: tail) =>
      constValue[head].toString :: constValueTuple[tail]
  }
}
