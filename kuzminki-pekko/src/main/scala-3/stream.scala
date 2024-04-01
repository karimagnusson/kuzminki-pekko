/*
* Copyright 2021 Kári Magnússon
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package kuzminki.pekko

import scala.deriving.Mirror.ProductOf
import scala.concurrent.{Future, ExecutionContext}
import org.apache.pekko.{Done, NotUsed}
import org.apache.pekko.stream.scaladsl.{Source, Sink}
import kuzminki.api.Kuzminki
import kuzminki.select.{Pages, Offset}
import kuzminki.insert.StoredInsert
import kuzminki.delete.StoredDelete
import kuzminki.update.StoredUpdate


package object stream {

  extension [P](query: StoredInsert[P]) {

    def asSink(
      using db: Kuzminki,
            ec: ExecutionContext
    ): Sink[P, Future[Done]] = {
      Sink.foreachAsync(1) { (p: P) =>
        db.exec(query.render(p))
      }
    }

    def asChunkSink(
      using db: Kuzminki,
            ec: ExecutionContext
    ): Sink[Seq[P], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[P]) =>
        db.execList(
          chunk.map { (p: P) => query.render(p) }
        )
      }
    }

    def asTypeSink[T <: Product](
      using mirror: ProductOf[T],
            ev: P <:< mirror.MirroredElemTypes,
            db: Kuzminki,
            ec: ExecutionContext
    ): Sink[T, Future[Done]] = {
      Sink.foreachAsync(1) { (t: T) =>
        db.exec(
          query.render(
            Tuple.fromProductTyped(t).asInstanceOf[P]
          )
        )
      }
    }

    def asTypeChunkSink[T <: Product](
      using mirror: ProductOf[T],
            ev: P <:< mirror.MirroredElemTypes,
            db: Kuzminki,
            ec: ExecutionContext
    ): Sink[Seq[T], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[T]) =>
        db.execList(
          chunk.toList.map { (t: T) =>
            query.render(
              Tuple.fromProductTyped(t).asInstanceOf[P]
            )
          }
        )
      }
    }
  }

  extension [P](query: StoredDelete[P]) {

    def asSink(
      using db: Kuzminki,
            ec: ExecutionContext
    ): Sink[P, Future[Done]] = {
      Sink.foreachAsync(1) { (p: P) =>
        db.exec(query.render(p))
      }
    }

    def asChunkSink(
      using db: Kuzminki,
            ec: ExecutionContext
    ): Sink[Seq[P], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[P]) =>
        db.execList(
          chunk.map { (p: P) => query.render(p) }
        )
      }
    }

    def asTypeSink[T <: Product](
      using mirror: ProductOf[T],
            ev: P <:< mirror.MirroredElemTypes,
            db: Kuzminki,
            ec: ExecutionContext
    ): Sink[T, Future[Done]] = {
      Sink.foreachAsync(1) { (t: T) =>
        db.exec(
          query.render(
            Tuple.fromProductTyped(t).asInstanceOf[P]
          )
        )
      }
    }

    def asTypeChunkSink[T <: Product](
      using mirror: ProductOf[T],
            ev: P <:< mirror.MirroredElemTypes,
            db: Kuzminki,
            ec: ExecutionContext
    ): Sink[Seq[T], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[T]) =>
        db.execList(
          chunk.toList.map { (t: T) =>
            query.render(
              Tuple.fromProductTyped(t).asInstanceOf[P]
            )
          }
        )
      }
    }
  }

  extension [P1, P2](query: StoredUpdate[P1, P2]) {

    def asSink(
      using db: Kuzminki,
            ec: ExecutionContext
    ): Sink[Tuple2[P1, P2], Future[Done]] = {
      Sink.foreachAsync(1) { (p: Tuple2[P1, P2]) =>
        db.exec(query.render(p._1, p._2))
      }
    }

    def asChunkSink(
      using db: Kuzminki,
            ec: ExecutionContext
    ): Sink[Seq[Tuple2[P1, P2]], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[Tuple2[P1, P2]]) =>
        db.execList(
          chunk.toList.map { (p: Tuple2[P1, P2]) =>
            query.render(p._1, p._2)
          }
        )
      }
    }
  }

  extension [M, R](query: Offset[M, R]) {

    def stream(
      using db: Kuzminki,
            ec: ExecutionContext
    ): Source[R, NotUsed] = {
      stream(100)
    }

    def stream(size: Int)(
      using db: Kuzminki,
            ec: ExecutionContext
    ): Source[R, NotUsed] = {
      val pages = Pages(query.render, size)
      Source
        .unfoldAsync(pages) { pages =>
          pages.next.map {
            case Nil => None
            case items => Some((pages, items))
          }
        }
        .mapConcat(i => i)
    }

    def streamType[T <: Product](
      using mirror: ProductOf[T],
            ev: R <:< mirror.MirroredElemTypes,
            db: Kuzminki,
            ec: ExecutionContext
    ): Source[T, NotUsed] = {
      stream.map((r: R) => mirror.fromProduct(r))
    }

    def streamType[T <: Product](size: Int)(
      using mirror: ProductOf[T],
            ev: R <:< mirror.MirroredElemTypes,
            db: Kuzminki,
            ec: ExecutionContext
    ): Source[T, NotUsed] = {
      stream(size).map((r: R) => mirror.fromProduct(r))
    }
  }
}





