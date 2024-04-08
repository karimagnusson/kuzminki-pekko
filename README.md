[![Twitter URL](https://img.shields.io/twitter/url/https/twitter.com/bukotsunikki.svg?style=social&label=Follow%20%40kuzminki_lib)](https://twitter.com/kuzminki_lib)

# kuzminki-pekko

#### About
This project adds support for [Pekko](https://pekko.apache.org/) streaming to [kuzminki-ec](https://github.com/karimagnusson/kuzminki-ec). Take a look at the [kuzminki-play-demo](https://github.com/karimagnusson/kuzminki-play-demo) for an example of usage.

If you are using Akka, you can use version [0.9.3](https://github.com/karimagnusson/kuzminki-pekko/releases/tag/0.9.3)

#### Sbt
```sbt
// available for Scala 2.13 and Scala 3
libraryDependencies += "io.github.karimagnusson" %% "kuzminki-pekko" % "0.9.5"
```

#### Examples
Query as Source.
```scala
import kuzminki.pekko.stream._

sql
  .select(user)
  .cols2(t => (
    t.name,
    t.email
  ))
  .all
  .orderBy(_.name.asc)
  .stream
  .map(doSmothing)
  .runWith(mySink)

// By default the source will fetch 100 rows each time.
// To fetch a different number of rows: .stream(1000)

// To get a stream where the tuple is read into a type.
.streamType[MyType]
```

Query as Sink.
```scala
import kuzminki.akka.stream._

val insertUserStm = sql
  .insert(user)
  .cols2(t => (
    t.name,
    t.email
  ))
  .cache

Source(someData)
  .map(doSmothing)
  .runWith(insertUserStm.asSink)

// To insert in batches of 100

Source(someData)
  .map(doSmothing)
  .grouped(100) // insert 100 in each transaction.
  .runWith(insertUserStm.asChunkSink)

// To create a sink that takes a type
insertUserStm.asTypeSink[MyType]
```








