package controllers

import play.api.mvc.{Action, Controller}
import models.{Display, DisplayItem}
import play.api.data.Form
import play.api.data.Forms._
import widgets.{SprintTitle, BurndownChart}
import models.widgetConfigs.SprintTitleConfig

object DisplayItems extends Controller {
  def create(displayId: Long) = Action {
    implicit request =>
      Display.findById(displayId).map {
        display =>
          displayItemFrom(display).bindFromRequest.fold(
          formWithErrors => BadRequest, {
            displayItem =>
              displayItem.save
              Redirect(routes.Displays.showConfig(displayId))
          })
      }.getOrElse(NotFound)
  }

  def edit(displayId: Long, displayItemId: Long) = Action {
    DisplayItem.findById(displayItemId).map {
      displayItem =>
        Ok(views.html.displayItem.edit(displayItem, displayItemForm(displayItem)))
    }.getOrElse(NotFound)
  }

  def update(displayId: Long, displayItemId: Long) = Action {
    implicit request =>
      DisplayItem.findById(displayItemId).map {
        displayItem =>
          displayItemForm(displayItem).bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.displayItem.edit(displayItem, formWithErrors)), {
            displayItem =>
              displayItem.save
              Redirect(routes.Displays.showConfig(displayId))
          })
      }.getOrElse(NotFound)
  }

  def displayItemFrom(display: Display) =
    displayItemForm(new DisplayItem(0, display.id, 0, 0, 0, 0, 0, "{}"))

  def displayItemForm(displayItem: DisplayItem) = Form(
    mapping(
      "widget" -> number,
      "posx" -> number(min = 0),
      "posy" -> number(min = 0),
      "width" -> number(min = 0),
      "height" -> number(min = 0),
      "burndownChartConfig" -> optional(BurndownChart.configMapping),
      "sprintTitleConfig" -> optional(SprintTitle.configMapping)
    ) {
      (widget, posx, posy, width, height, burndownCharConfig, sprintTitleConfig) =>
        displayItem.widgetNum = widget
        displayItem.posx = posx
        displayItem.posy = posy
        displayItem.width = width
        displayItem.height = height
        displayItem.burndownChartConfig = burndownCharConfig
        displayItem.sprintTitleConfig = sprintTitleConfig
        displayItem
    } {
      displayItem =>
        Some(
          displayItem.widgetNum,
          displayItem.posx,
          displayItem.posy,
          displayItem.width,
          displayItem.height,
          displayItem.burndownChartConfig,
          displayItem.sprintTitleConfig)
    }
  ).fill(displayItem)
}
