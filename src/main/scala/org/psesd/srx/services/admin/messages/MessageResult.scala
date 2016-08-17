package org.psesd.srx.services.admin.messages

import org.psesd.srx.shared.core.SrxResourceResult
import org.psesd.srx.shared.core.exceptions.ArgumentNullException
import org.psesd.srx.shared.core.sif.SifRequestAction.SifRequestAction
import org.psesd.srx.shared.core.sif.{SifCreateResponse, SifHttpStatusCode, SifRequestAction}
import org.psesd.srx.shared.data.DatasourceResult

import scala.xml.Node

/** SRX Message operation response.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  * */
class MessageResult(requestAction: SifRequestAction, httpStatusCode: Int, result: DatasourceResult) extends SrxResourceResult {
  if (requestAction == null) {
    throw new ArgumentNullException("request action")
  }

  statusCode = httpStatusCode

  if (result != null) {
    exceptions ++= result.exceptions
  }

  def toXml: Option[Node] = {

    requestAction match {

      case SifRequestAction.Create =>
        Option(SifCreateResponse().addResult(result.id.getOrElse(""), statusCode).toXml)

      case SifRequestAction.Query =>
        if (statusCode == SifHttpStatusCode.Ok) {
          Option(<messages>{MessageService.getMessagesFromResult(result).map(m => m.toXml)}</messages>)
        } else {
          None
        }

      case _ =>
        None
    }
  }
}

object MessageResult {
  def apply(requestAction: SifRequestAction, httpStatusCode: Int, result: DatasourceResult): MessageResult = new MessageResult(requestAction, httpStatusCode, result)
}
