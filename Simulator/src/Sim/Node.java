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
	private int _latestAckNr = -1;
	private boolean FINReceived = false;
	private ArrayList<MessageTCP> _msgBuffer = new ArrayList<MessageTCP>();
	private int _timeoutInterval = 10;
	
	// Lists used to verify that messages arrived
	private ArrayList<MessageTCP> _recMessages = new ArrayList<MessageTCP>();
	
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
	private NetworkAddr _toNode;
	
/*	Currently not used
	public void startSending(int network, int node, int number, int timeInterval, int startSeq)
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		
		send(this, new TimerEvent(), 0);	
	}
 */
	
	public void startSendingTCP(int network, int node, int number, int timeInterval)		// Send SYN
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		_toNode = new NetworkAddr(_toNetwork, _toHost);
		
//		_seq = rand.nextInt(101);
		_seq = 10;
		
		printSendMsgInfo("SYN", _seq, -1, _toNode);
		
		send(_peer, new MessageTCP(_id, _toNode, _seq, -1, 0, 1, 0), 0);
		_seq++;
	}
	
	public void printRecMessageList()
	{
		for(int i=0; i<_recMessages.size(); i++)
		{
			System.out.println(i+": "+_recMessages.get(i).getMessageInfo());
		}
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
				printSendMsgInfo("SEQ", _seq, _latestAckNr, _toNode);
				
				MessageTCP msg = new MessageTCP(_id, _toNode, _seq, _latestAckNr, 0, 0, 0);
				_msgBuffer.add(msg);
				send(_peer, msg, 0);
				
				send(this, new TimeoutResend(msg), _timeoutInterval);
				
				_seq++;
				_sentmsg++;
				
				send(this, new TimerEvent(), _timeBetweenSending);
			}
			else if(_sentmsg == _stopSendingAfter)
			{
				printSendMsgInfo("FIN1", _seq, _latestAckNr, _toNode);
				
				MessageTCP msg = new MessageTCP(_id, _toNode, _seq, _latestAckNr, 0, 0, 1);
				_msgBuffer.add(msg);
				send(_peer, msg, 0);
				
				send(this, new TimeoutResend(msg), _timeoutInterval);
				
				_seq++;
			}
			
/*			Currently not used
			if (_stopSendingAfter > _sentmsg)
			{
				_sentmsg++;
				send(_peer, new Message(_id, new NetworkAddr(_toNetwork, _toHost),_seq), 0);
				send(this, new TimerEvent(),_timeBetweenSending);
				System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" sent message with seq: "+_seq + " at time "+SimEngine.getTime());
				_seq++;
			}
 */
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
				printRecMsgInfo("SYN", recMsg.getSeqNr(), recMsg.getAckNr(), recMsg.source());
				
