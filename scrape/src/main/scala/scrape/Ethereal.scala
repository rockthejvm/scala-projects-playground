package scrape

import java.util.{Properties, UUID}
import javax.mail.*
import javax.mail.internet.*

object Ethereal:
  def sendEmail(to: String, subject: String, body: String): Unit = {
    val session =
      smtpSession(
        System.getenv("SMTP_USERNAME"),
        System.getenv("SMTP_PASSWORD"),
        smtpProperties()
      )

    try {
      Transport.send {
        val msg = new MimeMessage(session)
        msg.setFrom(new InternetAddress("no-reply@my-domain.net"))
        msg.setRecipients(Message.RecipientType.TO, to)
        msg.setSubject(subject)
        msg.setContent(body, "text/html")
        msg.setHeader("Message-ID", UUID.randomUUID().toString)
        msg
      }

      println("Email sent successfully!")
    } catch {
      case e: MessagingException =>
        e.printStackTrace()
    }
  }

  private def smtpSession(email: String, password: String, props: Properties): Session = {
    Session.getInstance(
      props,
      new Authenticator {
        override def getPasswordAuthentication: PasswordAuthentication = {
          new PasswordAuthentication(email, password)
        }
      }
    )
  }

  private def smtpProperties(): Properties = {
    val props = new Properties()
    props.put("mail.smtp.host", "smtp.ethereal.email")
    props.put("mail.smtp.port", "587")
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true") // For TLS
    props
  }
