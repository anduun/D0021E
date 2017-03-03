package Sim;

// TODO: clean this
// This class represent a routing table for the Home Agent
// by including the node connected to the Home Agent
// Link is not used in this case because our solution
// connects the Node directly to the Home Agent

public class HomeAgentTableEntry{
	int networkID;
	SimEnt node;
	
	HomeAgentTableEntry(int networkID, SimEnt node)
	{
		this.networkID = networkID;
		this.node = node;
	}
	
	public int getNetworkID()
	{
		return networkID;
	}

	public SimEnt getNode()
	{
		return node;
	}
	
}
