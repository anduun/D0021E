package Sim;

// This class implements a simple router

public class Router extends SimEnt{

	private RouteTableEntry[] _routingTable;
	private int _interfaces;
	private int _now = 0;
	private HomeAgentTableEntry[] _homeAgentTable;

	// When created, number of interfaces are defined
	Router(int interfaces)
	{
		_routingTable = new RouteTableEntry[interfaces];
		_interfaces=interfaces;
		_homeAgentTable = new HomeAgentTableEntry[interfaces];
	}
	
	// This method connects links to the router and also informs the 
	// router of the host connects to the other end of the link
	public void connectInterface(int interfaceNumber, SimEnt link, SimEnt node)
	{
		if (interfaceNumber<_interfaces)
		{
			_routingTable[interfaceNumber] = new RouteTableEntry(link, node);
		}
		else
		{
			System.out.println("Trying to connect to port not in router");
		}
		
		((Link) link).setConnector(this);
	}

	// This method searches for an entry in the routing table that matches
	// the network number in the destination field of a messages.
	// The link represents that network number is returned
	private SimEnt getInterface(int networkAddress)
	{
		SimEnt routerInterface=null;
		
		// Check Home Agent routing table
		for(int i=0; i<_interfaces; i++){
			if(_homeAgentTable[i] != null)
			{
				if(((Node) _routingTable[i].node()).getAddr().networkId() == networkAddress)
				{
					routerInterface = _routingTable[i].link();
					
					// Used for testing
					System.out.println();
					System.out.println("Found Node in Home Agent table!");
					System.out.println();
					
					// Has to return here because in our solution we don't
					// remove the Mobile Nodes from the Routing Table
					// (Which is done to keep the interface occupied)
					return routerInterface;
				}
			}
		}
		
		// Check Router routing table
		for(int i=0; i<_interfaces; i++)
			if (_routingTable[i] != null)
			{
				if (((Node) _routingTable[i].node()).getAddr().networkId() == networkAddress)
				{
					routerInterface = _routingTable[i].link();
				}
			}
		return routerInterface;
	}
	
	// Moves a connection from one interface to another
	public void updateInterface(NetworkAddr source, int newInterfaceIndex){
		for(int i=0; i<_interfaces; i++){
			if (_routingTable[i] != null){
				if(((Node) _routingTable[i].node()).getAddr() == source){
					RouteTableEntry rte = _routingTable[i];
					_routingTable[i] = null;
					_routingTable[newInterfaceIndex] = rte;
					System.out.println("Router moves Node "+source.networkId()+"."+source.nodeId()+" from interface "+i+" to "+newInterfaceIndex);
					break;
				}
			}
		}
	}
	
	// Add a Node to the Home Agent routing table
	private void addToHomeAgentTable(SimEnt node){
		for(int i=0; i<_interfaces; i++){
			if(_homeAgentTable[i] == null){
				System.out.println();
				System.out.println("Added a node to Home Agent Table index "+i);
				System.out.println();
				_homeAgentTable[i] = new HomeAgentTableEntry(null, node);
				break;
			}
		}
	}
	
	// Removes a Node from the Home Agent routing table
	private void removeFromHomeAgentTable(SimEnt node){
		for(int i=0; i<_interfaces; i++){
			if(_homeAgentTable[i] != null){		// Needed to avoid null pointer exceptions
				if(((Node) _homeAgentTable[i].node()) == node){
					System.out.println();
					System.out.println("Removed a node to Home Agent Table index "+i);
					System.out.println();
					_homeAgentTable[i] = null;
					break;
				}
			}
		}
	}
	
	// When messages are received at the router this method is called
	public void recv(SimEnt source, Event event)
	{
		if (event instanceof Message)
		{
			System.out.println("Router handles packet with seq: " + ((Message) event).seq()+" from node: "+((Message) event).source().networkId()+"." + ((Message) event).source().nodeId() );
			SimEnt sendNext = getInterface(((Message) event).destination().networkId());
			System.out.println("Router sends to node: " + ((Message) event).destination().networkId()+"." + ((Message) event).destination().nodeId());		
			send (sendNext, event, _now);
		}
		else if (event instanceof ChangeInterface)
		{
			updateInterface(((ChangeInterface) event).source(), ((ChangeInterface) event).newInterfaceNumber());
		}
		else if (event instanceof AgentSolicitation)
		{
			// Give the Node a Care-of Address, etc - we're not doing this
			// because the node can talk directly to it's own Home Agent

			System.out.println();
			System.out.println("A Router received a Agent Solicitation from Node "+((Node) ((AgentSolicitation) event).source()).getAddr().networkId()+"."+((Node) ((AgentSolicitation) event).source()).getAddr().networkId()+" at time "+SimEngine.getTime());
			System.out.println();
			
			send(((AgentSolicitation) event).source(), new AgentAdvertisement(), 0);
		}
		else if (event instanceof RegistrationRequest)
		{
			Node sourceNode = (Node) ((RegistrationRequest) event).source();
			
			System.out.println();
			System.out.println("A Router received a Registration Request from Node "+sourceNode.getAddr().networkId()+"."+sourceNode.getAddr().nodeId()+" at time "+SimEngine.getTime());
			System.out.println();
			
			// Add Node to Home Agent table
			addToHomeAgentTable(sourceNode);
			
			// Send a RegistrationReply back to Node
			send(sourceNode, new RegistrationReply(), 0);
		}
		else if (event instanceof Deregistration)
		{
			Node sourceNode = (Node) ((Deregistration) event).source();
			
			System.out.println();
			System.out.println("A Router received a Deregistration from Node "+sourceNode.getAddr().networkId()+"."+sourceNode.getAddr().nodeId()+" at time "+SimEngine.getTime());
			System.out.println();
			
			// Remove the Node from the HA table
			removeFromHomeAgentTable(sourceNode);
		}
//		else if (event instanceof Reregistration)
//		{
//			// Prolong the Lifetime if the Node stays at a foreign network for a longer time
//		}
	}
}
