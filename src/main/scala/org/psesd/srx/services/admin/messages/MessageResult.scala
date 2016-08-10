package org.psesd.srx.services.admin.messages

import org.psesd.srx.shared.data.DatasourceResult

/** SRX Message operation response.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  * */
class MessageResult(result: DatasourceResult) extends DatasourceResult(result.id, result.rows, result.exceptions) {

}

object MessageResult {
  def apply(result: DatasourceResult): MessageResult = new MessageResult(result)
}
