package models

import play.api.libs.json.Json
import widgetConfigs._
import play.api.db._
import play.api.Play.current

import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

import org.scalaquery.ql.extended.H2Driver.Implicit._

import org.scalaquery.session.{Database, Session}
import org.scalaquery.ql.{SimpleFunction, Query}
import scala.Some
import globals.Global

case class DisplayItem(id: Option[Long],
                       displayId: Long,
                       posx: Int,
                       posy: Int,
                       width: Int,
                       height: Int,
                       styleNum: Int,
                       widgetNum: Int,
                       projectId: Option[Long],
                       appearsInFeed: Boolean,
                       animationCycles: Option[String],
                       widgetConfigJson: String) {

  def this() = this(None, 0, 0, 0, 0, 0, 0, 0, None, false, None, "{}")

  def widget: DisplayWidgets.Type = DisplayWidgets(widgetNum)

  def style: DisplayStyles.Type = DisplayStyles(styleNum)

  def widgetConfig = Json.parse(widgetConfigJson)

  def burndownConfig = {
    if (widget == DisplayWidgets.Burndown) {
      Some(Json.fromJson[BurndownConfig](widgetConfig))
    } else {
      None
    }
  }

  def sprintTitleConfig = {
    if (widget == DisplayWidgets.SprintTitle) {
      Some(Json.fromJson[SprintTitleConfig](widgetConfig))
    } else {
      None
    }
  }

  def clockConfig = {
    if (widget == DisplayWidgets.Clock) {
      Some(Json.fromJson[ClockConfig](widgetConfig))
    } else {
      None
    }
  }

  def alarmsConfig = {
    if (widget == DisplayWidgets.Alarms) {
      Some(Json.fromJson[AlarmsConfig](widgetConfig))
    } else {
      None
    }
  }

  def iframeConfig = {
    if (widget == DisplayWidgets.IFrame) {
      Some(Json.fromJson[IFrameConfig](widgetConfig))
    } else {
      None
    }
  }

  def buildStatusConfig = {
    if (widget == DisplayWidgets.BuildStatus) {
      Some(Json.fromJson[BuildStatusConfig](widgetConfig))
    } else {
      None
    }
  }

  def hostStatusConfig = {
    if (widget == DisplayWidgets.HostStatus) {
      Some(Json.fromJson[HostStatusConfig](widgetConfig))
    } else {
      None
    }
  }

  def metricsConfig = {
    if (widget == DisplayWidgets.Metrics) {
      Some(Json.fromJson[MetricsConfig](widgetConfig))
    } else {
      None
    }
  }

  def insert = {
    val insertedId = DisplayItem.database.withSession {
      implicit db: Session =>
        DisplayItem.insert(this)
        Query(DisplayItem.seqID).first
    }
    val result = DisplayItem(Some(insertedId), displayId, posx, posy, width, height, styleNum, widgetNum,
      projectId, appearsInFeed, None, widgetConfigJson)
    Global.displayUpdater ! result
    result
  }

  def update = {
    DisplayItem.database.withSession {
      implicit db: Session =>
        DisplayItem.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    DisplayItem.database.withSession {
      implicit db: Session =>
        DisplayItem.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object DisplayItem extends Table[DisplayItem]("DISPLAYITEM") {
  lazy val seqID = SimpleFunction.nullary[Long]("identity")

  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O PrimaryKey, O AutoInc)

  def displayId = column[Long]("DISPLAYID", O NotNull)

  def posx = column[Int]("POSX", O NotNull)

  def posy = column[Int]("POSY", O NotNull)

  def width = column[Int]("WIDTH", O NotNull)

  def height = column[Int]("HEIGHT", O NotNull)

  def styleNum = column[Int]("STYLENUM", O NotNull)

  def widgetNum = column[Int]("WIDGETNUM", O NotNull)

  def projectId = column[Long]("PROJECTID", O Nullable)

  def appearsInFeed = column[Boolean]("APPEARSINFEED", O NotNull)

  def animationCycles = column[String]("ANIMATIONCYCLES")

  def widgetConfigJson = column[String]("WIDGETCONFIGJSON", O NotNull)

  def * = id.? ~ displayId ~ posx ~ posy ~ width ~ height ~ styleNum ~ widgetNum ~ projectId.? ~ appearsInFeed ~
    animationCycles.? ~ widgetConfigJson <>((apply _).tupled, unapply _)

  def formApply(id: Option[Long],
                displayId: Long,
                posx: Int,
                posy: Int,
                width: Int,
                height: Int,
                styleNum: Int,
                widgetNum: Int,
                projectId: Option[Long],
                appearsInFeed: Boolean,
                burndownChartConfig: Option[BurndownConfig],
                sprintTitleConfig: Option[SprintTitleConfig],
                clockConfig: Option[ClockConfig],
                alarmsConfig: Option[AlarmsConfig],
                iframeConfig: Option[IFrameConfig],
                buildStatusConfig: Option[BuildStatusConfig],
                hostStatusConfig: Option[HostStatusConfig],
                metricsConfig: Option[MetricsConfig]): DisplayItem = {

    val widgetConfig = DisplayWidgets(widgetNum) match {
      case DisplayWidgets.Burndown => Json.toJson(burndownChartConfig.getOrElse(BurndownConfig()))
      case DisplayWidgets.SprintTitle => Json.toJson(sprintTitleConfig.getOrElse(SprintTitleConfig()))
      case DisplayWidgets.Clock => Json.toJson(clockConfig.getOrElse(ClockConfig()))
      case DisplayWidgets.Alarms => Json.toJson(alarmsConfig.getOrElse(AlarmsConfig()))
      case DisplayWidgets.IFrame => Json.toJson(iframeConfig.getOrElse(IFrameConfig()))
      case DisplayWidgets.BuildStatus => Json.toJson(buildStatusConfig.getOrElse(BuildStatusConfig()))
      case DisplayWidgets.HostStatus => Json.toJson(hostStatusConfig.getOrElse(HostStatusConfig()))
      case DisplayWidgets.Metrics => Json.toJson(metricsConfig.getOrElse(MetricsConfig()))
    }

    DisplayItem(id, displayId, posx, posy, width, height, styleNum, widgetNum, projectId, appearsInFeed,
      None, Json.stringify(widgetConfig))
  }

  def formUnapply(displayItem: DisplayItem) =
    Some(
      displayItem.id,
      displayItem.displayId,
      displayItem.posx,
      displayItem.posy,
      displayItem.width,
      displayItem.height,
      displayItem.styleNum,
      displayItem.widgetNum,
      displayItem.projectId,
      displayItem.appearsInFeed,
      displayItem.burndownConfig,
      displayItem.sprintTitleConfig,
      displayItem.clockConfig,
      displayItem.alarmsConfig,
      displayItem.iframeConfig,
      displayItem.buildStatusConfig,
      displayItem.hostStatusConfig,
      displayItem.metricsConfig)

  def query = Query(this)

  def findAllForDisplay(displayId: Long): Seq[DisplayItem] = database.withSession {
    implicit db: Session =>
      query.where(d => d.displayId === displayId).orderBy(id.asc).list
  }

  def findAllForDisplayFeed(displayId: Long): Seq[DisplayItem] = database.withSession {
    implicit db: Session =>
      query.where(d => d.displayId === displayId && d.appearsInFeed).orderBy(id.asc).list
  }

  def findById(displayItemId: Long): Option[DisplayItem] = database.withSession {
    implicit db: Session =>
      query.where(d => d.id === displayItemId).firstOption
  }

}