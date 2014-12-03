package curl

import com.thoughtworks.go.plugin.api.annotation.Extension
import com.thoughtworks.go.plugin.api.response.validation.{ValidationError, ValidationResult}
import com.thoughtworks.go.plugin.api.task.{Task, TaskConfig, TaskView}

@Extension
class CurlTask extends Task {

  import CurlTask._
  
  override def config(): TaskConfig = {
    val config = new TaskConfig()
    config.addProperty(URL_PROPERTY)
    config.addProperty(SECURE_CONNECTION_PROPERTY).withDefault(SECURE_CONNECTION)
    config.addProperty(REQUEST_PROPERTY).withDefault(REQUEST_TYPE)
    config.addProperty(ADDITIONAL_OPTIONS)
    config
  }

  override def executor() = new CurlTaskExecutor()

  override def view(): TaskView = new TaskView {
    val displayValue = "Curl"

    def template() = {
      try {
        io.Source.fromInputStream(getClass.getResourceAsStream("/views/task.template.html")).mkString
      } catch {
        case e: Exception => "Failed to find template: " + e.getMessage
      }
    }
  }

  override def validate(configuration: TaskConfig): ValidationResult = {
    val result = new ValidationResult()

    val url = configuration.getValue(URL_PROPERTY)
    if (url == null || url.trim.isEmpty) {
      result.addError(new ValidationError(URL_PROPERTY, "URL cannot be empty"))
    }

    result
  }

}

object CurlTask {
  val URL_PROPERTY = "Url"
  val ADDITIONAL_OPTIONS = "AdditionalOptions"
  val SECURE_CONNECTION = "yes"
  val SECURE_CONNECTION_PROPERTY = "SecureConnection"
  val REQUEST_TYPE = "-G"
  val REQUEST_PROPERTY = "RequestType"
}
