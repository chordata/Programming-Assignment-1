
import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
	private   RemoteEventListener<GPSOfficeEvent> GPSOfficeListener;
	 String originnode;
	private String nexthop;
	double x;
	double y;
	long trackno;
	
	
	
	public Customer(String args[]) 
	{
	
		if (args.length != 5) usage();
		 host = args[0];
		 port = parseInt (args[1], "port");
		 try {
			registry = new RegistryProxy (host, port);
		} catch (RemoteException e1) {
			System.out.println(" Unable to connect to Registry server on host "+ host + " port "+ port);
	
		}
		 originnode=args[2];
		x=parseDouble(args[3],"x");
		y=parseDouble(args[4], "y");
		GPSOfficeRef node = null;
		try {
			node = (GPSOfficeRef) registry.lookup (originnode);
		} catch (RemoteException e1) {
			System.out.println(" Error on RMI object for office " + originnode);
		} catch (NotBoundException e1) {
			System.out.println(" Unable to retrieve RMI object for office "+ originnode + " as it is not bound to registry");
			
		}
		try {
			trackno = node.sendpackage(x,y);
		} catch (RemoteException e1) {
			System.out.println(" Unable to send package error in RMI of  "+ originnode + " office");
			e1.printStackTrace();
		} catch (NotBoundException e1) {
			System.out.println(" Unable to send package as  "+ originnode + " office not bound to registry");

		}
		
	/*	listener=new LeaseListener() {
			
			public void leaseRenewed(Lease arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void leaseExpired(Lease arg0) {
				System.out.println("Package number "+trackno + " lost by " +nexthop);
				
			}
			
			public void leaseCanceled(Lease arg0) {
				// TODO Auto-generated method stub
				
			}
		};*/
		GPSOfficeListener = new RemoteEventListener<GPSOfficeEvent>()
				{
				public void report (long seqnum, GPSOfficeEvent event) throws RemoteException
					{
					// Print log report on the console.
					
					
					if(event.TrackNo==trackno)
					{
					System.out.println(event.message);
					
					if(event.message.contains("delivered") || event.message.contains("lost"))
						System.exit(0);
					
				
					
					else if(event.nexthop!=null)
					{
						nexthop=event.nexthop;
						 listenToNode(nexthop);						
					}
					
					}
					
					
							
				}
				};
			try {
				UnicastRemoteObject.exportObject(GPSOfficeListener, 0);
			} catch (RemoteException e) {
				System.out.println(" Unable to export GPSOfficeListener unicast remote object");
			}
			
	}
	
	
	
		
	/**
	 * Query main program.
	 */
	public static void main
		(String[] args)
		
		{
		
		   Customer A=new Customer(args);
		  A.listenToNode(A.originnode);
		    
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
	(String objectName) 
	{
	
		//System.out.println(objectName);
		GPSOfficeRef node = null;
		try {
			node = (GPSOfficeRef) registry.lookup (objectName);
		} catch (RemoteException e) {
			System.out.println(" Unable to add listener to office "+ objectName + " due to error on RMI");

			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println(" Unable to add listener to office "+ objectName + " as RMI not bound to registry");

			e.printStackTrace();
		}
		Lease event = null;
		try {
			event = node.addListener (GPSOfficeListener);
		} catch (RemoteException e) {
			System.out.println(" Unable to add listener to office "+ objectName + " due to error on RMI");

			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println(" Unable to add listener to office "+ objectName + " as RMI not bound to registry");

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	return event;
		
		
	}

	}
