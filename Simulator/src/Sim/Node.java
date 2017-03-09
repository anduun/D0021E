package Sim;

import java.util.ArrayList;
import java.util.Random;
// This class implements a node (host) it has an address, a peer that it communicates with
// and it count messages send and received.

public class Node extends SimEnt {
	private NetworkAddr _id;
	private SimEnt _peer;
	private int _sentmsg = 0;
	private int _seq = 0;

	// TCP variables
	private boolean allowedToStartSending = false;
	private int _latestSeqNr = -1;
	private int _latestAckNr = -1;
	private ArrayList<MessageTCP> _msgBuffer = new ArrayList<MessageTCP>();
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
	
//	public void startSending(int network, int node, int number, int timeInterval, int startSeq)
//	{
//		_stopSendingAfter = number;
//		_timeBetweenSending = timeInterval;
//		_toNetwork = network;
//		_toHost = node;
//		_seq = startSeq;
//		
//		send(this, new TimerEvent(), 0);	
//	}
	
	public void startSendingTCP(int network, int node, int number, int timeInterval)		// Send SYN
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		allowedToStartSending = true;
		
//		_seq = rand.nextInt(101);
		_seq = 10;
		
		System.out.println("-----------------------------------------------");
		System.out.println("Node "+_id.getString()+" sends SYN message with seq "+_seq+" to Node "+_toNetwork+"."+_toHost);
		System.out.println("-----------------------------------------------");
		System.out.println();
		
		send(_peer, new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), _seq, -1, 0, 1, 0), 0);
		_seq++;
	}
	
//**********************************************************************************	

	// This method is called upon that an event destined for this node triggers.

	public void recv(SimEnt src, Event ev)
	{
		if (ev instanceof TimerEvent)
		{
			
			// Every time TimerEvent fires, a new message is created and added to the FIFO Queue (buffer)
			// If all messages are sent, add FIN message to buffer
			
			if(_sentmsg < _stopSendingAfter)
			{
				
				System.out.println("Node "+_id.getString()+" sends SEQ message with seq "+_seq+" and ack * to Node "+_toNetwork+"."+_toHost);
				System.out.println();
				
//				System.out.println("-----------------------------------------------");
//				System.out.println();
//				System.out.println("SEQ Sent from "+_id.getString());
//				System.out.println("seqNr: " + _seq);
//				System.out.println("ackNr: *");
//				System.out.println();
//				System.out.println("-----------------------------------------------");
				
				MessageTCP msg = new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), _seq, -1, 0, 0, 0);
				_msgBuffer.add(msg);
				send(_peer, msg, 0);
				
				_seq++;
				_sentmsg++;
				
				send(this, new TimerEvent(), _timeBetweenSending);
			}
			
			// Currently not used
