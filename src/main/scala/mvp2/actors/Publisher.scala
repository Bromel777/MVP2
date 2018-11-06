package mvp2.actors

import akka.actor.{ActorRef, Props}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import mvp2.data.{KeyBlock, Transaction}
import scala.language.postfixOps

class Publisher extends CommonActor {

  var mempool: List[Transaction] = List.empty
  var lastKeyBlock: KeyBlock = KeyBlock()
  val testTxGenerator: ActorRef = context.actorOf(Props(classOf[TestTxGenerator]), "testTxGenerator")

  context.system.scheduler.schedule(10 second, 5 seconds)(createKeyBlock)

  override def specialBehavior: Receive = {
    case transaction: Transaction =>
      logger.info(s"Publisher received tx: $transaction.")
      mempool = transaction :: mempool
    case keyBlock: KeyBlock => lastKeyBlock = keyBlock
  }

  def createKeyBlock: KeyBlock = {
    val keyBlock: KeyBlock =
      KeyBlock(lastKeyBlock.height + 1, System.currentTimeMillis, lastKeyBlock.currentBlockHash, mempool)
    logger.info(s"New keyBlock with height ${keyBlock.height} is published by local publisher.")
    mempool = List.empty
    context.parent ! keyBlock
    self ! keyBlock
    keyBlock
  }

}