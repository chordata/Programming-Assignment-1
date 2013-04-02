import edu.rit.ds.Lease;
import edu.rit.ds.LeaseListener;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventFilter;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryProxy;
import edu.rit.ds.registry.RegistryServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * Class Log provides the log program in the P2Pedia system.
 */
public class HQ
	{
	
	private  RegistryEventListener registryListener;
	private  RemoteEventListener<NodeEvent> nodeListener;
	private HashMap<Lease, String> leasemap;
	//private static LeaseListener leaselisten;
	private  String host;
	private  int port;
	private  RegistryProxy registry;
	
	public HQ(String[] args) throws RemoteException
	{
		if (args.length != 2) usage();
		String host = args[0];
		int port = parseInt (args[1], "port");
        registry = new RegistryProxy (host, port);
		leasemap =new HashMap<Lease, String>();
		/* leaselisten=new LeaseListener() {
			
			public void leaseRenewed(Lease arg0) {
		
			}
			
			public void leaseExpired(Lease died) {
				informnodes(died);
				
			}
			
			public void leaseCanceled(Lease arg0) {
			}
		};
*/
		
		registryListener = new RegistryEventListener()
			{
			public void report (long seqnum, RegistryEvent event)
				{
			      
				
				Lease eventlease=	listenToNode (event.objectName());
			   // eventlease.setListener(leaselisten);
			   // leasemap.put(eventlease,event.objectName() );
			   
				if(eventlease!=null)
				System.out.println("listening to: "+ eventlease.getExpiration()+ " for "+ event.objectName());
				}
			};
		UnicastRemoteObject.exportObject (registryListener, 0);

		nodeListener = new RemoteEventListener<NodeEvent>()
			{
			public void report (long seqnum, NodeEvent event)
				{
				    System.out.println(event.message);
				}
			};
		UnicastRemoteObject.exportObject (nodeListener, 0);

		registry.addEventListener (registryListener);
		for (String objectName : registry.list ())
			{
			 listenToNode (objectName);
			//   leasemap.put(eventlease, objectName);
			}
		

	}
	
		
		
		private synchronized void informnodes(Lease died) {
			
			String node=leasemap.get(died);
			System.out.println("Died "+ died.hashCode() + " "+node);
			
			try {
				for (String obj: registry.list())
				{
					if(!obj.equals(died))
					{
						System.out.println(obj);
					
						GPSOfficeRef officeref=(GPSOfficeRef)registry.lookup(obj);
					if(officeref!=null)
					{
					//.deletenode(node);
					}
					}
					}
			} catch (RemoteException e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
			
		// TODO Auto-generated method stub
		
	}



		// TODO Auto-generated method stub
		
	
	public static void main
		(String[] args)
		throws Exception
		{
		// Parse command line arguments.
			HQ A=new HQ(args);	
		
		}

	/**
	 * Tell the given node object to notify us of queries.
	 *
	 * @param  objectName  Node object's name.
	 * @return 
	 *
	 * @exception  RemoteException
	 *     Thrown if a remote error occurred.
	 */
	private  synchronized Lease listenToNode
		(String objectName)
		{
		Lease event = null;
		try
			{
			GPSOfficeRef node = (GPSOfficeRef) registry.lookup (objectName);
			if(node!=null)
			event=node.addListener (nodeListener);
			else 
				System.out.println(" null value : "+ objectName );
			}
		catch (NotBoundException exc)
			{
			}
		catch (RemoteException exc)
			{
			}
	
	return event;
		}
	

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java Log <host> <port>");
		System.err.println ("<host> = Registry Server's host");
		System.err.println ("<port> = Registry Server's port");
		System.exit (1);
		}

	/**
	 * Parse an integer command line argument.
	 *
	 * @param  arg  Command line argument.
	 * @param  name  Argument name.
	 *
	 * @return  Integer value of <TT>arg</TT>.
	 *
	 * @exception  IllegalArgumentException
	 *     (unchecked exception) Thrown if <TT>arg</TT> cannot be parsed as an
	 *     integer.
	 */
	private static int parseInt
		(String arg,
		 String name)
		{
		try
			{
			return Integer.parseInt (arg);
			}
		catch (NumberFormatException exc)
			{
			System.err.printf ("Log: Invalid <%s>: \"%s\"", name, arg);
			usage();
			return 0;
			}
		}
	}