////////////////////////////////////////////////////////////////////////////////
//  Description :   This file defines the custom mailbox for cyberorg's message
//  Author      :   Ahmed Abdel Moamen (ama883@mail.usask.ca)
//  Date        :   2016/06/27
//  Version     :   1.0   
////////////////////////////////////////////////////////////////////////////////

//
//  package
//
package Tenants


//
//  import
//
import akka.actor.{ ActorRef, ActorSystem }
import com.typesafe.config.Config
import akka.dispatch.{Envelope, MessageQueue}
import java.util.concurrent.ConcurrentLinkedQueue
import akka.dispatch.sysmsg.Suspend
import akka.dispatch.sysmsg.Suspend
import akka.dispatch.Dispatcher
import scala.collection.mutable.PriorityQueue
import java.util.ArrayList

class CyberorgMailbox extends akka.dispatch.MailboxType {

  // This constructor signature must exist, it will be called by Akka
  def this(settings: ActorSystem.Settings, config: Config) = this()

  // The create method is called to create the MessageQueue
  final override def create(owner: Option[ActorRef], system: Option[ActorSystem]): MessageQueue =
    new MessageQueue {
    
     // randomly access queue
      private final val queue = new PriorityQueue[Envelope]()

      /**
     *  clean up the messages from the queue
     *  
     * @param owner: The owner of the queue
     * @param deadLetters: dead letter's message queue 
     */
      final def cleanUp(owner: ActorRef, deadLetters: MessageQueue): Unit =
      {
              while (hasMessages) {
                  deadLetters.enqueue(owner, dequeue())
              }
      }
      
      /**
     *  insert an envelope into the queue
     *  
     * @param receiver: the receiver of the message
     * @param handle: the envelope to be inserted
     */
      def enqueue(receiver: ActorRef, handle: Envelope): Unit =
      { 
         queue.enqueue(handle)
      }
      
       /**
     *  dequeue an envelope from the queue
     */
      def dequeue(): Envelope = 
      {
        var envelope: Envelope = queue.dequeue
        return envelope
    }
      
       /**
     *  return the queue size
     */
      def numberOfMessages: Int = 
      {
        queue.size
      }
      
      
       /**
     *  check if the queue is empty
     */
      def hasMessages: Boolean = 
      {
        !queue.isEmpty
      }
 
    }
}
