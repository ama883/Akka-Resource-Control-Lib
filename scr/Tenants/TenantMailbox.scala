////////////////////////////////////////////////////////////////////////////////
//  Description :   This file defines the custom mailbox for cyberorg's message. 
//  Author      :   Ahmed Abdel Moamen (ama883@mail.usask.ca)
//  Date        :   2016/01/13
//  Version     :   1.0   
////////////////////////////////////////////////////////////////////////////////

package Cyberorgs

import akka.actor.{ ActorRef, ActorSystem }
import com.typesafe.config.Config
import akka.dispatch.{Envelope, MessageQueue}
import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.mutable.PriorityQueue
import akka.dispatch.sysmsg.Suspend
import akka.dispatch.sysmsg.Suspend
import akka.dispatch.Dispatcher

class CyberorgMailbox extends akka.dispatch.MailboxType {

  // This constructor signature must exist, it will be called by Akka
  def this(settings: ActorSystem.Settings, config: Config) = this()

  // The create method is called to create the MessageQueue
  final override def create(owner: Option[ActorRef], system: Option[ActorSystem]): MessageQueue =
    new MessageQueue {
    
      private final val queue = new PriorityQueue[Envelope]()

      final def cleanUp(owner: ActorRef, deadLetters: MessageQueue): Unit =
      {
              while (hasMessages) {
                  deadLetters.enqueue(owner, dequeue())
              }
      }
      
      def enqueue(receiver: ActorRef, handle: Envelope): Unit =
      { 
         queue.enqueue(handle)
      }
      
      def dequeue(): Envelope = 
      {
       return queue.dequeue()
      }
      
      def numberOfMessages: Int = 
      {
        queue.size
      }
      
      def hasMessages: Boolean = 
      {
        !queue.isEmpty
      }
 
    }
}
