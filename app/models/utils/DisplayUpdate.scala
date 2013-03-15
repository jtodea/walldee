package models.utils

import play.api.libs.json._
import play.api.libs.json.JsObject

case class DisplayUpdate(removeWidgets: Seq[String],
                         changedWidgets: Seq[RenderedWidget],
                         animationCycles: Int,
                         animationDelay: Int)

object DisplayUpdate {

  implicit object DisplayUpdateFormat extends Writes[DisplayUpdate] {

    override def writes(displayUpdate: DisplayUpdate): JsValue = JsObject(Seq(
      "removeWidgets" -> Json.toJson(displayUpdate.removeWidgets),
      "changedWidgets" -> Json.toJson(displayUpdate.changedWidgets),
      "animationCycles" -> JsNumber(displayUpdate.animationCycles),
      "animationDelay" -> JsNumber(displayUpdate.animationDelay)))
  }
}
