package scrape

import com.sendgrid.*
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.{Content, Email}

import scala.util.{Try, Success, Failure}

object SendGrid:
  def sendEmail(
    apiKey:  String,
    from:    String,
    subject: String,
    to:      String,
    body:    String
  ): Unit = {
    val sg = new SendGrid(apiKey)

    val mail =
      new Mail(
        new Email(from),
        subject,
        new Email(to),
        new Content("text/html", body)
      )

    val request = new Request()
    request.setMethod(Method.POST)
    request.setEndpoint("mail/send")
    request.setBody(mail.build())

    Try(sg.api(request)) match
      case Success(response) =>
        println(s"Email sent with status code: ${response.getStatusCode}")
      case Failure(exception) =>
        println(s"Failed to send email: ${exception.getMessage}")
  }
end SendGrid
