package org.psesd.srx.services.admin.messages

import org.psesd.srx.shared.core.sif.SifMessageId
import org.psesd.srx.shared.data.{DataRow, DatasourceResult}

/** SRX Message operation response.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  **/
class MessageResult(val messageId: SifMessageId, rows: List[DataRow], exceptions: List[Exception]) extends DatasourceResult(rows, exceptions) {

}

object MessageResult {
  def apply(messageId: SifMessageId, result: DatasourceResult): MessageResult = new MessageResult(messageId, result.rows, result.exceptions)
}
