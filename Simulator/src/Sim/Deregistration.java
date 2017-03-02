package Sim;

// Used when a Node deregisters its position at its Home Agent
// (Moves back home)

public class Deregistration implements Event{
	private SimEnt _source;
	
	Deregistration(SimEnt source)
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