package models

import play.api.db._
import play.api.Play.current
import slick.driver.H2Driver.simple._
import globals.Global
import scala.slick.session.{Database, Session}

case class Display(id: Option[Long],
                   name: String,
                   projectId: Option[Long],
                   teamId: Option[Long],
                   styleNum: Int,
                   refreshTime: Int,
                   useLongPolling: Boolean,
                   relativeLayout: Boolean,
                   animationConfigJson: String) {

  def this() = this(None, "", None, None, 0, 5, false, false, "{}")

  def style: DisplayStyles.Type = DisplayStyles(styleNum)

  def insert = Display.database.withSession {
    implicit db: Session =>
      Display.insert(this)
  }

  def update = {
    Display.database.withSession {
      implicit db: Session =>
        Display.where(_.id === id).update(this)
    }
    Global.displayUpdater ! this
  }

  def delete = {
    Display.database.withSession {
      implicit db: Session =>
        Display.where(_.id === id).delete
    }
    Global.displayUpdater ! this
  }
}

object Display extends Table[Display]("DISPLAY") {
  lazy val database = Database.forDataSource(DB.getDataSource())

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME", O.NotNull)

  def projectId = column[Long]("PROJECTID", O.NotNull)

  def teamId = column[Long]("TEAMID")

  def styleNum = column[Int]("STYLENUM", O.NotNull)

  def refreshTime = column[Int]("REFRESHTIME", O.NotNull)

  def useLongPolling = column[Boolean]("USELONGPOLLING")

  def relativeLayout = column[Boolean]("RELATIVELAYOUT")

  def animationConfig = column[String]("ANIMATIONCONFIG")

  def * = id.? ~ name ~ projectId.? ~ teamId.? ~ styleNum ~ refreshTime ~ useLongPolling ~
    relativeLayout ~ animationConfig <>((apply _).tupled, unapply _)

  def query = Query(this)

  def findAll: Seq[Display] = database.withSession {
    implicit db: Session =>
      query.sortBy(d => d.name.asc).list
  }

  def findAllOther(displayId:Long): Seq[Display] = database.withSession {
    implicit  db:Session =>
      query.where(d => d.id =!= displayId).sortBy(d => d.name.asc).list
  }

  def findAllForProject(projectId: Long): Seq[Display] = database.withSession {
    implicit db: Session =>
      query.where(d => d.projectId === projectId).sortBy(d => d.name.asc).list
  }

  def findAllForTeam(teamId: Long): Seq[Display] = database.withSession {
    implicit db: Session =>
      query.where(d => d.teamId === teamId).sortBy(d => d.name.asc).list
  }

  def findById(displayId: Long): Option[Display] = database.withSession {
    implicit db: Session =>
      query.where(d => d.id === displayId).firstOption
  }
}
