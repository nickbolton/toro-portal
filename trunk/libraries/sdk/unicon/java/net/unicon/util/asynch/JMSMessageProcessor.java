package net.unicon.util.asynch;
import java.io.Serializable;
import javax.jms.Message;

/** 
* Interface for Message Processors
* @author unicon@unicon.net
* @version $Revision: 1.2 $
*/
public interface JMSMessageProcessor {
    /** REturens the name of this processor
    * @return name
    */
    public String getName();
    /** Processes message
    * @param message to be processed
    */
    public void processMessage(Message message) ;
}

/*
* 
* $Log: JMSMessageProcessor.java,v $
* Revision 1.2  2002/11/11 17:40:53  chavan
* Made processMEssage take a Message
*
* Revision 1.1  2002/09/24 18:58:46  chavan
*  Message Subscribption code
*
*/