//			if (_stopSendingAfter > _sentmsg)
//			{
//				_sentmsg++;
//				send(_peer, new Message(_id, new NetworkAddr(_toNetwork, _toHost),_seq), 0);
//				send(this, new TimerEvent(),_timeBetweenSending);
//				System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" sent message with seq: "+_seq + " at time "+SimEngine.getTime());
//				_seq++;
//			}
		}
		else if (ev instanceof Message)
		{
			System.out.println("Node "+_id.getString()+" receives message with seq: "+((Message) ev).seq() + " at time "+SimEngine.getTime());
		}
		else if (ev instanceof MessageTCP)
		{
			MessageTCP recMsg = ((MessageTCP) ev);
			int msgACKFlag = recMsg.getACKFlag();
			int msgSYNFlag = recMsg.getSYNFlag();
			int msgFINFlag = recMsg.getFINFlag();

			if (msgACKFlag == 0 && msgSYNFlag == 1 && msgFINFlag == 0)			// Receive SYN, Send SYN-ACK
			{
				
				System.out.println("-----------------------------------------------");
				System.out.println("Node "+_id.getString()+" receives SYN message with seq "+recMsg.getSeqNr()+" from Node "+recMsg.source().getString());
				System.out.println("-----------------------------------------------");
				System.out.println();
				
//				System.out.println("-----------------------------------------------");
//				System.out.println();
//				System.out.println("SYN received by "+_id.getString());
//				System.out.println("seqNr: " + recMsg.getSeqNr());
//				System.out.println();
//				System.out.println("Send SYN-ACK from "+_id.getString());
//				System.out.println();
//				System.out.println("-----------------------------------------------");
				
//				_seq = rand.nextInt(101) + 1000;
				_seq = 10000;
				_latestAckNr = recMsg.getSeqNr() + 1;
				NetworkAddr dest = recMsg.source();
				
				System.out.println("-----------------------------------------------");
				System.out.println("Node "+_id.getString()+" sends SYN-ACK message with seq "+_seq+" and ack "+_latestAckNr+" to Node "+dest.getString());
				System.out.println("-----------------------------------------------");
				System.out.println();
				
				send(_peer, new MessageTCP(_id, dest, _seq , _latestAckNr, 1, 1, 0), 0);
			}
			else if (msgACKFlag == 1 && msgSYNFlag == 1 && msgFINFlag == 0)		// Receive SYN-ACK, Send SEQ, Start Sending
			{
				
				System.out.println("-----------------------------------------------");
				System.out.println("Node "+_id.getString()+" receives SYN-ACK with seq "+recMsg.getSeqNr()+" and ack "+recMsg.getAckNr()+" from Node "+recMsg.source().getString());
				System.out.println("-----------------------------------------------");
				System.out.println();
				
//				System.out.println("-----------------------------------------------");
//				System.out.println();
//				System.out.println("SYN-ACK received by "+_id.getString());
//				System.out.println("seqNr: " + recMsg.getSeqNr());
//				System.out.println("ackNr: " + recMsg.getAckNr());
//				System.out.println();
//				System.out.println("Send ACK from "+_id.getString());
//				System.out.println();
//				System.out.println("-----------------------------------------------");
				
				// Send ACK to finish 3-way handshake
				_latestAckNr = recMsg.getSeqNr() + 1;
				NetworkAddr dest = recMsg.source();
				send(_peer, new MessageTCP(_id, dest, _seq, _latestAckNr, 1, 0, 0), 0);
				
				System.out.println("-----------------------------------------------");
				System.out.println("Node "+_id.getString()+" sends ACK with seq "+_seq+" and ack "+_latestAckNr+" to Node "+dest.getString()+" to finish three way handshake");
				System.out.println("-----------------------------------------------");
				System.out.println();
				
				// Start sending messages
				send(this, new TimerEvent(), _timeBetweenSending);
				
			}
			else if (msgACKFlag == 1 && msgSYNFlag == 0 && msgFINFlag == 0)		// Receive ACK
			{
				
				System.out.println("-----------------------------------------------");
				System.out.println("Node "+_id.getString()+" receives ACK with ack "+recMsg.getAckNr()+" from Node "+recMsg.source().getString());
				System.out.println("-----------------------------------------------");
				System.out.println();
				
//				System.out.println("-----------------------------------------------");
//				System.out.println();
//				System.out.println("ACK received by "+_id.getString());
//				System.out.println("seqNr: " + recMsg.getSeqNr());
//				System.out.println("ackNr: " + recMsg.getAckNr());
//				System.out.println();
//				System.out.println("-----------------------------------------------");
				
				if(_msgBuffer.size() == 0)
				{
					System.out.println("Buffer is empty");
				}
				else
				{
					if(_msgBuffer.get(0).getSeqNr() + 1 == recMsg.getAckNr())
					{
						System.out.println("Remove seq #"+_msgBuffer.get(0).getSeqNr()+" from buffer.");
						_msgBuffer.remove(0);
					}
					else
					{
						for(int i=0; i < _msgBuffer.size(); i++)
						{
							MessageTCP msg = _msgBuffer.get(i);
							send(_peer, msg, 0);
						}
					}
				}
				
				// Check first position in FIFO Queue
				// If Sequence number matches - remove from Queue
				// If Sequence number does not match - re-send message
			}
			else if (msgACKFlag == 0 && msgSYNFlag == 0 && msgFINFlag == 0)		// Receive SEQ, Send ACK
			{
				System.out.println("-----------------------------------------------");
				System.out.println("Node "+_id.getString()+" receives SEQ message with seq "+recMsg.getSeqNr()+" from Node "+recMsg.source().getString());
				System.out.println("-----------------------------------------------");
				System.out.println();
				
//				System.out.println("-----------------------------------------------");
//				System.out.println();
//				System.out.println("SEQ received by "+_id.getString());
//				System.out.println("seqNr: " + recMsg.getSeqNr());
//				System.out.println("ackNr: " + recMsg.getAckNr());
//				System.out.println();
//				System.out.println("-----------------------------------------------");
				
				NetworkAddr dest = recMsg.source();
				int recSeq = recMsg.getSeqNr();
				if(recSeq == _latestAckNr)
				{
					_latestAckNr++;
					send(_peer, new MessageTCP(_id, dest, -1, _latestAckNr, 1, 0, 0), 0);
					//
					System.out.println("-----------------------------------------------");
					System.out.println("Node "+_id.getString()+" sends ACK message with ack "+_latestAckNr+" to Node "+dest.getString());
					System.out.println("-----------------------------------------------");
					System.out.println();
					
//					System.out.println("-----------------------------------------------");
//					System.out.println();
//					System.out.println("ACK sent from "+_id.getString());
//					System.out.println("seqNr: *");
//					System.out.println("ackNr: " + _latestAckNr);
//					System.out.println();
//					System.out.println("-----------------------------------------------");
					//
				}
				else if(recSeq > _latestAckNr)
				{
					// Discard packet?
					send(_peer, new MessageTCP(_id, dest, -1, _latestAckNr, 1, 0, 0), 0);
					//
					System.out.println("-----------------------------------------------");
					System.out.println("Node "+_id.getString()+" sends ACK message with ack "+_latestAckNr+" to Node "+dest.getString());
					System.out.println("-----------------------------------------------");
					System.out.println();
					
//					System.out.println("-----------------------------------------------");
//					System.out.println();
//					System.out.println("ACK sent from "+_id.getString());
//					System.out.println("seqNr: *");
//					System.out.println("ackNr: " + _latestAckNr);
//					System.out.println();
//					System.out.println("-----------------------------------------------");
					//
				}
				else
				{
					System.out.println(_latestAckNr);
					System.out.println("Packet discarded - "+((MessageTCP) ev).getMessageInfo());		// Print packet/message info maybe
				}
				
//				_seq = ((MessageTCP) ev).getAckNr();
//				_latestAckNr = ((MessageTCP) ev).getSeqNr() + 1;
//				send(_peer, new MessageTCP(_id, dest, _seq, _latestAckNr, 1, 0, 0), 0);
			}
			else if(msgACKFlag == 1 && msgSYNFlag == 0 && msgFINFlag == 1)
			{
				System.out.println("-----------------------------------------------");
				System.out.println();
				System.out.println("FIN2 received");
				System.out.println("ackNr: " + ((MessageTCP) ev).getAckNr());
				System.out.println("seqNr: " + ((MessageTCP) ev).getSeqNr());
				System.out.println();
				System.out.println("Send ACK");
				System.out.println();
				System.out.println("-----------------------------------------------");
				int ackNr = ((MessageTCP) ev).getSeqNr() + 1;
				send(this, new MessageTCP(_id, new NetworkAddr(_toNetwork, _toHost), -1, ackNr, 1 , 0 ,0), 0);
			}
			else
			{
				System.out.println("Something went wrong in Node "+_id.networkId()+"."+_id.nodeId());
			}
		}

	}
}
