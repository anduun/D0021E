package Sim;

// An example of how to build a topology and starting the simulation engine

public class Run {
	public static void main (String [] args)
	{
 		//Creates two links
 		LossyLink link1 = new LossyLink(99);
		Link link2 = new Link();

		// Create two end hosts that will be
		// communicating via the router
		Node host1 = new Node(1,1);
		Node host2 = new Node(2,1);

		//Connect links to hosts
		host1.setPeer(link1);
		host2.setPeer(link2);

		// Creates as router and connect
		// links to it. Information about 
		// the host connected to the other
		// side of the link is also provided
		// Note. A switch is created in same way using the Switch class
		Router routeNode = new Router(2);
		routeNode.connectInterface(0, link1, host1);
		routeNode.connectInterface(1, link2, host2);
		
		// Generate some traffic
		// host1 will send 10 messages with time interval 3 to network 2, node 1.
		host1.startSendingTCP(2, 1, 10, 3);
		
		// Start the simulation engine and of we go!
		Thread t=new Thread(SimEngine.instance());
	
		t.start();
		try
		{
			t.join();
			System.out.println();
			System.out.println("-----------------------------------------------");
			System.out.println("Node 1.1 received and approved messages:");
			host1.printRecMessageList();
			System.out.println();
			System.out.println("Node 2.1 received and approved messages:");
			host2.printRecMessageList();
			System.out.println();
			System.out.println("Link Statistics:");
			link1.printLinkStatistics();
		}
		catch (Exception e)
		{
			System.out.println("The motor seems to have a problem, time for service?");
		}		



	}
}
