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
public class Log
	{
	private static String host;
	private static int port;
	private  RegistryProxy registry;
	private  RegistryEventListener registryListener;
	private  RegistryEventFilter registryFilter;
	private  RemoteEventListener<NodeEvent> nodeListener;
	private HashMap<Lease, String> leasemap;
	private static LeaseListener leaselisten;
	
	public Log(String[] args) throws RemoteException
	{
		if (args.length != 2) usage();
		String host = args[0];
		int port = parseInt (args[1], "port");
         
		// Get proxy for the Registry Server.
		registry = new RegistryProxy (host, port);

		// Export a remote event listener object for receiving notifications
		// from the Registry Server.
		leasemap =new HashMap<Lease, String>();
		 leaselisten=new LeaseListener() {
			
			public void leaseRenewed(Lease arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void leaseExpired(Lease died) {
				
				informnodes(died);
				// TODO Auto-generated method stub
				
			}
			
			public void leaseCanceled(Lease arg0) {
				// TODO Auto-generated method stub
				
			}
		};

		
		registryListener = new RegistryEventListener()
			{
			public void report (long seqnum, RegistryEvent event)
				{
			   Lease eventlease=	listenToNode (event.objectName());
			    eventlease.setListener(leaselisten);
			    leasemap.put(eventlease,event.objectName() );
				System.out.println("listening to: "+ eventlease.getExpiration()+ " for "+ event.objectName());
				}
			};
		UnicastRemoteObject.exportObject (registryListener, 0);

		// Export a remote event listener object for receiving notifications
		// from Node objects.
		nodeListener = new RemoteEventListener<NodeEvent>()
			{
			public void report (long seqnum, NodeEvent event)
				{
				// Print log report on the console.
				System.out.printf ("Node %s -- %s%n",
					event.nodeID, event.message);
				}
			};
		UnicastRemoteObject.exportObject (nodeListener, 0);

		// Tell the Registry Server to notify us when a new Node object is
		// bound.
		registry.addEventListener (registryListener);

		// Tell all existing Node objects to notify us of queries.
		for (String objectName : registry.list ())
			{
			 Lease eventlease=	listenToNode (objectName);
			   leasemap.put(eventlease, objectName);
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
						officeref.deletenode(node);
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
			Log A=new Log(args);	
		
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
	private  Lease listenToNode
		(String objectName)
		{
		Lease event = null;
		try
			{
			GPSOfficeRef node = (GPSOfficeRef) registry.lookup (objectName);
			 event=node.addListener (nodeListener);
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