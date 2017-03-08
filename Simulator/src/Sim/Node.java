package Sim;

import java.util.Random;
// This class implements a node (host) it has an address, a peer that it communicates with
// and it count messages send and received.

public class Node extends SimEnt {
	private NetworkAddr _id;
	private SimEnt _peer;
	private int _sentmsg=0;
	private int _seq = 0;


	
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
	
	public void startSending(int network, int node, int number, int timeInterval, int startSeq)
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		send(this, new TimerEvent(),0);	
	}
	
	
	public void startSendingTCP(int network, int node, int number, int timeInterval)
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		int seqNr = rand.nextInt(101);
		System.out.println();
		System.out.println("Send SYN");
		System.out.println();
		send(this, new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), seqNr, -1 , 0 , 1 ,0), 0);
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
		else if (ev instanceof Message)
		{
			System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" receives message with seq: "+((Message) ev).seq() + " at time "+SimEngine.getTime());
		}
		else if (ev instanceof MessageTCP)
		{
			
			if (((MessageTCP) ev).getACKFlag() == 0 && ((MessageTCP) ev).getSYNFlag() == 1 ){
				System.out.println("-----------------------------------------------");
				System.out.println();
				System.out.println("SYN received");
				System.out.println("seqNr: " + ((MessageTCP) ev).getSeqNr());
				System.out.println();
				System.out.println("Send SYN-ACK");
				System.out.println();
				System.out.println("-----------------------------------------------");
				int seqNr = rand.nextInt(101) + 1000;
				int ackNr = ((MessageTCP) ev).getSeqNr() + 1;
				send(this, new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), seqNr ,ackNr, 1 , 1 ,0), 0);
			}
			else if (((MessageTCP) ev).getACKFlag() == 1 && ((MessageTCP) ev).getSYNFlag() == 1 ){
				System.out.println("-----------------------------------------------");
				System.out.println();
				System.out.println("SYN-ACK received");
				System.out.println("seqNr: " + ((MessageTCP) ev).getSeqNr());
				System.out.println("ackNr: " + ((MessageTCP) ev).getAckNr());
				System.out.println();
				System.out.println("Send ACK");
				System.out.println();
				System.out.println("-----------------------------------------------");
				int seqNr = ((MessageTCP) ev).getAckNr();
				int ackNr = ((MessageTCP) ev).getSeqNr() + 1;
				send(this, new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), seqNr, ackNr, 1 , 0 ,0), 0);
				
				
				System.out.println("-----------------------------------------------");
				System.out.println();
				System.out.println("Send FIN");
				System.out.println();
				System.out.println("-----------------------------------------------");
				send(this, new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), seqNr, ackNr, 0 , 0 ,1), 0);
			}
			else if(((MessageTCP) ev).getACKFlag() == 0 && ((MessageTCP) ev).getSYNFlag() == 0  && ((MessageTCP) ev).getFINFlag() == 1){
				System.out.println("-----------------------------------------------");
				System.out.println();
				System.out.println("FIN1 received");
				System.out.println("seqNr: " + ((MessageTCP) ev).getSeqNr());
				System.out.println("ackNr: " + ((MessageTCP) ev).getAckNr());
				System.out.println();
				System.out.println("Send ACK");
				System.out.println();
				System.out.println("Send FIN2");
				System.out.println();
				System.out.println("-----------------------------------------------");
				int ackNr = ((MessageTCP) ev).getSeqNr() + 1;
				int seqNr = ((MessageTCP) ev).getAckNr();
				send(this, new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), -1, ackNr, 1 , 0 ,0), 0);
				send(this, new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), seqNr, ackNr, 1 , 0 ,1), 0);
				
			}
			else if(((MessageTCP) ev).getACKFlag() == 1 && ((MessageTCP) ev).getSYNFlag() == 0 &&((MessageTCP) ev).getFINFlag() == 1){
				System.out.println("-----------------------------------------------");
				System.out.println();
				System.out.println("FIN2 received");
				System.out.println("seqNr: " + ((MessageTCP) ev).getSeqNr());
				System.out.println("ackNr: " + ((MessageTCP) ev).getAckNr());
				System.out.println();
				System.out.println("Send ACK");
				System.out.println();
				System.out.println("-----------------------------------------------");
				int ackNr = ((MessageTCP) ev).getSeqNr() + 1;
				send(this, new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), -1, ackNr, 1 , 0 ,0), 0);
				
			}
			
			else{
				if (((MessageTCP) ev).getSeqNr() == -1)
				{
					System.out.println("-----------------------------------------------");
					System.out.println();
					System.out.println("ACK received");	
					System.out.println("ackNr: " + ((MessageTCP) ev).getAckNr());
					System.out.println();
					System.out.println("-----------------------------------------------");
				}
				else{
					System.out.println("-----------------------------------------------");
					System.out.println();
					System.out.println("ACK received");
					System.out.println("seqNr: " + ((MessageTCP) ev).getSeqNr());
					System.out.println("ackNr: " + ((MessageTCP) ev).getAckNr());
					System.out.println();
					System.out.println("-----------------------------------------------");
				}
				
			}
		}

	}
}
