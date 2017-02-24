package Sim;

public class ChangeInterface implements Event{
	private NetworkAddr _source;
	private int _newInterfaceNumber;
	
	ChangeInterface (NetworkAddr source, int newInterfaceNumber)
	{
		_source = source;
		_newInterfaceNumber = newInterfaceNumber;
	}
	
	public NetworkAddr source()
	{
		return _source;
	}
	
	public int newInterfaceNumber()
	{
		return _newInterfaceNumber;
	}

	public void entering(SimEnt locale)
	{
	}
}




