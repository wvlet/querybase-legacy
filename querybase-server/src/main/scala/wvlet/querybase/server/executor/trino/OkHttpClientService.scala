package wvlet.querybase.server.executor.trino

import okhttp3.OkHttpClient
import wvlet.airframe.Design
import wvlet.log.LogSupport

import java.util.concurrent.TimeUnit

/**
  */
object OkHttpClientService extends LogSupport {
  def design: Design =
    Design.newDesign
      .bind[OkHttpClient].toInstance {
        info(s"Starting OkHttp client")
        val builder = new OkHttpClient.Builder
        builder
          .connectTimeout(30, TimeUnit.SECONDS)
          .readTimeout(1, TimeUnit.MINUTES)
          .writeTimeout(1, TimeUnit.MINUTES)
        builder.build
      }
      .onShutdown { okHttpClient =>
        info(s"Closing OkHttp client")
        okHttpClient.dispatcher().executorService().shutdown()
        okHttpClient.connectionPool().evictAll()
      }
}
