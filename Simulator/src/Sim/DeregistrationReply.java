package Sim;

// Used when a Router replies to a Deregistration event sent by a Node
// Sends information about the Nodes "new" network ID (the same as it used before it moved)

public class DeregistrationReply implements Event{
	private int _oldNetworkID;
	
	DeregistrationReply(int oldNetworkID)
	{
		_oldNetworkID = oldNetworkID;
	}
	
	public int getOldNetworkID()
	{
		return _oldNetworkID;
	}

	public void entering(SimEnt locale)
	{
	}
}