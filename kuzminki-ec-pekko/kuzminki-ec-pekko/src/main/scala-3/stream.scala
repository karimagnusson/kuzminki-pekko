package kuzminki.pekko

import scala.concurrent.{Future, ExecutionContext}
import org.apache.pekko.{Done, NotUsed}
import org.apache.pekko.stream.scaladsl.{Source, Sink}
import kuzminki.api.Kuzminki
import kuzminki.select.{Pages, Offset}
import kuzminki.run.{
  RunUpdate,
  RunOperationParams
}


package object stream {

  extension [P](query: RunOperationParams[P]) {

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

  extension [P1, P2](query: RunUpdate[P1, P2]) {

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

    def asSource(using db: Kuzminki, ec: ExecutionContext): Source[R, NotUsed] = {
      asSourceBatch(100)
    }

    def asSourceBatch(size: Int)(using db: Kuzminki, ec: ExecutionContext): Source[R, NotUsed] = {
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





