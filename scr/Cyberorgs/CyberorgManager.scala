////////////////////////////////////////////////////////////////////////////////
//  Description :   This file defines the CyberorgManager class.     
//                  CyberorgManager keeps track of the cyberorgs in the system. 
//                  It is used by the dispatcher to update the remaining amount of resources in the cyberorg.
//  Author      :   Ahmed Abdel Moamen (ama883@mail.usask.ca)
//  Date        :   2016/06/27
//  Version     :   1.0   
////////////////////////////////////////////////////////////////////////////////

//
//  package.
//
package Cyberorgs

//
//  import
//

import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import java.util.ArrayList
import akka.actor.ActorRef
import scala.collection.JavaConverters._
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import scala.concurrent.duration.FiniteDuration
import java.io.File
import com.typesafe.config.ConfigFactory


object CyberorgManagerObject
{
  
  var m_instance: CyberorgManager = null;
 
  /**
     *  returns a singelton of CyberorgManager
     */
   def getInstance(): CyberorgManager= {
      if (m_instance == null) {
         m_instance = new CyberorgManager();
         // load the config file
         val myConfigFile = new File("src/resources/cyberorg.conf")
         val fileConfig = ConfigFactory.parseFile(myConfigFile).getConfig("cyberorg")
         val config = ConfigFactory.load(fileConfig)
         // load the interval time
          m_instance.m_interval = config.getLong("interval") 
    }
    return m_instance;
  }
   
  
 class CyberorgManager {
 
// 
//  parameters
//
   var m_lCyberorgs: ArrayList[CyberOrg] = null // list of cyberorgs in this ActorSystem
   var m_interval : Long = 1 // the time interval (rate) for all cyberorgs in the system
   var m_intervalUnit: TimeUnit = TimeUnit.SECONDS // the unit of resources in this cyberorg
   
  
   /**
     *  returns the interval
     */
   def getInterval(): Long =
   {
     return m_interval;
   }
   
   /**
     *  returns the interval unit
     */
   def getIntervalUnit(): TimeUnit =
   {
     return m_intervalUnit;
   }
   
    /**
     *  register a cyberorg
     * @param p_CyberOrg: The cyberorg which is added to the current cyberorg list
     */
   def registerCyberorg(p_CyberOrg: CyberOrg)
   {
      if (m_lCyberorgs == null)
      {
        m_lCyberorgs = new ArrayList[CyberOrg]()
      }
      m_lCyberorgs.add(p_CyberOrg)
   }
   
    /** 
     * Checks if an Actor has more ticks for execution
     * Called by the dispatcher before scheduling an actor for execution    
     * Returns true if the actor has can be scheduled 
     * @param p_anActor: The name of the actor to be scheduled
     */
   def isSchedullable(p_Actor: ActorRef): Boolean=
   {
//       println("CyberorgManager-isSchedullable: " + p_Actor.toString())
     if(m_lCyberorgs ne null)
     {
       for (cyber <- m_lCyberorgs.asScala) {
         // if the cyberorg which belongs to the actor is active, it returns true
            if(cyber.isActorFound(p_Actor))  
            {
              return cyber.isActive()
            }
       }
     }
     // return true if actor is not found to be belonging to any cyberorg
      return true 
   }
   
   
    /** 
     * Check if an Actor has more ticks for execution
     * Called by the dispatcher before scheduling an actor for execution    
     * Returns true if the actor has can be scheduled 
     * @param p_anActor: The name of the actor to be scheduled
     * @param executionTime: the amout of resources assigned to the actor
     */
   def isSchedullable(p_Actor: ActorRef, executionTime: Long): Boolean=
   {
     if(m_lCyberorgs ne null)
     {
       for (cyber <- m_lCyberorgs.asScala) {
         // if the cyberorg which belongs to the actor is active, it returns true
            if(cyber.isActorFound(p_Actor))  
            {
              if(cyber.getTickCounter() >= executionTime) 
              {
                reportExecutionTime(p_Actor, executionTime)
                return true
              }
              else
              {
                return false
              }
            }
       }
     }
     // return true if actor is not found to be belonging to any cyberorg
      return true 
   }
   
   
   /** 
     * Check if an Actor belongs to a cyberorg
     * Returns true if the actor is found
     * @param p_anActor: The name of the actor to be searched
     */
    def isActorFound(p_Actor: ActorRef): Boolean ={ 
     if(m_lCyberorgs ne null)
     {
       for (cyber <- m_lCyberorgs.asScala) {
         // if the cyberorg which belongs to the actor is active, it returns true
            if(cyber.isActorFound(p_Actor))  
            {
                return true
            }
       }
     }
     // return false if actor not found to be belonging to any cyberorg
      return false
    }
   
  /**
    * Update the number ticks of a cyberorg  
    * Called by the dispatcher in order to report an execution time by an actor belongs to this cyberorg
    * @p_time in nanosecnds
  */
   def reportExecutionTime(p_Actor: ActorRef, p_time: Long)
   {
     if(m_lCyberorgs ne null)
     {
       for (cyber <- m_lCyberorgs.asScala) {
            if(cyber.isActorFound(p_Actor))  
            {
              cyber.consumeTicks(p_time) // report execution time
            }
       }
     }
   }
   
   /**
    * reset the execution time of all cyberorgs in the actor system
  */
   def resetExecutionTime()
   {
     if(m_lCyberorgs ne null)
     {
       for (cyber <- m_lCyberorgs.asScala) {
           cyber.resetTicks()
           
       }
     }
   }
   
  
 }
 
}