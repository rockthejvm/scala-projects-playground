package scrape

import org.quartz.{Job, JobExecutionContext}

class NewsAlertJob extends Job:
  def execute(context: JobExecutionContext): Unit = {
    val headlines = Guardian.scrapeHeadlines()

    SendGrid.sendEmail(
      System.getenv("SENDGRID_API_KEY"),
      System.getenv("SENDGRID_FROM_EMAIL"),
      "News Headlines",
      "some_to_address@gmail.com", // TODO: use receiver's email address
      headlines
        .map(h => s"<li><a href=\"${h.url}\">${h.title}</a></li>")
        .mkString("<div><ul>", "\n\n", "</ul></div>")
    )
  }
