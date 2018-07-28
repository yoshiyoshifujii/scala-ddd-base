package com.github.j5ik2o.dddbase.redis

import cats.data.ReaderT
import com.github.j5ik2o.dddbase.AggregateMultiWriter
import com.github.j5ik2o.dddbase.redis.AggregateIOBaseFeature.RIO
import com.github.j5ik2o.reactive.redis.RedisConnection
import monix.eval.Task

import scala.concurrent.duration.Duration

trait AggregateMultiWriteFeature extends AggregateMultiWriter[RIO] with AggregateBaseWriteFeature {

  override type SMO = Duration
  override val defaultStoreMultiOption: SMO = Duration.Inf

  override def storeMulti(aggregates: Seq[AggregateType], storeMultiOption: SMO): RIO[Long] =
    ReaderT[Task, RedisConnection, Long] { con =>
      for {
        records <- Task.traverse(aggregates) { aggregate =>
          convertToRecord(aggregate)(con)
        }
        result <- dao.setMulti(records.map(v => (v, storeMultiOption))).run(con)
      } yield result
    }

}