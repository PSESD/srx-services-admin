package org.psesd.srx.services.admin.config

import com.amazonaws.services.s3.model.AmazonS3Exception
import org.json4s.JValue
import org.psesd.srx.shared.core.exceptions.{AmazonS3UnauthorizedException, ArgumentInvalidException, ArgumentNullException, ArgumentNullOrEmptyOrWhitespaceException, SrxResourceNotFoundException}
import org.psesd.srx.shared.core.extensions.TypeExtensions._
import org.psesd.srx.shared.core.io.AmazonS3Client
import org.psesd.srx.shared.core.sif.SifRequestAction.SifRequestAction
import org.psesd.srx.shared.core.sif.{SifHttpStatusCode, SifRequestAction, SifRequestParameter, _}
import org.psesd.srx.shared.core.{SrxResource, SrxResourceErrorResult, SrxResourceResult, SrxResourceService}

import scala.xml.Node

/** Represents a ZoneConfig entity.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  */
class ZoneConfig(val id: String, val zoneConfig: String) extends SrxResource {
  if (id.isNullOrEmpty) {
    throw new ArgumentNullOrEmptyOrWhitespaceException("id parameter")
  }
  if (zoneConfig.isNullOrEmpty) {
    throw new ArgumentNullOrEmptyOrWhitespaceException("zoneConfig parameter")
  }

  def toJson: JValue = {
    toXml.toJsonStringNoRoot.toJson
  }

  def toXml: Node = {
    zoneConfig.toXml
  }

}

/** Represents a ZoneConfig method result.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  */
class ZoneConfigResult(
                  requestAction: SifRequestAction,
                  httpStatusCode: Int,
                  id: String,
                  zoneConfig: String
                ) extends SrxResourceResult {
  statusCode = httpStatusCode

  def toJson: Option[JValue] = {
    requestAction match {

      case SifRequestAction.Create =>
        None

      case SifRequestAction.Delete =>
        None

      case SifRequestAction.Query =>
        if (statusCode == SifHttpStatusCode.Ok) {
          val sb = new StringBuilder("[")
          sb.append(zoneConfig.toJson.toJsonString)
          sb.append("]")
          Some(sb.toString.toJson)
        } else {
          None
        }

      case SifRequestAction.Update =>
        None

      case _ =>
        None
    }
  }

  def toXml: Option[Node] = {

    requestAction match {

      case SifRequestAction.Create =>
        None

      case SifRequestAction.Delete =>
        None

      case SifRequestAction.Query =>
        if (statusCode == SifHttpStatusCode.Ok) {
          Option(zoneConfig.toXml)
        } else {
          None
        }

      case SifRequestAction.Update =>
        None

      case _ =>
        None
    }
  }
}

/** ZoneConfig methods.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  */
object ZoneConfig extends SrxResourceService {
  def apply(zoneConfig: String): ZoneConfig = {
    if (zoneConfig == null) {
      throw new ArgumentNullException("zoneConfig parameter")
    }
    ZoneConfig(zoneConfig.toXml, None)
  }

  def apply(zoneConfigXml: Node, parameters: Option[List[SifRequestParameter]]): ZoneConfig = {
    if (zoneConfigXml == null) {
      throw new ArgumentNullException("zoneConfigXml parameter")
    }
    val rootElementName = zoneConfigXml.label.toLowerCase
    if (rootElementName != "zone") {
      throw new ArgumentInvalidException("root element '%s'".format(rootElementName))
    }
    val name = (zoneConfigXml \ "@name").textOption.getOrElse("")
    new ZoneConfig(
      name,
      zoneConfigXml.toXmlString
    )
  }

  def create(resource: SrxResource, parameters: List[SifRequestParameter]): SrxResourceResult = {
    SrxResourceErrorResult(SifHttpStatusCode.InternalServerError, new Exception("ZoneConfig CREATE method not implemented."))
  }

  def delete(parameters: List[SifRequestParameter]): SrxResourceResult = {
    SrxResourceErrorResult(SifHttpStatusCode.InternalServerError, new Exception("ZoneConfig DELETE method not implemented."))
  }

  def query(parameters: List[SifRequestParameter]): SrxResourceResult = {
    val id = getKeyIdFromRequestParameters(parameters)
    if (id.isEmpty || id.get == "-1") {
      SrxResourceErrorResult(SifHttpStatusCode.BadRequest, new ArgumentInvalidException("id parameter"))
    } else {
      try {
        val zoneConfig = getZoneConfig(id.get)
        if (zoneConfig.isDefined) {
          new ZoneConfigResult(SifRequestAction.Query, SifRequestAction.getSuccessStatusCode(SifRequestAction.Query), id.get, zoneConfig.get)
        } else {
          SrxResourceErrorResult(SifHttpStatusCode.NotFound, new SrxResourceNotFoundException("ZoneConfig"))
        }
      } catch {
        case e: Exception =>
          SrxResourceErrorResult(SifHttpStatusCode.InternalServerError, e)
      }
    }
  }

  def update(resource: SrxResource, parameters: List[SifRequestParameter]): SrxResourceResult = {
    SrxResourceErrorResult(SifHttpStatusCode.InternalServerError, new Exception("ZoneConfig UPDATE method not implemented."))
  }

  protected def getKeyIdFromRequestParameters(parameters: List[SifRequestParameter]): Option[String] = {
    var result: Option[String] = None
    try {
      val id = getIdFromRequestParameters(parameters)
      if (id.isDefined) {
        result = Some(id.get)
      }
    } catch {
      case e: Exception =>
        result = Some("-1")
    }
    result
  }

  private def getZoneConfig(zoneId: String): Option[String] = {
    var zoneConfig: String = null

    try {
      val fileName = "%s_config.xml".format(zoneId)
      val s3Client = AmazonS3Client()
      if (s3Client.fileExists(fileName)) {
        zoneConfig = s3Client.download(fileName)
      }
      s3Client.shutdown
    } catch {
      case s3: AmazonS3Exception =>
        s3.getErrorCode match {
          case "403 Forbidden" =>
            throw new AmazonS3UnauthorizedException
          case _ =>
            throw s3
        }

      case ex: Exception =>
        throw ex
    }

    if(zoneConfig.isNullOrEmpty) {
      None
    } else {
      Some(zoneConfig)
    }
  }

}
