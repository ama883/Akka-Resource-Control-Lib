////////////////////////////////////////////////////////////////////////////////
//  Description :   This file defines the TenantManager class.     
//                  TenantManager keeps track of the Tenants in the system. 
//                  It is used by the dispatcher to update the remaining amount of resources in the Tenant.
//  Author      :   Ahmed Abdel Moamen (ama883@mail.usask.ca)
//  Date        :   2016/06/27
//  Version     :   1.0   
////////////////////////////////////////////////////////////////////////////////

//
//  package.
//
package Tenants

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


object TenantManagerObject
{
  
  var m_instance: TenantManager = null;
 
  /**
     *  returns a singelton of TenantManager
     */
   def getInstance(): TenantManager= {
      if (m_instance == null) {
         m_instance = new TenantManager();
         // load the config file
         val myConfigFile = new File("src/resources/tenant.conf")
         val fileConfig = ConfigFactory.parseFile(myConfigFile).getConfig("tenant")
         val config = ConfigFactory.load(fileConfig)
         // load the interval time
          m_instance.m_interval = config.getLong("interval") 
    }
    return m_instance;
  }
   
  
 class TenantManager {
 
// 
//  parameters
//
   var m_lTenants: ArrayList[Tenant] = null // list of Tenants in this ActorSystem
   var m_interval : Long = 1 // the time interval (rate) for all Tenants in the system
   var m_intervalUnit: TimeUnit = TimeUnit.SECONDS // the unit of resources in this Tenant
   
  
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
     *  register a Tenant
     * @param p_Tenant: The Tenant which is added to the current Tenant list
     */
   def registerTenant(p_Tenant: Tenant)
   {
      if (m_lTenants == null)
      {
        m_lTenants = new ArrayList[Tenant]()
      }
      m_lTenants.add(p_Tenant)
   }
   
    /** 
     * Checks if an Actor has more ticks for execution
     * Called by the dispatcher before scheduling an actor for execution    
     * Returns true if the actor has can be scheduled 
     * @param p_anActor: The name of the actor to be scheduled
     */
   def isSchedullable(p_Actor: ActorRef): Boolean=
   {
//       println("TenantManager-isSchedullable: " + p_Actor.toString())
     if(m_lTenants ne null)
     {
       for (cyber <- m_lTenants.asScala) {
         // if the Tenant which belongs to the actor is active, it returns true
            if(cyber.isActorFound(p_Actor))  
            {
              return cyber.isActive()
            }
       }
     }
     // return true if actor is not found to be belonging to any Tenant
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
     if(m_lTenants ne null)
     {
       for (cyber <- m_lTenants.asScala) {
         // if the Tenant which belongs to the actor is active, it returns true
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
     // return true if actor is not found to be belonging to any Tenant
      return true 
   }
   
   
   /** 
     * Check if an Actor belongs to a Tenant
     * Returns true if the actor is found
     * @param p_anActor: The name of the actor to be searched
     */
    def isActorFound(p_Actor: ActorRef): Boolean ={ 
     if(m_lTenants ne null)
     {
       for (cyber <- m_lTenants.asScala) {
         // if the Tenant which belongs to the actor is active, it returns true
            if(cyber.isActorFound(p_Actor))  
            {
                return true
            }
       }
     }
     // return false if actor not found to be belonging to any Tenant
      return false
    }
   
  /**
    * Update the number ticks of a Tenant  
    * Called by the dispatcher in order to report an execution time by an actor belongs to this Tenant
    * @p_time in nanosecnds
  */
   def reportExecutionTime(p_Actor: ActorRef, p_time: Long)
   {
     if(m_lTenants ne null)
     {
       for (cyber <- m_lTenants.asScala) {
            if(cyber.isActorFound(p_Actor))  
            {
              cyber.consumeTicks(p_time) // report execution time
            }
       }
     }
   }
   
   /**
    * reset the execution time of all Tenants in the actor system
  */
   def resetExecutionTime()
   {
     if(m_lTenants ne null)
     {
       for (cyber <- m_lTenants.asScala) {
           cyber.resetTicks()
           
       }
     }
   }
   
  
 }
 
}