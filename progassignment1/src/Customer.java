
import edu.rit.ds.Lease;
import edu.rit.ds.LeaseListener;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventFilter;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryProxy;
import edu.rit.ds.test.LeaseListener01;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.omg.CosNaming.NamingContextExtPackage.AddressHelper;

/**
 * Class Query encapsulates a query in the P2Pedia system. A query is uniquely
 * identified by the originating node ID and serial number. Class Query is
 * serializable so it can be passed in remote method calls. Class Query
 * implements the <TT>equals()</TT> and <TT>hashCode()</TT> methods so query
 * objects can be used as keys in hashed data structures.
 * <P>
 * Class Query also has the main program for querying the P2Pedia system.
 * <P>
 * Usage: java Query <I>host</I> <I>port</I> <I>id</I> "<I>title</I>"
 * <BR><I>host</I> = Registry Server's host
 * <BR><I>port</I> = Registry Server's ports
 * <BR><I>id</I> = ID of originating node
 * <BR><I>title</I> = Article title
 */
public class Customer
	implements Serializable
	{
	/**
	 * This query's originating node ID.
	 */
	
	/**
	 * This query's serial number.
	 */
	

	/**
	 * This query's article title.
	 */
	private String host;
	private   int port;
    private  RegistryProxy registry;
	private   RemoteEventListener<NodeEvent> nodeListener;
	private LeaseListener listener;
	 String originnode;
	 private Lease leaseevent;
	 private String nexthop;
	double x;
	double y;
	long trackno;
	
	
	
	public Customer(String args[]) throws RemoteException, NotBoundException
	{
	
		if (args.length != 5) usage();
		 host = args[0];
		 port = parseInt (args[1], "port");
		 registry = new RegistryProxy (host, port);
		 originnode=args[2];
		x=parseDouble(args[3],"x");
		y=parseDouble(args[4], "y");
		GPSOfficeRef node = (GPSOfficeRef) registry.lookup (originnode);
		trackno = node.sendpackage(x,y);
		
		listener=new LeaseListener() {
			
			public void leaseRenewed(Lease arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void leaseExpired(Lease arg0) {
				System.out.println("Package number "+trackno + " lost by " +nexthop);
				
			}
			
			public void leaseCanceled(Lease arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		nodeListener = new RemoteEventListener<NodeEvent>()
				{
				public void report (long seqnum, NodeEvent event) throws RemoteException
					{
					// Print log report on the console.
					
					
					if(event.TrackNo==trackno)
					{
					System.out.println(event.nodeID + ":"+ event.message);
					
					if(event.message.contains("delivered"))
						System.exit(0);
					
					else if(event.nexthop!=null)
					{
						System.out.println("nexthop : " + event.nexthop);
						try {
							leaseevent.cancel();
							nexthop=event.nexthop;
							registerupdate(nexthop);
							
						} catch (NotBoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					}
					
					
							
				}
				};
			UnicastRemoteObject.exportObject(nodeListener, 0);
			
	}
	
	public void registerupdate(String originnode) throws RemoteException, NotBoundException
	{
		
		 leaseevent= listenToNode(originnode);
		 leaseevent.setListener(listener);
			
			/*List<String> lookup=registry.list();
			for (String string : lookup) {
				GPSOfficeRef node=(GPSOfficeRef) registry.lookup(string);
				node.addListener(nodeListener);
				
			}
			*/
	}
	
		
	/**
	 * Query main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		
		   Customer A=new Customer(args);
		  A.registerupdate(A.originnode);
		    
		}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java Query <host> <port> <id> \"<title>\"");
		System.err.println ("<host> = Registry Server's host");
		System.err.println ("<port> = Registry Server's port");
		System.err.println ("<id> = ID of originating node");
		System.err.println ("<title> = Article title");
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
			System.err.printf ("Query: Invalid <%s>: \"%s\"", name, arg);
			usage();
			return 0;
			}
		}
	private static double parseDouble
	(String arg,
	 String name)
	{
	try
		{
		return Double.parseDouble(arg);
		}
	catch (NumberFormatException exc)
		{
		System.err.printf ("Query: Invalid <%s>: \"%s\"", name, arg);
		usage();
		return 0;
		}
	}
	private Lease listenToNode
	(String objectName) throws RemoteException, NotBoundException
	{
	
		System.out.println(objectName);
		GPSOfficeRef node = (GPSOfficeRef) registry.lookup (objectName);
		Lease event=node.addListener (nodeListener);
		
		
	return event;
		
		
	}

	}
