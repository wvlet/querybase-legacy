package wvlet.querybase.server.backend.query

import java.time.format.{DateTimeFormatterBuilder, SignStyle}
import java.time.temporal.ChronoField._
import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.Locale
import java.util.concurrent.{ThreadLocalRandom, TimeUnit}
import scala.concurrent.duration.MILLISECONDS

/** A Scala version of https://github.com/trinodb/trino/blob/master/core/trino-main/src/main/java/io/trino/execution/QueryIdGenerator.java
  */
class QueryIdGenerator {
  private val BASE_32 = Array('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't',
    'u', 'v', 'w', 'x', 'y', 'z', '2', '3', '4', '5', '6', '7', '8', '9')

  private val coordinatorId = {
    val s = new StringBuilder
    (0 until 5).foreach { i =>
      s.append(BASE_32(ThreadLocalRandom.current().nextInt(32)))
    }
    s.result()
  }

  private val BASE_SYSTEM_TIME_MILLIS = System.currentTimeMillis
  private val BASE_NANO_TIME          = System.nanoTime

  private val TIMESTAMP_FORMAT = {
    new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendValue(MONTH_OF_YEAR, 2)
      .appendValue(DAY_OF_MONTH, 2)
      .appendLiteral('_')
      .appendValue(HOUR_OF_DAY, 2)
      .appendValue(MINUTE_OF_HOUR, 2)
      .appendValue(SECOND_OF_MINUTE, 2)
      .toFormatter(Locale.US)
  }

  private var lastTimeInSeconds = 0L
  private var lastTimestamp     = ""
  private var lastTimeInDays    = 0L
  private var counter: Long     = 0L

  private def nowInMillis: Long = {
    // Avoid unexpected clock rolling back
    BASE_SYSTEM_TIME_MILLIS + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - BASE_NANO_TIME)
  }

  def newQueryId: String = {
    import org.weakref.jmx.internal.guava.util.concurrent.Uninterruptibles

    import java.util.concurrent.TimeUnit
    // only generate 100,000 ids per day
    if (counter > 99_999) { // wait for the second to rollover
      while (MILLISECONDS.toSeconds(nowInMillis) != lastTimeInSeconds) {
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS)
      }
      counter = 0
    }

    // if it has been a second since the last id was generated, generate a new timestamp
    val now = nowInMillis
    if (MILLISECONDS.toSeconds(now) != lastTimeInSeconds) { // generate new timestamp
      lastTimeInSeconds = MILLISECONDS.toSeconds(now)
      val timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneOffset.UTC)
      lastTimestamp = TIMESTAMP_FORMAT.format(timestamp)
      // if the day has rolled over, restart the counter
      if (MILLISECONDS.toDays(now) == lastTimeInDays) {
        lastTimeInDays = MILLISECONDS.toDays(now)
        counter = 0
      }
    }
    val c = counter
    counter += 1
    f"${lastTimestamp}_${c}%05d_${coordinatorId}"
  }

}
