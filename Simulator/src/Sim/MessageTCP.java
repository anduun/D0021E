package Sim;


// This class implements an event that sends a TCP Message

public class MessageTCP implements Event{
	private NetworkAddr _source;
	private NetworkAddr _destination;
	private int _seqNr=0;
	private int _ackNr=0;
	private int _ACKFlag=0;
	private int _SYNFlag=0;
	private int _FINFlag=0;
	private double _timeSent;
	
	MessageTCP (NetworkAddr from, NetworkAddr to, int seqNr, int ackNr, int ACKFlag, int SYNFlag, int FINFlag)
	{
		_source = from;
		_destination = to;
		_seqNr=seqNr;
		_ackNr=ackNr;
		_ACKFlag=ACKFlag;
		_SYNFlag=SYNFlag;
		_FINFlag=FINFlag;
		_timeSent = SimEngine.getTime();
	}
	
	public NetworkAddr source()
	{
		return _source; 
	}
	
	public NetworkAddr destination()
	{
		return _destination; 
	}
	
	public int getSeqNr()
	{
		return _seqNr; 
	}
	
	public int getAckNr()
	{
		return _ackNr; 
	}
	
	public int getACKFlag()
	{
		return _ACKFlag; 
	}
	
	public int getSYNFlag()
	{
		return _SYNFlag; 
	}
	public int getFINFlag()
	{
		return _FINFlag; 
	}
	
	public double getTimeSent()
	{
		return _timeSent;
	}

	public void entering(SimEnt locale)
	{
	}
}