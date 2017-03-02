package Sim;

public class AgentSolicitation implements Event{
	private SimEnt _source;
	
	 AgentSolicitation(SimEnt source)
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