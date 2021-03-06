package com.github.j5ik2o.dddbase.slick

import com.github.j5ik2o.dddbase.slick.AggregateIOBaseFeature.RIO
import com.github.j5ik2o.dddbase.{ AggregateNotFoundException, AggregateSingleReader }
import monix.eval.Task

trait AggregateSingleReadFeature extends AggregateSingleReader[RIO] with AggregateBaseReadFeature {

  override def resolveById(id: IdType): RIO[AggregateType] =
    for {
      record <- Task
        .deferFutureAction { implicit ec =>
          import profile.api._
          db.run(dao.filter(byCondition(id)).take(1).result)
            .map(_.headOption)
            .map(_.getOrElse(throw AggregateNotFoundException(id)))
        }
      aggregate <- convertToAggregate(record)
    } yield aggregate
}
