package com.olegych.scastie.balancer

import com.typesafe.config.Config
import akka.actor.ActorSelection
import com.olegych.scastie.api.ActorConnected
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.olegych.scastie.api.RunnerPing

abstract class BaseDispatcher[R, S](config: Config) extends Actor with ActorLogging {
  case class SocketAddress(host: String, port: Int)

  import context._
  
  private def getRemoteActorsPath(
    key: String,
    runnerName: String,
    actorName: String
  ): Map[SocketAddress, String] = {
    val host = config.getString(s"remote-$key-hostname")
    val portStart = config.getInt(s"remote-$key-ports-start")
    val portSize = config.getInt(s"remote-$key-ports-size")
    (0 until portSize).map(_ + portStart)
      .map(port => {
        val addr = SocketAddress(host, port)
        (addr, getRemoteActorPath(runnerName, addr, actorName))
      })
      .toMap
  }

  def getRemoteActorPath(
    runnerName: String,
    runnerAddress: SocketAddress,
    actorName: String
  ) = s"akka://$runnerName@${runnerAddress.host}:${runnerAddress.port}/user/$actorName"

  def connectRunner(path: String): ActorSelection = {
    val selection = context.actorSelection(path)
    selection ! ActorConnected
    selection
  }

  def getRemoteServers(
    key: String,
    runnerName: String,
    actorName: String
    ): Map[SocketAddress, ActorSelection] = {
      getRemoteActorsPath(key, runnerName, actorName).map {
        case (address, url) => (address, connectRunner(url))
      }
    }

  def ping(servers: List[ActorSelection]): Future[List[Boolean]] = {
    implicit val timeout: Timeout = Timeout(10.seconds)
    val futures = servers.map { s =>
        (s ? RunnerPing).map { _ =>
          log.info(s"pinged $s")
          true
        }.recover { e => 
          log.error(e, s"could not ping $s")
          false
        }
      }
    Future.sequence(futures)
  }
      
}
