package kuzminki.pekko

import scala.concurrent.{Future, ExecutionContext}
import org.apache.pekko.{Done, NotUsed}
import org.apache.pekko.stream.scaladsl.{Source, Sink}
import shapeless._
import shapeless.ops.hlist.Tupler
import kuzminki.api.Kuzminki
import kuzminki.select.{Pages, Offset}
import kuzminki.insert.StoredInsert
import kuzminki.delete.StoredDelete
import kuzminki.update.StoredUpdate


package object stream {

  implicit class InsertAsSink[P, B <: HList](query: StoredInsert[P]) {

    def asSink(
      implicit db: Kuzminki,
               ec: ExecutionContext
    ): Sink[P, Future[Done]] = {
      Sink.foreachAsync(1) { (p: P) =>
        db.exec(query.render(p))
      }
    }

    def asBatchSink(
      implicit db: Kuzminki,
               ec: ExecutionContext
    ): Sink[Seq[P], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[P]) =>
        db.execList(
          chunk.map { (p: P) => query.render(p) }
        )
      }
    }

    def asTypeSink[T <: Product](
      implicit generic: Generic.Aux[T, B],
               tupler: Tupler.Aux[B, P],
               db: Kuzminki,
               ec: ExecutionContext
    ): Sink[T, Future[Done]] = {
      Sink.foreachAsync(1) { (t: T) =>
        db.exec(
          query.render(
            Generic[T].to(t).tupled
          )
        )
      }
    }

    def asTypeChunkSink[T <: Product](
      implicit generic: Generic.Aux[T, B],
               tupler: Tupler.Aux[B, P],
               db: Kuzminki,
               ec: ExecutionContext
    ): Sink[Seq[T], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[T]) =>
        db.execList(
          chunk.toList.map { (t: T) =>
            query.render(
              Generic[T].to(t).tupled
            )
          }
        )
      }
    }
  }

  implicit class DeleteAsSink[P](query: StoredDelete[P]) {

    def asSink(
      implicit db: Kuzminki,
               ec: ExecutionContext
    ): Sink[P, Future[Done]] = {
      Sink.foreachAsync(1) { (p: P) =>
        db.exec(query.render(p))
      }
    }

    def asBatchSink(
      implicit db: Kuzminki,
               ec: ExecutionContext
    ): Sink[Seq[P], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[P]) =>
        db.execList(
          chunk.map { (p: P) => query.render(p) }
        )
      }
    }
  }

  implicit class UpdateAsSink[P1, P2](query: StoredUpdate[P1, P2]) {

    def asSink(
      implicit db: Kuzminki,
               ec: ExecutionContext
    ): Sink[Tuple2[P1, P2], Future[Done]] = {
      Sink.foreachAsync(1) { (p: Tuple2[P1, P2]) =>
        db.exec(query.render(p._1, p._2))
      }
    }    

    def asBatchSink(
      implicit db: Kuzminki,
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

  implicit class RunAsSource[M, R, B <: HList](query: Offset[M, R]) {

    def stream(implicit db: Kuzminki, ec: ExecutionContext): Source[R, NotUsed] = {
      stream(100)
    }

    def stream(size: Int)(implicit db: Kuzminki, ec: ExecutionContext): Source[R, NotUsed] = {
      val pages = Pages(query.render, size)
      Source.unfoldAsync(pages) { pages =>
        pages.next.map {
          case Nil => None
          case items => Some((pages, items))
        }
      }
      .mapConcat(i => i)
    }

    def streamType[T <: Product](
      implicit untupler: Generic.Aux[R, B],
               generic: Generic.Aux[T, B],
               db: Kuzminki,
               ec: ExecutionContext
    ): Source[T, NotUsed] = {
      stream.map((r: R) => generic.from(untupler.to(r)))
    }

    def streamType[T <: Product](size: Int)(
      implicit untupler: Generic.Aux[R, B],
               generic: Generic.Aux[T, B],
               db: Kuzminki,
               ec: ExecutionContext
    ): Source[T, NotUsed] = {
      stream(size).map((r: R) => generic.from(untupler.to(r)))
    }
  }
}





