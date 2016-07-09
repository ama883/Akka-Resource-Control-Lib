////////////////////////////////////////////////////////////////////////////////
//  Description :   This is the main file. It describes how to use Tenant through a simple example of resource allocation
//                  to messages 
//  Author      :   Ahmed Abdel Moamen (ama883@mail.usask.ca)
//  Date        :   2016/06/27
//  Version     :   1.0   
////////////////////////////////////////////////////////////////////////////////

package Tenants

import akka.actor._
import akka.actor.Actor._
import scala.collection.JavaConversions._
import scala.util._
import java.util._
import java.util.concurrent._
import scala.concurrent._
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.dispatch.MessageDispatcher
import akka.dispatch.Dispatcher
import akka.dispatch.Mailbox
import java.io.File


import akka.dispatch.Dispatcher

object main extends scala.App {
  
 // load the config file
  val myConfigFile = new File("src/resources/tenant.conf")
  val fileConfig = ConfigFactory.parseFile(myConfigFile).getConfig("tenant")
  val config = ConfigFactory.load(fileConfig)

  val  actorSystem: ActorSystem = ActorSystem("default", config)
   
  val heavyActor = actorSystem.actorOf(Props[HeavyActor])
  val echoActor1 = actorSystem.actorOf(Props[EchoActor1])
  val echoActor2 = actorSystem.actorOf(Props[EchoActor2])
  val echoActor3 = actorSystem.actorOf(Props[EchoActor3])
  

   var cyber1: Tenant = TenantExtension(actorSystem).createTenant("tenant1", 100, TimeUnit.MILLISECONDS)
   var cyber2: Tenant = TenantExtension(actorSystem).createTenant("tenant2", 300, TimeUnit.MILLISECONDS)

  cyber1.insertActor(echoActor1)
  cyber1.insertActor(echoActor2)
  cyber1.insertActor(echoActor3)
  
  cyber2.insertActor(heavyActor)
  
  val r = scala.util.Random
  

  for {i <- 1 to 10} {
    heavyActor !  new TenantMessage(i, heavyActor, 50, TimeUnit.MILLISECONDS)
  }
  
   for {i <- 1 to 10} {
    echoActor1 !  TenantExtension(actorSystem).createTenantMessage(i, echoActor1, r.nextInt(50), TimeUnit.MILLISECONDS)
    echoActor2 !  TenantExtension(actorSystem).createTenantMessage(i, echoActor2, r.nextInt(50), TimeUnit.MILLISECONDS)
    echoActor3 !  TenantExtension(actorSystem).createTenantMessage(i, echoActor3, r.nextInt(50), TimeUnit.MILLISECONDS)
 
  }
  
  
}
 
 class HeavyActor extends Actor {
    def receive = {
      
      case m: TenantMessage => {  
        var before: Long = System.currentTimeMillis();
        var after: Long = before;
        while (after - before < 50) {
          after = System.currentTimeMillis();
        }
        println("HeavyActor --> " + m.getMessage() + ", " + after) 
      }
      
    }
  }
 
class EchoActor1 () extends Actor {
  
   def receive = {
     case m: TenantMessage => println("EchoActor1 --> " + m.getMessage() + ",  " + m.getExecutionTime())
  } 
}

class EchoActor2 () extends Actor {
  
   def receive = {
     case m: TenantMessage => println("EchoActor2 --> " + m.getMessage() + ",  " + m.getExecutionTime)
  } 
}

class EchoActor3 () extends Actor {
  
   def receive = {
     case m: TenantMessage => println("EchoActor3 --> " + m.getMessage() + ",  " + m.getExecutionTime)
  } 
}


