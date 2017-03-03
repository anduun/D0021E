package Sim;

// Used when a Node registers its position at its Home Agent

public class RegistrationRequest implements Event{
	private SimEnt _source;
	private int _oldNetworkID;
	
	RegistrationRequest (SimEnt source, int oldNetworkID)
	{
		_source = source;
		_oldNetworkID = oldNetworkID;
	}
	
	public SimEnt source()
	{
		return _source;
	}
	
	public int getOldNetworkID()
	{
		return _oldNetworkID;
	}

	public void entering(SimEnt locale)
	{
	}
}