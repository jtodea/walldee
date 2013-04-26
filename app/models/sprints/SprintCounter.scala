package models.sprints

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class SprintCounter(name: String, color: String, side: SprintCounterSide.Type)

object SprintCounter {

  implicit object SprintCounterFormat extends Format[SprintCounter] {
    override def reads(json: JsValue): JsResult[SprintCounter] =
      JsSuccess(SprintCounter(
        (json \ "name").as[String],
        (json \ "color").as[String],
        SprintCounterSide((json \ "side").as[Int])))

    override def writes(sprintCounter: SprintCounter): JsValue = JsObject(Seq(
      "name" -> JsString(sprintCounter.name),
      "color" -> JsString(sprintCounter.color),
      "side" -> JsNumber(sprintCounter.side.id)))
  }

}