package Sim;

// This class is used as a timeout event to trigger re-sending of packages

public class TimeoutResend implements Event{
	private MessageTCP _msg;
	
	TimeoutResend (MessageTCP msg)
	{
		_msg = msg;
	}
	
	public MessageTCP getMsg()
	{
		return _msg; 
	}

	public void entering(SimEnt locale)
	{
	}
}
	
