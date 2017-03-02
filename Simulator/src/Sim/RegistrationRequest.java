package Sim;

// Used when a Node registers its position at its Home Agent

public class RegistrationRequest implements Event{
	private SimEnt _source;
	
	RegistrationRequest (SimEnt source)
	{
		_source = source;
	}
	
	public SimEnt source()
	{
		return _source;
	}

	public void entering(SimEnt locale)
	{
	}
}