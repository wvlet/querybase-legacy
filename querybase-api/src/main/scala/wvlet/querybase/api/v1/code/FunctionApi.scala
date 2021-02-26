package wvlet.querybase.api.v1.code

import java.time.Instant

import wvlet.airframe.http.RPC

/**
  */
object FunctionApi {

  case class FunctionDef(
      // The module ID it belongs to
      moduleId: Option[String] = None,
      // The server-generated function ID
      id: String = "N/A",
      // Function name
      name: String,
      // Type of the function (e.g., SQL, Scala, etc.)
      kind: String,
      // Function input
      input: FunctionInput,
      // Function output. This can be defined lazily
      output: FunctionOutput,
      // Function body
      body: String,
      // Tags for classifying functions
      tags: Map[String, String] = Map.empty,
      // Parent function (if exists)
      parent: Option[FunctionDef] = None
  )

  case class FunctionInput(
      params: Seq[Param]
  )

  case class FunctionOutput(
      params: Seq[Param],
      revision: Option[Int] = None,
      updatedAt: Instant
  )

  case class Param(
      name: String,
      paramType: String,
      value: String
  )
}

@RPC
trait FunctionApi {
  import FunctionApi._

  def run(sessionId: String, functionId: String, input: FunctionInput): Unit
}
