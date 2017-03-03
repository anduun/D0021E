package Sim;

// Used by the Node to nudge a Router into sending back a Agent Advertisement message

public class AgentSolicitation implements Event{
	private SimEnt _source;
	private int _newNetworkID;
	
	 AgentSolicitation(SimEnt source, int newNetworkID)
	{
		_source = source;
		_newNetworkID = newNetworkID;
	}
	
	public SimEnt source()
	{
		return _source;
	}
	
	public int getNewNetworkID()
	{
		return _newNetworkID;
	}

	public void entering(SimEnt locale)
	{
	}
}