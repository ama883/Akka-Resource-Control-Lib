////////////////////////////////////////////////////////////////////////////////
//  Description :   This file defines the Cyberorg class.
//                  CyberOrgs is a model for hierarchical coordination of resource usage by multi-agent applications in a network of peer-owned resources. 
//  Author      :   Ahmed Abdel Moamen (ama883@mail.usask.ca)
//  Date        :   2016/06/27
//  Version     :   1.0   
////////////////////////////////////////////////////////////////////////////////

//
//  package
//
package Cyberorgs

//
//  import
//

import akka.actor.{ActorSystem, ActorLogging, Actor, Props, UntypedActor}
import akka.actor.ActorRef
import akka.event.Logging
import scala.collection.JavaConversions._
import java.util.ArrayList
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import akka.actor.Extension
import akka.actor.ActorSystem
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem


object CyberOrgExtension extends ExtensionId[CyberOrg] with ExtensionIdProvider {
  //The lookup method is required by ExtensionIdProvider,
  // so we return ourselves here, this allows us
  // to configure our extension to be loaded when
  // the ActorSystem starts up
  override def lookup = CyberOrgExtension
 
  //This method will be called by Akka to instantiate the Extension
  override def createExtension(system: ExtendedActorSystem) = new CyberOrg
 
  /**
   * Java API: retrieve the CyberOrg extension for the given system
   */
  override def get(system: ActorSystem): CyberOrg = super.get(system)
  
  
}

class CyberOrg () extends Extension {
  
//
//  parameters
//
  @volatile
  protected var m_lTicks : Long = 0 // resources in this cyberorg
  protected var m_lTicksCounter : Long = 0 // counter for the resources in this cyberorg
  protected var m_rateUnit: TimeUnit = TimeUnit.MILLISECONDS // the unit of resources in this cyberorg
  protected var m_name : String = "" // an optional name for the cyberorg
  protected var m_llActors: ArrayList[ActorRef] = null // list of actors in this cyberorg
  @volatile
  protected var m_bIsActive: Boolean = false // it is true if the cyberorg still has some ticks 
 
  /**
   * A secondary constructor
   * This constructor creates a new instance of CyberOrg with known resource specification
   */
  def this(p_name: String , p_lTicks:Long ,  p_rateUnit: TimeUnit) {
    this();
     //   Set the values of member variables
        m_name = p_name;
        m_lTicks = p_lTicks
        m_rateUnit = p_rateUnit;
        m_bIsActive = true;
        m_llActors = new ArrayList[ActorRef] ()
        
        // convert ticks to Nano seconds
        m_lTicks = TimeUnit.NANOSECONDS.convert(m_lTicks, p_rateUnit)
        m_lTicksCounter = m_lTicks
        // register the cyberorg into the CyberorgManager
        CyberorgManagerObject.getInstance().registerCyberorg(this)
  }
  
  /**
   * Create an instance of Cyberorg
   */
  def createCyberorg(p_name: String , p_lTicks:Long ,  p_rateUnit: TimeUnit): CyberOrg = 
    new CyberOrg(p_name, p_lTicks, p_rateUnit)
  
  /**
   * Create an instance of CyberOrgMessage
   */
  def createCyberOrgMessage(p_message: Any, p_receiver: ActorRef, p_executionTime: Long, p_unit: TimeUnit): CyberOrgMessage = 
    new CyberOrgMessage(p_message, p_receiver , p_executionTime, p_unit)
  
  
    /**
     *  Insert an actor to the actors list
     *  
     * @param p_anToInsert: The name of the actor which is added to the current actor list
     */
    def insertActor(p_anToInsert: ActorRef){
        m_llActors.add(p_anToInsert);
    }
    
     /** Reset the total number of ticks (resources) for this cyberorg
      */
    def resetTicks(){
        m_lTicksCounter = m_lTicks;
    }
    
   /**
    * Update the number ticks and deactivate the cyberorg if it ran out of resources 
    * Called by the CyberorgManager when reporting execution time by an actor belongs to this cyberorg
    * @p_time in nanosecnds
   */
    def consumeTicks(p_time: Long){
      synchronized{
        m_lTicksCounter -= p_time
      }   
     //   println("Cyberorg-consumeTicks: current ticks --> " + m_lTicks.toString())
    }
     
    /** 
     * Delete an Actor when it is distroied
     * 
     * @param p_anActor The name of the actor to be deleted
     */
    
    def deleteActor(p_anActor: ActorRef){
       m_llActors.remove(p_anActor);
    }
    
    /** 
     * Check if an Actor belongs to this cyberorg
     * Returns true if the actor is found
     * @param p_anActor: The name of the actor to be searched
     */
    def isActorFound(p_anActor: ActorRef): Boolean ={ 
      for( i <- 0 to m_llActors.size - 1){
            if(m_llActors(i).equals(p_anActor))
              return true
        }
      return false
    }
     
    
    /**
     * Returns the actor list
     * @return The actor list of the current cyberorg
     */
    def getActors(): ArrayList[ActorRef] ={
      return m_llActors
    }
    
    
     /**
     * Returns the amount of resources the current cyberorg has
     * @return The amount of resources
     */
    def getTicks(): Long ={
      return m_lTicks
    }
    
    /**
     * Returns the amount of resources the current cyberorg has
     * @return The amount of resources
     */
    def getTickCounter(): Long ={
      return m_lTicksCounter
    }
    
   /** 
     * Check weather the cyberorg is active or not
     * Returns true if the cyberorg is active
     */
    def isActive(): Boolean ={  
      synchronized{
      if(m_lTicksCounter <= 0)
           return false
      else
        return true
      }
    }
   
 
}

class CyberOrgMessage {
  
//  parameters
//
  private var message : Any = null 
  private var receiver : ActorRef = null 
  private var executionTime : Long = -1 // resources in this message
  private var unit: TimeUnit = TimeUnit.MILLISECONDS
  
  def this(p_message: Any, p_receiver: ActorRef, p_executionTime: Long, p_unit: TimeUnit)
  {
   this()
    message = p_message
    receiver = p_receiver
    unit = p_unit
    // convert resources to Nano seconds
    executionTime = TimeUnit.NANOSECONDS.convert(p_executionTime, unit)
  }
  
   /** 
     * Returns the message's content
     */
  def getMessage(): Any={
    return message
  }
  
  /** 
     * Returns the message's receiver
  */
  def getReceiver(): ActorRef={
    return receiver
  }
  
   /** 
     * Returns the message's execution time
     */
  def getExecutionTime(): Long={
    return executionTime
  }
  

}