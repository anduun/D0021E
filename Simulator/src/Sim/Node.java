package Sim;

import java.util.Random;
// This class implements a node (host) it has an address, a peer that it communicates with
// and it count messages send and received.

public class Node extends SimEnt {
	private NetworkAddr _id;
	private SimEnt _peer;
	private int _sentmsg=0;
	private int _seq = 0;

	
	// Used for the sink class
	private int _messagesReceived = 0;
	
	// Home Agent variables
	private Router _homeAgent;
	private boolean onForeignNetwork = false;
	
	// TCP variables
	private Random rand = new Random();
	
	
	public Node(int network, int node)
	{
		super();
		_id = new NetworkAddr(network, node);
	}	
	
	
	// Sets the peer to communicate with. This node is single homed
	
	public void setPeer (SimEnt peer)
	{
		_peer = peer;
		
		if(_peer instanceof Link )
		{
			 ((Link) _peer).setConnector(this);
		}
	}
	
	
	public NetworkAddr getAddr()
	{
		return _id;
	}
	
//**********************************************************************************	
	// Just implemented to generate some traffic for demo.
	// In one of the labs you will create some traffic generators
	
	private int _stopSendingAfter = 0; //messages
	private int _timeBetweenSending = 10; //time between messages
	private int _toNetwork = 0;
	private int _toHost = 0;
	
	public void StartSending(int network, int node, int number, int timeInterval, int startSeq)
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		send(this, new TimerEvent(),0);	
	}
	
	// timeToMove is the sim time when the Node should move back to its home network
	public void moveBackHome(int timeToMove)
	{
		// Send Deregistration message to HA and update links etc
		send(_homeAgent, new Deregistration(this), timeToMove);
	}
	
	public void startSendingTCP(int network, int node, int number, int timeInterval)
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		int seqNr = rand.nextInt(101);
		int ackNr = rand.nextInt(11);
		_trafficDistributionType = 0;
		send(this, new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), seqNr, ackNr, 0 , 1 ), 0);
	}
	
//**********************************************************************************	
	
	// This method is called upon that an event destined for this node triggers.
	
	public void recv(SimEnt src, Event ev)
	{
		if (ev instanceof TimerEvent)
		{			
			if (_stopSendingAfter > _sentmsg)
			{
				_sentmsg++;
				send(_peer, new Message(_id, new NetworkAddr(_toNetwork, _toHost),_seq),0);
				send(this, new TimerEvent(),_timeBetweenSending);
				System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" sent message with seq: "+_seq + " at time "+SimEngine.getTime());
				_seq++;
			}
		}
		if (ev instanceof Message)
		{
			System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" receives message with seq: "+((Message) ev).seq() + " at time "+SimEngine.getTime());
			
		}
		else if (ev instanceof AgentAdvertisement)
		{
			
			System.out.println();
			System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" receives Agent Advertisement at time "+SimEngine.getTime());
			System.out.println("Change Network ID from "+_id.networkId()+" to "+((AgentAdvertisement) ev).getNewNetworkID());
			System.out.println("Send a Registration Request with old Network ID "+_id.networkId()+" to Home Agent");
			System.out.println();
			
			// oldNetworkID is used so the Node can identify itself at its Home Agent
			int oldNetworkID = _id.networkId();
			
			// Handle the AgentAdvertisement things (Care-of Address)
			_id = new NetworkAddr(((AgentAdvertisement) ev).getNewNetworkID(), _id.nodeId());
			
			// Send a Registration Request to Home Agent
			// Registration Request can include a Lifetime parameter (not included in our solution)
			send(_homeAgent, new RegistrationRequest(this, oldNetworkID), 0);
		}
		else if (ev instanceof RegistrationReply)
		{
			// Registration to Home Agent is ok
			onForeignNetwork = true;
			
			System.out.println();
			System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" received Registration Reply at time "+SimEngine.getTime());
			System.out.println();
			
			// Set Lifetime value (not included in our solution)
		}
		else if (ev instanceof DeregistrationReply)
		{
			System.out.println();
			System.out.println("Node "+_id.networkId()+"."+_id.nodeId()+" received Deregistration Reply at time "+SimEngine.getTime());
			System.out.println("Change Network ID from "+_id.networkId()+" to "+((DeregistrationReply) ev).getOldNetworkID());
			System.out.println();
			_id = new NetworkAddr(((DeregistrationReply) ev).getOldNetworkID(), _id.nodeId());
			onForeignNetwork = false;
		}
		else if	(ev instanceof MessageTCP)
		{
			System.out.println("TCP HYPE");
			
		}
	}
}