//				_seq = rand.nextInt(101) + 1000;
				_seq = 10000;
				_latestAckNr = recMsg.getSeqNr() + 1;
				NetworkAddr dest = recMsg.source();
				
				printSendMsgInfo("SYN-ACK", _seq, _latestAckNr, dest);
				
				send(_peer, new MessageTCP(_id, dest, _seq , _latestAckNr, 1, 1, 0), 0);
				
				_seq++;
			}
			else if (msgACKFlag == 1 && msgSYNFlag == 1 && msgFINFlag == 0)		// Receive SYN-ACK, Send SEQ, Start Sending
			{
				printRecMsgInfo("SYN-ACK", recMsg.getSeqNr(), recMsg.getAckNr(), recMsg.source());
				
				// Send ACK to finish 3-way handshake
				_latestAckNr = recMsg.getSeqNr() + 1;
				NetworkAddr dest = recMsg.source();
				send(_peer, new MessageTCP(_id, dest, _seq, _latestAckNr, 1, 0, 0), 0);
				
				// Not using the printSendMsgInfo method here because we print extra information
				System.out.println("-----------------------------------------------");
				System.out.println("Node "+_id.getString()+" sends ACK message with seq "+_seq+" and ack "+_latestAckNr+" to Node "+dest.getString()+" at time "+SimEngine.getTime()+" to finish three way handshake");
				System.out.println("-----------------------------------------------");
				System.out.println();
				
				// Start sending messages
				send(this, new TimerEvent(), _timeBetweenSending);
				
			}
			else if (msgACKFlag == 1 && msgSYNFlag == 0 && msgFINFlag == 0)		// Receive ACK
			{
				printRecMsgInfo("ACK", recMsg.getSeqNr(), recMsg.getAckNr(), recMsg.source());
				
				// Check every message in the buffer
				// If the sequence number is lower than the acknowledge number
				// 		it means that the message has reached the destination and
				//			is then removed from the buffer
				// If the sequence number is equal or higher than the acknowledge number
				//		re-send the message

				if(_msgBuffer.size() == 0)
				{
					System.out.println("Buffer is empty");
				}
				else
				{
					for(int i=0; i<_msgBuffer.size(); i++){
						if(recMsg.getAckNr() > _msgBuffer.get(i).getSeqNr())
						{
							System.out.println("Node "+_id.getString()+" removes message with seq "+_msgBuffer.get(i).getSeqNr()+" from buffer");
							_msgBuffer.remove(i);
							
							// Need to take a step back when an element is removed to not skip any element
							i--;
						}
						else
						{
							System.out.println("Node "+_id.getString()+" re-sends message with seq "+_msgBuffer.get(i).getSeqNr()+" to Node "+recMsg.source().getString()+" from buffer");
							
							MessageTCP msg = _msgBuffer.get(i);
							send(_peer, msg, 0);
						}
					}
					System.out.println("-----------------------------------------------");
					System.out.println();
				}
			}
			else if (msgACKFlag == 0 && msgSYNFlag == 0 && msgFINFlag == 0)		// Receive SEQ, Send ACK
			{	
				printRecMsgInfo("SEQ", recMsg.getSeqNr(), recMsg.getAckNr(), recMsg.source());
				
				NetworkAddr dest = recMsg.source();
				int recSeq = recMsg.getSeqNr();
				
				if(recSeq == _latestAckNr)
				{
					// Packet arrived in the correct position
					_recMessages.add(recMsg);
					
					// Assuming 1 byte of data per message (hence the +1)
					_latestAckNr = recSeq + 1;
				}
				else
				{
					// Packet arrived but in the wrong position
					// Discard packet and request re-sending of previous packets
					// (A better implementation could save the "future" packets until
					//		the missing packets arrive instead of discarding them)
					
					System.out.println("Node "+_id.getString()+" received seq "+recSeq+" when Node has ack "+_latestAckNr);
					System.out.println("Discarded packet: "+((MessageTCP) ev).getMessageInfo());
				}
				
				printSendMsgInfo("ACK", _seq, _latestAckNr, dest);
				send(_peer, new MessageTCP(_id, dest, _seq, _latestAckNr, 1, 0, 0), 0);
			}
			else if(msgACKFlag == 0 && msgSYNFlag == 0 && msgFINFlag == 1)		// Receive FIN1, send FINACK1
			{
				printRecMsgInfo("FIN1", recMsg.getSeqNr(), recMsg.getAckNr(), recMsg.source());
				
				NetworkAddr dest = recMsg.source();
				int recSeq  = recMsg.getSeqNr();
				
				if(recSeq == _latestAckNr)
				{
					// Packet arrived safely and in the correct position

					printSendMsgInfo("FIN1ACK", _seq, _latestAckNr+1, dest);
					send(_peer, new MessageTCP(_id, dest, _seq, _latestAckNr+1, 1, 0, 0), 0);
					
					if(FINReceived == false)
					{
						FINReceived = true;
						_recMessages.add(recMsg);
						
						MessageTCP fin2 = new MessageTCP(_id, dest, _seq, _latestAckNr+1, 1, 0, 1);
						printSendMsgInfo("FIN2", _seq, _latestAckNr+1, dest);
						_msgBuffer.add(fin2);
						send(_peer, fin2, _timeoutInterval);
						send(this, new TimeoutResend(fin2), _timeoutInterval);
					}
				}
			}
			else if(msgACKFlag == 1 && msgSYNFlag == 0 && msgFINFlag == 1)		// Receive FIN2, send FINACK2
			{
				if(FINReceived == false)
				{
					_recMessages.add(recMsg);
					FINReceived = true;
				}
				
				printRecMsgInfo("FIN2", recMsg.getSeqNr(), recMsg.getAckNr(), recMsg.source());
				
				NetworkAddr dest = recMsg.source();
				
				printSendMsgInfo("FIN2ACK", _seq, _latestAckNr+1, dest);
				send(_peer, new MessageTCP(_id, dest, _seq, _latestAckNr+1, 1, 0, 0), 0);
			}
			else
			{
				System.out.println("Something went wrong in Node "+_id.networkId()+"."+_id.nodeId());
			}
		}
		else if (ev instanceof TimeoutResend)
		{
			MessageTCP msg = ((TimeoutResend) ev).getMsg();
			
			// If the message is not in the buffer, it has been acknowledged -> do nothing
			// If the message is in the buffer, re-send it and queue up another TimeoutResend event
			
			/*
			if(_msgBuffer.size() > 0){
				System.out.println();
				System.out.println();
				System.out.println("___________________________________________________________________");
				for(int i=0; i<_msgBuffer.size(); i++)
				{
					System.out.println(i+ " - "+_msgBuffer.get(i).getMessageInfo());
				}
				System.out.println("___________________________________________________________________");
				System.out.println();
				System.out.println();
			}
			*/
			
			for(int i=0; i<_msgBuffer.size(); i++)
			{
				if(_msgBuffer.get(i) == msg)
				{
					send(_peer, msg, 0);
					send(this, new TimeoutResend(msg), _timeoutInterval);
					System.out.println("Timeout - Node "+_id.getString()+" re-sending message: "+msg.getMessageInfo());
					System.out.println();
					break;
				}
			}
		}
	}
	
	private void printSendMsgInfo(String msgType, int seq, int ack, NetworkAddr destNode)
	{
		System.out.println("-----------------------------------------------");
		System.out.println("Node "+_id.getString()+" sends "+msgType+" message with seq "+seq+" and ack "+ack+" to Node "+destNode.getString()+" at time "+SimEngine.getTime());
		System.out.println("-----------------------------------------------");
		System.out.println();
	}
	
	private void printRecMsgInfo(String msgType, int seq, int ack, NetworkAddr from)
	{
		System.out.println("-----------------------------------------------");
		System.out.println("Node "+_id.getString()+" receives "+msgType+" message with seq "+seq+" and ack "+ack+" from Node "+from.getString()+" at time "+SimEngine.getTime());
		System.out.println("-----------------------------------------------");
		System.out.println();
	}
}
