package Sim;

// This class represent a routing table for the Home Agent
// by including the node connected to the Home Agent
// Link is not used in this case because our solution
// connects the Node directly to the Home Agent

public class HomeAgentTableEntry extends TableEntry{

	HomeAgentTableEntry(SimEnt link, SimEnt node)
	{
		super(link, node);
	}
	
	public SimEnt link()
	{
		return super.link();
	}

	public SimEnt node()
	{
		return super.node();
	}
	
}
