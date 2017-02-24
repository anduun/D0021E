package Sim;

// An example of how to build a topology and starting the simulation engine

public class Run {
	public static void main (String [] args)
	{
 		//Creates two links
 		Link link1 = new Link();
		Link link2 = new Link();
		
		// Create two end hosts that will be
		// communicating via the router
		Node host1 = new Node(1,1);
		Node host2 = new Node(2,2);

		//Connect links to hosts
		host1.setPeer(link1);
		host2.setPeer(link2);

		// Creates as router and connect
		// links to it. Information about 
		// the host connected to the other
		// side of the link is also provided
		// Note. A switch is created in same way using the Switch class
		Router routeNode = new Router(3);
		routeNode.connectInterface(0, link1, host1);
		routeNode.connectInterface(1, link2, host2);
		
		// Generate some traffic
		// host1 will send 10 messages with time interval 2 to network 2, node 1. Sequence starts with number 1
		host1.startSendingCBR(2, 1, 10, 2, 1);
		// host2 will send 15 messages with time interval 2 to network 1, node 1. Sequence starts with number 21
		host2.startSendingCBR(1, 1, 15, 2, 21);
		
		
		
		// host1 will change interface from 0 to 2 after it has sent 5 messages
		host1.changeInterface(2, 5);
		
		
		
		// Start the simulation engine and of we go!
		Thread t=new Thread(SimEngine.instance());
	
		t.start();
		try
		{
			t.join();
			System.out.println("____________________________________________________________");
			System.out.println("Node 1.1 sent a total of "+host1.getMessagesSent()+" messages");
			System.out.println("Node 1.1 received a total of "+host1.getMessagesReceived()+" messages");
			System.out.println("Node 2.1 sent a total of "+host2.getMessagesSent()+" messages");
			System.out.println("Node 2.1 received a total of "+host2.getMessagesReceived()+" messages");
		}
		catch (Exception e)
		{
			System.out.println("The motor seems to have a problem, time for service?");
		}
	}
}
