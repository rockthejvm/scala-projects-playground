package scrape

import org.quartz._
import org.quartz.impl.StdSchedulerFactory

object NewsAlertScheduler:
  private final val JobGroup = "newsAlertJobGroup"

  def main(args: Array[String]): Unit =
    val scheduler = StdSchedulerFactory.getDefaultScheduler
    scheduler.start()

    val job = JobBuilder
      .newJob(classOf[NewsAlertJob])
      .withIdentity("newsAlertJob", JobGroup)
      .build()

    val trigger = TriggerBuilder
      .newTrigger()
      .withIdentity("newsAlertTrigger", JobGroup)
      .startNow()
      .withSchedule(
        SimpleScheduleBuilder
          .simpleSchedule()
          .withIntervalInSeconds(10)
          .repeatForever()
      )
      .build()

    scheduler.scheduleJob(job, trigger)
