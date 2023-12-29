package kuzminki.akka

import scala.concurrent.{Future, ExecutionContext}
import akka.stream.scaladsl.{Source, Sink}
import kuzminki.api.Kuzminki
import kuzminki.run.{
  RunQuery,
  RunUpdate,
  RunOperationParams,
  Pages
}


package object stream {

  implicit class OperationAsSink[P](query: RunOperationParams[P])(implicit db: Kuzminki, ec: ExecutionContext) {

    def asSink = Sink.foreachAsync(1) { (p: P) =>
      db.exec(query.render(p))
    }

    def asBatchSink = Sink.foreachAsync(1) { (chunk: Seq[P]) =>
      db.execList(chunk.map(p => query.render(p)))
    }
  }

  implicit class UpdateAsSink[P1, P2](query: RunUpdate[P1, P2])(implicit db: Kuzminki, ec: ExecutionContext) {

    def asSink = Sink.foreachAsync(1) { (p: Tuple2[P1, P2]) =>
      db.exec(query.render(p._1, p._2))
    }

    def asBatchSink = Sink.foreachAsync(1) { (chunk: Seq[Tuple2[P1, P2]]) =>
      db.execList(chunk.toList.map(p => query.render(p._1, p._2)))
    }
  }

  implicit class RunAsSource[R](query: RunQuery[R])(implicit db: Kuzminki, ec: ExecutionContext) {

    def asSource = asSourceBatch(100)

    def asSourceBatch(size: Int) = {
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





