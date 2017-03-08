
package Sim;

import java.util.Random;

public class LossyLink extends Link{
	private SimEnt _connectorA=null;
	private SimEnt _connectorB=null;
	private double _delay;
	private int _delayRange;
	private double _jitter;
	private int _dropPacketProbability;
	private double _prevTransit = 0;
	private int packetsDropped = 0;
	
	private Random rand = new Random();
	
	// delay range in time units from 0 to delay range, used to randomize delay
	// start jitter in time units
	// % chance to drop packet
	
	public LossyLink(int delayRange, int startJitter, int dropPacketProbability){
		super();
		_delayRange = delayRange;
		_jitter = startJitter;
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
	
	// Called when a message enters the lossy link
	
	public void recv(SimEnt src, Event ev)
	{
		if (ev instanceof Message)
		{
			System.out.println("Link recv msg, passes it through");
			
			// Calculates if the packet should be dropped or not
			
			if(rand.nextInt(100) < _dropPacketProbability)
			{
				packetsDropped++;
				System.out.println("Packet dropped. Total packets dropped: "+packetsDropped);
			}
			else
			{
				
				// If the packet isn't dropped, calculate the jitter,
				// randomize delay and forward the message
				
				calculateJitter(SimEngine.getTime(), ((Message) ev).getTimeSent());
				_delay = rand.nextInt(_delayRange);
				
				if (src == _connectorA)
				{
					send(_connectorB, ev, _delay+_jitter);
				}
				else
				{
					send(_connectorA, ev, _delay+_jitter);
				}
			}
		}
	}
	
	// Calculates jitter according RFC 1889
	private void calculateJitter(double timeArrival, double timeSent)
	{
		//System.out.println("Arrival "+timeArrival+" Sent "+timeSent);
		double transit = timeArrival - timeSent;	// Arrival Time - Time Sent
		//System.out.println("Transit "+transit);
		double deviation = transit - _prevTransit;
		_prevTransit = transit;
		if(deviation < 0)
		{
			deviation = -deviation;
		}
		_jitter = _jitter + ((1./16.) * (deviation - _jitter));
		//System.out.println("Jitter: " + _jitter);
	}
	
}