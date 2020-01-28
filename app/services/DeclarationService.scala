package services

import com.google.inject.Inject
import connectors.TrustConnector
import models.http.DeclarationResponse
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class DeclarationService @Inject()(connector: TrustConnector) {

  def declareNoChange(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[DeclarationResponse] = {
    connector.declare(utr)
  }
}
