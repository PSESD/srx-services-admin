package org.psesd.srx.services.admin.messages

import org.psesd.srx.shared.core.SrxResourceResult
import org.psesd.srx.shared.core.exceptions.ArgumentNullException
import org.psesd.srx.shared.core.sif.SifRequestAction.SifRequestAction
import org.psesd.srx.shared.core.sif.{SifCreateResponse, SifRequestAction}
import org.psesd.srx.shared.data.DatasourceResult

import scala.xml.Node

/** SRX Message operation response.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  * */
class MessageResult(requestAction: SifRequestAction, result: DatasourceResult) extends SrxResourceResult {
  if (requestAction == null) {
    throw new ArgumentNullException("request action")
  }

  if(result != null) {
    exceptions ++= result.exceptions
  }

  statusCode = {

    requestAction match {

      case SifRequestAction.Create =>
        if (success && result != null) {
          SifRequestAction.getSuccessStatusCode(requestAction)
        } else {
          500
        }

      case SifRequestAction.Query =>
        if (success && result != null && result.rows.nonEmpty) {
          SifRequestAction.getSuccessStatusCode(requestAction)
        } else {
          if (success && (result == null || result.rows.isEmpty)) {
            404
          } else {
            400
          }
        }
    }
  }

  def toXml: Option[Node] = {

    requestAction match {

      case SifRequestAction.Create =>
        Option(SifCreateResponse().addResult(result.id.getOrElse(""), statusCode).toXml)

      case SifRequestAction.Query =>
        if (statusCode == 200) {
          Option(<messages>{MessageService.getMessagesFromResult(result).map(m => m.toXml)}</messages>)
        } else {
          None
        }
    }
  }
}

object MessageResult {
  def apply(requestAction: SifRequestAction, result: DatasourceResult): MessageResult = new MessageResult(requestAction, result)
}
