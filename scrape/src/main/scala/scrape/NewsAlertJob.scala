package scrape

import org.quartz.{Job, JobExecutionContext}

class NewsAlertJob extends Job:
  def execute(context: JobExecutionContext): Unit = {
    val body =
      Guardian
        .scrapeHeadlines()
        .map(h => s"<li><a href=\"${h.url}\">${h.title}</a></li>")
        .mkString("<div><ul>", "\n\n", "</ul></div>")

    Ethereal.sendEmail(
      "some_to_address@gmail.com",
      "News Headlines",
      body
    )
  }
