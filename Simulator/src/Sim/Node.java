package Sim;

// This class implements a node (host) it has an address, a peer that it communicates with
// and it count messages send and received.

public class Node extends SimEnt {
	private NetworkAddr _id;
	private SimEnt _peer;
	private int _sentmsg = 0;
	private int _seq = 0;
	
	// Used for the sink class
	private int _messagesReceived = 0;
	
	public Node (int network, int node)
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
	
	public int getMessagesSent()
	{
		return _sentmsg;
	}
	
	public int getMessagesReceived()
	{
		return _messagesReceived;
	}
	
//**********************************************************************************
	// Just implemented to generate some traffic for demo.
	// In one of the labs you will create some traffic generators
	
	private int _stopSendingAfter = 0; //messages
	private int _timeBetweenSending = 10; //time between messages
	private int _toNetwork = 0;
	private int _toHost = 0;
	private int _trafficDistributionType = 0; // 0 = CBR, 1 = Gaussian, 2 = Poisson
	private double _lambda;			// average number of events per interval, used for Poisson
	private int changeInterfaceAfterPackets = -1;
	private int newInterfaceNumber = -1;
	
	public void startSendingCBR(int network, int node, int number, int timeInterval, int startSeq)
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		_trafficDistributionType = 0;
		send(this, new TimerEvent(),0);
	}
	
	public void startSendingGaussian(int network, int node, int number, int timeInterval, int startSeq)
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		_trafficDistributionType = 1;
		send(this, new TimerEvent(),0);
	}
	
	public void startSendingPoisson(int network, int node, int number, int timeInterval, int startSeq, double lambda)
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		_trafficDistributionType = 2;
		_lambda = lambda;
		send(this, new TimerEvent(),0);
	}
	
	public void changeInterface(int interfaceNumber, int packetsSent){
		changeInterfaceAfterPackets = packetsSent;
		newInterfaceNumber = interfaceNumber;
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
				
				double timeBetweenSending = 0;
				
				if(_trafficDistributionType == 0){				// CBR
					timeBetweenSending = _timeBetweenSending;
				} else if(_trafficDistributionType == 1) {		// Gaussian
					timeBetweenSending = _timeBetweenSending + generateGaussian(5,1);
				} else if(_trafficDistributionType == 2) {		// Poisson
					timeBetweenSending = _timeBetweenSending + generatePoisson(_lambda);
				}
				
				send(this, new TimerEvent(), timeBetweenSending);
				System.out.println("Node "+_id.networkId()+"."+_id.nodeId()+" sent message with seq: "+_seq + " at time "+SimEngine.getTime());
				_seq++;
				
				if(_sentmsg == changeInterfaceAfterPackets){
					System.out.println("Node "+_id.networkId()+"."+_id.nodeId()+" requests change interface to interface number "+newInterfaceNumber+" at time "+SimEngine.getTime());
					send(_peer, new ChangeInterface(_id, newInterfaceNumber), 0);
				}
			}
		}
		if (ev instanceof Message)
		{
			_messagesReceived++;
			System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" receives message with seq: "+((Message) ev).seq() + " at time "+SimEngine.getTime());
			
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
	
	// amount is amount of numbers generated
	// used for testing Gaussian distribution generation
	// paste results into excel to see the curve
	public void printGaussianDistribution(int amount)
	{
		// Change result into a string that fits in swedish version of Google Sheets
		for(int i = 0;i<=amount;i++){
			String g = "=" + String.valueOf(generateGaussian(5,1));
		      g = g.replace(".", ",");
		      g = g.replaceAll("E", "*10^");
		      System.out.println(g);
		}
	}
	
	// Knuth's algorithm to generate Poisson distribution
	// https://en.wikipedia.org/wiki/Poisson_distribution#Generating_Poisson-distributed_random_variables
	// http://stackoverflow.com/questions/1241555/algorithm-to-generate-poisson-and-binomial-random-numbers
	private int generatePoisson(double lambda){
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;
		
		do {
			k++;
			p = p * Math.random();
		} while (p > L);
		
		return k - 1;
	}
	
	// amount is amount of numbers generated
	// used for testing Poisson distribution generation
	// paste results into excel to see the curve
	private void printPoissonDistribution(double lambda, int amount){
		for(int i = 0; i < amount; i++){
			System.out.println(generatePoisson(lambda));
		}
	}	
}
