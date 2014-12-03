package curl

import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult
import com.thoughtworks.go.plugin.api.task._
import java.io.{ ByteArrayInputStream, IOException }
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.sys.process._

class CurlTaskExecutor extends TaskExecutor {

  val CURLED_FILE = "index.txt"

  override def execute(taskConfig: TaskConfig, taskContext: TaskExecutionContext): ExecutionResult = {
    try {
      runCommand(taskConfig, taskContext)
    } catch {
      case e: Exception =>
        ExecutionResult.failure("Failed to download file from URL: " + taskConfig.getValue(CurlTask.URL_PROPERTY), e)
    }
  }

  @throws[IOException]
  @throws[InterruptedException]
  private def runCommand(taskConfig: TaskConfig, taskContext: TaskExecutionContext): ExecutionResult = {
    val curl = createCurlCommandWithOptions(taskContext, taskConfig)
    val console: Console = taskContext.console
    val environment: EnvironmentVariables = taskContext.environment

    console.printLine("Launching command: " + curl.mkString(" "))
    val goEnv = environment.asMap.asScala
    val allEnvVars = System.getenv.asScala ++ goEnv
    console.printEnvironment(allEnvVars.asJava, environment.secureEnvSpecifier)

    val process = Process(curl, None, goEnv.toList: _*)
    val stdout = new ListBuffer[String]
    val stderr = new ListBuffer[String]
    val exitCode = process.!(ProcessLogger(stdout.append(_), stderr.append(_)))

    console.readErrorOf(new ByteArrayInputStream(stderr.mkString("\n").getBytes))
    console.readOutputOf(new ByteArrayInputStream(stdout.mkString("\n").getBytes))

    if (exitCode != 0) {
      ExecutionResult.failure("Failed downloading file. Please check the output")
    } else {
      ExecutionResult.success("Downloaded file: " + CURLED_FILE)
    }
  }

  private def createCurlCommandWithOptions(taskContext: TaskExecutionContext, taskConfig: TaskConfig): Seq[String] = {
    val requestType = taskConfig.getValue(CurlTask.REQUEST_PROPERTY)
    val isInsecure = if (taskConfig.getValue(CurlTask.SECURE_CONNECTION_PROPERTY) != CurlTask.SECURE_CONNECTION) "--insecure" else ""
    val additionalOptions = Option(taskConfig.getValue(CurlTask.ADDITIONAL_OPTIONS)).filterNot(_.trim.isEmpty) getOrElse ""
    val destinationFilePath = taskContext.workingDir + "/" + CURLED_FILE
    val url = taskConfig.getValue(CurlTask.URL_PROPERTY)

    Seq("curl", requestType, isInsecure, additionalOptions, "-o", destinationFilePath, url).filterNot(_.trim.isEmpty)
  }

}
