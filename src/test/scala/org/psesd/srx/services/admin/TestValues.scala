package org.psesd.srx.services.admin

import org.psesd.srx.shared.core.config.Environment
import org.psesd.srx.shared.core.sif._

object TestValues {

  lazy val sifAuthenticationMethod = SifAuthenticationMethod.SifHmacSha256
  lazy val sessionToken = SifProviderSessionToken(Environment.getProperty(Environment.SrxAdminSessionTokenKey))
  lazy val sharedSecret = SifProviderSharedSecret(Environment.getProperty(Environment.SrxAdminSharedSecretKey))
  lazy val sifUrl: SifProviderUrl = SifProviderUrl("http://localhost:%s".format(Environment.getPropertyOrElse("SERVER_PORT", "80")))
  lazy val sifProvider = new SifProvider(sifUrl, sessionToken, sharedSecret, sifAuthenticationMethod)
  lazy val timestamp: SifTimestamp = SifTimestamp("2015-02-24T20:51:59.878Z")
  lazy val authorization = new SifAuthorization(sifProvider, timestamp)

}
