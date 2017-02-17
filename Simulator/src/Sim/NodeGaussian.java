
package Sim;


// This class implements a node (host) it has an address, a peer that it communicates with
// and it count messages send and received.

public class NodeGaussian extends SimEnt {
	private NetworkAddr _id;
	private SimEnt _peer;
	private int _sentmsg=0;
	private int _seq = 0;
	
	public NodeGaussian (int network, int node)
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
				// Sends the next message in _timeBetweenSending and a Gaussian generated time interval
				send(this, new TimerEvent(),_timeBetweenSending + generateGaussian(5,1));
				System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" sent message with seq: "+_seq + " at time "+SimEngine.getTime());
				_seq++;
			}
		}
		if (ev instanceof Message)
		{
			System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" receives message with seq: "+((Message) ev).seq() + " at time "+SimEngine.getTime());
			
		}
	}
	
	//Test function for Gaussian distribution
	public static void test()
	{
		for(int i = 0;i<=10000;i++){
			String xd = "=" + String.valueOf(generateGaussian(5,1));
		      xd = xd.replace(".", ",");
		      xd = xd.replaceAll("E", "*10^");
		      System.out.println(xd);
		}
	}
	
	//Box-Muller algorithm used to generate Gaussian (normal) distribution
	// http://en.wikipedia.org/wiki/Box-Muller_transform
	//http://www.cs.princeton.edu/courses/archive/fall09/cos126/assignments/StdGaussian.java.html
	public static double generateGaussian(double mu, double sigmaSquared)
	{		
	      double r, x, y;
	      double sigma = Math.sqrt(sigmaSquared);
	      
	      // find a uniform random point (x, y) inside unit circle
	      do {
	         x = 2.0 * Math.random() - 1.0;
	         y = 2.0 * Math.random() - 1.0;
	         r = x*x + y*y;
	      } while (r > 1 || r == 0);    // loop executed 4 / pi = 1.273.. times on average
	                                    
	  
	      // apply the Box-Muller formula to get standard Gaussian z    
	      double z = x * Math.sqrt(-2.0 * Math.log(r) / r);
	      z = z * sigma + mu;

	      return z;	
	}	
}