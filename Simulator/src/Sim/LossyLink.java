
package Sim;

import java.util.Random;

public class LossyLink extends Link{
	private SimEnt _connectorA=null;
	private SimEnt _connectorB=null;
	private int _dropPacketProbability;
	private int _packetsDropped = 0;
	
	private Random rand = new Random();
	
	// % chance to drop packets, normally from 0 to 100
	public LossyLink(int dropPacketProbability){
		super();
		_dropPacketProbability = dropPacketProbability;
	}
	
	// Connects the link to some simulation entity like
	// a node, switch, router etc.
	public void setConnector(SimEnt connectTo)
	{
		if (_connectorA == null) 
			_connectorA=connectTo;
		else
			_connectorB=connectTo;
	}
	
	public int getPacketsDropped()
	{
		return _packetsDropped;
	}
	
	// Called when a message enters the lossy link
	public void recv(SimEnt src, Event ev)
	{
		if (ev instanceof MessageTCP)
		{
			MessageTCP recMsg = ((MessageTCP) ev);
			System.out.println("Link receives message: "+recMsg.getMessageInfo());
			
			// Checks if the message is part of a three way handshake or FIN to grant drop immunity
			// Note: the ACK in the 3rd part of the handshake can still be dropped but
			//			it doesn't affect our solution
			
			// TODO: What about the FIN ACKs?
			
			if(recMsg.getSYNFlag() == 1 || recMsg.getFINFlag() == 1)
			{
				sendMessage(src, recMsg);
			}
			else if(rand.nextInt(100) < _dropPacketProbability)		// Calculates if the packet should be dropped or not
			{
				_packetsDropped++;
				System.out.println("Packet dropped");
			}
			else
			{
				sendMessage(src, recMsg);
			}
		}
	}
	
	private void sendMessage(SimEnt src, Event ev)
	{
		if (src == _connectorA)
		{
			send(_connectorB, ev, 0);
		}
		else
		{
			send(_connectorA, ev, 0);
		}
	}
}