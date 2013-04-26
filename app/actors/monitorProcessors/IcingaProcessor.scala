package actors.monitorProcessors

import models.{StatusTypes, StatusValue, StatusMonitor}
import play.api.libs.ws.Response
import play.api.libs.json._
import models.statusValues._
import play.api.libs.ws.Response
import play.api.libs.ws.Response

case class IcingaHost(hostName: String,
                      hostStatus: String,
                      statusOk: Int,
                      statusWarning: Int,
                      statusUnknown: Int,
                      statusCritical: Int,
                      statusPending: Int)

object IcingaHost {

  implicit object IcingaHostReads extends Reads[IcingaHost] {
    override def reads(json: JsValue): JsResult[IcingaHost] =
      JsSuccess(IcingaHost(
        (json \ "host_name").as[String],
        (json \ "host_status").as[String],
        (json \ "services_status_ok").as[Int],
        (json \ "services_status_warning").as[Int],
        (json \ "services_status_unknown").as[Int],
        (json \ "services_status_critical").as[Int],
        (json \ "services_status_pending").as[Int]
      ))
  }

}

case class IcingaHostGroup(hostGroupName: String, members: Seq[IcingaHost])

object IcingaHostGroup {

  implicit object IcingaHostGroupReads extends Reads[IcingaHostGroup] {
    override def reads(json: JsValue): JsResult[IcingaHostGroup] =
      JsSuccess(IcingaHostGroup(
        (json \ "hostgroup_name").as[String],
        (json \ "members").as[Seq[IcingaHost]]
      ))
  }

}

case class IcingaOverviewStatus(hostgroups: Seq[IcingaHostGroup])

object IcingaOverviewStatus {

  implicit object IcingaStatusReads extends Reads[IcingaOverviewStatus] {
    override def reads(json: JsValue): JsResult[IcingaOverviewStatus] =
      JsSuccess(IcingaOverviewStatus(
        (json \ "hostgroup_overview").as[Seq[IcingaHostGroup]]
      ))
  }

}

case class IcingaOverview(status: IcingaOverviewStatus)

object IcingaOverview {

  implicit object IcingaOverviewReads extends Reads[IcingaOverview] {
    override def reads(json: JsValue): JsResult[IcingaOverview] =
      JsSuccess(IcingaOverview(
        (json \ "status").as[IcingaOverviewStatus]
      ))
  }

}

object IcingaProcessor extends MonitorProcessor {
  override def apiUrl(url: String): String = url match {
    case url if url.endsWith("&jsonoutput") => url
    case url => url + "&jsonoutput"
  }

  def process(statusMonitor: StatusMonitor, response: Response) {
    val icingaOverview = response.json.as[IcingaOverview]

    val expecteds = statusMonitor.icingaConfig.map(_.expected.map(expected => expected.host -> expected).toMap).getOrElse(Map.empty)
    var status = StatusTypes.Ok
    val icingaStatus = HostsStatus(icingaOverview.status.hostgroups.map {
      hostgroup =>
        HostsGroup(hostgroup.members.map {
          host =>
            val hostStatus = if (host.hostStatus == "UP") HostStatusTypes.Up else HostStatusTypes.Down
            val expected = expecteds.get(host.hostName)
            val serviceStatus = if (host.statusCritical > expected.map(_.criticals).getOrElse(0)) {
              status = StatusTypes.Failure
              HostServiceStatusTypes.Critical
            } else if (host.statusWarning > expected.map(_.warnings).getOrElse(0)) {
              status = StatusTypes.Failure
              HostServiceStatusTypes.Warning
            } else
              HostServiceStatusTypes.Ok
            HostStatus(host.hostName, hostStatus, serviceStatus)
        })
    })

    updateStatus(statusMonitor, status, Json.toJson(icingaStatus))
  }

}
