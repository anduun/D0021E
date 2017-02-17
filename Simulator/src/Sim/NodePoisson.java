package Sim;

// This class implements a node (host) it has an address, a peer that it communicates with
// and it count messages send and received.

public class NodePoisson extends SimEnt {
	private NetworkAddr _id;
	private SimEnt _peer;
	private int _sentmsg=0;
	private int _seq = 0;
	
	public NodePoisson (int network, int node)
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
	private double _lambda;			// average number of events per interval
	
	public void StartSending(int network, int node, int number, int timeInterval, int startSeq, double lambda)
	{
		_stopSendingAfter = number;
		_timeBetweenSending = timeInterval;
		_toNetwork = network;
		_toHost = node;
		_seq = startSeq;
		_lambda = lambda;
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
				
				// Sends the next message in _timeBetweenSending and a Poisson generated time interval
				int timeBetweenSending = _timeBetweenSending + generatePoisson(_lambda);
				
				send(this, new TimerEvent(),timeBetweenSending);
				System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" sent message with seq: "+_seq + " at time "+SimEngine.getTime());
				_seq++;
			}
		}
		if (ev instanceof Message)
		{
			System.out.println("Node "+_id.networkId()+ "." + _id.nodeId() +" receives message with seq: "+((Message) ev).seq() + " at time "+SimEngine.getTime());
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
