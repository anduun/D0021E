package Sim;

// Sends information about the Foreign Router to the Mobile Node
// Gives the Mobile Node a new Network ID to use on the Foreign Network

public class AgentAdvertisement implements Event{
	private int _newNetworkID;
	
	AgentAdvertisement(int newNetworkID)
	{
		 _newNetworkID = newNetworkID;
	}
	
	public int getNewNetworkID(){
		return _newNetworkID;
	}

	public void entering(SimEnt locale)
	{
	}
}



