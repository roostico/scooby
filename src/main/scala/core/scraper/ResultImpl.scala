package org.unibo.scooby
package core.scraper

import Aggregator.ItAggregator

/**
 * Class representing {@link ScraperActor}'s results.
 * @tparam T
 *   representing result's type.
 */
trait Result[T]:

  /**
   * Data structure used to store data.
   * @return
   *   data structure.
   */
  def data: Iterable[T]

  /**
   * Batch a single data to result.
   *
   * @param data
   *   single data to add.
   * @param aggregator
   *   data aggregator.
   * @return
   *   a new Result instance with data updated.
   */
  def updateStream(data: T)(using aggregator: ItAggregator[T]): Result[T]

  /**
   * Batch a sequence of data to result.
   *
   * @param data
   *   sequence of data to add.
   * @param aggregator
   *   data aggregator.
   * @return
   *   a new Result instance with data updated.
   */
  def updateBatch(data: Iterable[T])(using aggregator: ItAggregator[T]): Result[T]

  /**
   * Aggregate actual Result with a given one.
   *
   * @param result
   *   the Result to aggregate.
   * @param aggregator
   *   data aggregator.
   * @return
   *   a new Result instance with data aggregated.
   */
  def aggregate(result: Result[T])(using aggregator: ItAggregator[T]): Result[T]

/**
 * Class representing {@link ScraperActor}'s results implementation.
 * @param data
 *   representing actual result.
 * @tparam T
 *   representing result's type.
 */
final case class ResultImpl[T](data: Iterable[T]) extends Result[T]:

  override def updateStream(data: T)(using aggregator: ItAggregator[T]): Result[T] =
    ResultImpl(aggregator.aggregateStream(this.data, data))

  override def updateBatch(data: Iterable[T])(using aggregator: ItAggregator[T]): Result[T] =
    ResultImpl(aggregator.aggregateBatch(this.data, data))

  override def aggregate(result: Result[T])(using aggregator: ItAggregator[T]): Result[T] =
    updateBatch(result.data)

object Result:

  /**
   * A builder with a starting data.
   *
   * @param data
   *   the starting data iterable.
   * @tparam T
   *   the data type.
   * @return
   *   a new Result instance with given data.
   */
  def apply[T](data: Iterable[T]): Result[T] = ResultImpl(data)

  /**
   * A builder with a starting data.
   *
   * @param data the starting singular data.
   * @tparam T the data type.
   * @return a new Result instance with given data.
   */
  def apply[T](data: T): Result[T] = Result(Iterable(data))

  private def apply[T](): Result[T] = Result(Iterable.empty)

  /**
   * A builder for an empty {@link Result}.
   *
   * @tparam T the data type.
   * @return a new Result instance with given data.
   */
  def empty[T]: Result[T] = Result()
