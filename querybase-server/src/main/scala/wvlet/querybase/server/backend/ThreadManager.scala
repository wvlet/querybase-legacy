package wvlet.querybase.server.backend

import java.util.concurrent.Executors

class ThreadManager extends AutoCloseable {
  private val executorService = Executors.newCachedThreadPool()
  def submit[U](body: => U): Unit = {
    executorService.submit(new Runnable {
      override def run(): Unit = body
    })
  }
  override def close(): Unit = executorService.shutdownNow()
}
