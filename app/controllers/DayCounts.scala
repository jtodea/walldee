package controllers

import play.api.mvc.{Action, Controller}
import models.{Sprint, DayCount, Story}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.json.SprintCounterValue

object DayCounts extends Controller {
  def create(sprintId: Long) = Action {
    implicit request =>
      Sprint.findById(sprintId).map {
        sprint =>
          dayCountForm(sprint).bindFromRequest.fold(
          formWithErrors => BadRequest, {
            dayCount =>
              dayCount.save
              Redirect(routes.Sprints.show(sprintId))
          })
      }.getOrElse(NotFound)
  }

  def dayCountForm(sprint: Sprint): Form[DayCount] = {
    val dayCount = new DayCount(dayNum = 0, sprintId = sprint.id)

    dayCount.counterValues = sprint.counters.map {
      counter =>
        SprintCounterValue(counter.name, 0)
    }
    dayCountForm(dayCount)
  }

  def dayCountForm(dayCount: DayCount): Form[DayCount] = Form(
    mapping(
      "dayNum" -> number(min = 0, max = 100),
      "counterValues" -> list(
        mapping(
          "name" -> text,
          "value" -> number
        ) {
          (name, value) => SprintCounterValue(name, value)
        } {
          counterValue =>
            println(">>>>>>>>>>>" + counterValue)
            Some(counterValue.name, counterValue.value)
        }
      )
    ) {
      (dayNum, counterValues) =>
        dayCount.dayNum = dayNum
        dayCount.counterValues = counterValues
        dayCount
    } {
      dayCount =>
        println(">>>>>>>>>>>" + dayCount.counterValues)
        Some(dayCount.dayNum, dayCount.counterValues.toList)
    }
  ).fill(dayCount)

}
