package kuzminki.akka

import scala.concurrent.{Future, ExecutionContext}
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Source, Sink}
import kuzminki.api.Kuzminki
import kuzminki.select.{Pages, Offset}
import kuzminki.insert.StoredInsert
import kuzminki.delete.StoredDelete
import kuzminki.update.StoredUpdate


package object stream {

  extension [P](query: StoredInsert[P]) {

    def asSink(using db: Kuzminki, ec: ExecutionContext): Sink[P, Future[Done]] = {
      Sink.foreachAsync(1) { (p: P) =>
        db.exec(query.render(p))
      }
    }

    def asBatchSink(using db: Kuzminki, ec: ExecutionContext): Sink[Seq[P], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[P]) =>
        db.execList(chunk.map(p => query.render(p)))
      }
    }
  }

  extension [P](query: StoredDelete[P]) {

    def asSink(using db: Kuzminki, ec: ExecutionContext): Sink[P, Future[Done]] = {
      Sink.foreachAsync(1) { (p: P) =>
        db.exec(query.render(p))
      }
    }

    def asBatchSink(using db: Kuzminki, ec: ExecutionContext): Sink[Seq[P], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[P]) =>
        db.execList(chunk.map(p => query.render(p)))
      }
    }
  }

  extension [P1, P2](query: StoredUpdate[P1, P2]) {

    def asSink(using db: Kuzminki, ec: ExecutionContext): Sink[Tuple2[P1, P2], Future[Done]] = {
      Sink.foreachAsync(1) { (p: Tuple2[P1, P2]) =>
        db.exec(query.render(p._1, p._2))
      }
    }

    def asBatchSink(using db: Kuzminki, ec: ExecutionContext): Sink[Seq[Tuple2[P1, P2]], Future[Done]] = {
      Sink.foreachAsync(1) { (chunk: Seq[Tuple2[P1, P2]]) =>
        db.execList(chunk.toList.map(p => query.render(p._1, p._2)))
      }
    }
  }

  extension [M, R](query: Offset[M, R]) {

    def stream(using db: Kuzminki, ec: ExecutionContext): Source[R, NotUsed] = {
      stream(100)
    }

    def stream(size: Int)(using db: Kuzminki, ec: ExecutionContext): Source[R, NotUsed] = {
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
  }
}





