import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryProxy;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Class Log provides the log program in the P2Pedia system.
 */
public class Headquarters {

	private RegistryEventListener registryListener;
	private RemoteEventListener<GPSOfficeEvent> GPSOfficeListener;
	private String host;
	private int port;
	private RegistryProxy registry;

	public Headquarters(String[] args)  {
		if (args.length != 2)
			usage();
		 host = args[0];
		port = parseInt(args[1], "port");
		try {
			registry = new RegistryProxy(host, port);
		} catch (RemoteException e1) {
			System.out.println(" Unable to connect to registry");
		}
		registryListener = new RegistryEventListener() {
			public void report(long seqnum, RegistryEvent event) {

				try {
					Thread.sleep(1000);
					listenToNode(event.objectName());
				} catch (InterruptedException e) {
					System.out.println(" Sleep interrupted");
				}
			}
		};
		try {
			UnicastRemoteObject.exportObject(registryListener, 0);
		} catch (RemoteException e) {
			System.out.println(" Unable to export Headquarters unicast remote object");
			
		}

		GPSOfficeListener = new RemoteEventListener<GPSOfficeEvent>() {
			public void report(long seqnum, GPSOfficeEvent event) {
				System.out.println(event.message);
			}
		};
		try {
			UnicastRemoteObject.exportObject(GPSOfficeListener, 0);
		} catch (RemoteException e) {
			System.out.println(" Unable to export GPSOfficeListener unicast remote object");
		}

		try {
			registry.addEventListener(registryListener);
		} catch (RemoteException e) {
			System.out.println(" Unable to export RegistryListener unicast remote object");
		}
		try {
			for (String objectName : registry.list()) {
				listenToNode(objectName);

			}
		} catch (RemoteException e) {
			System.out.println(" Unable to add listner on an office");
		}

	}

	public static void main(String[] args) {
		// Parse command line arguments.
		Headquarters A = new Headquarters(args);

	}

	/**
	 * Tell the given node object to notify us of queries.
	 * 
	 * @param objectName
	 *            Node object's name.
	 * @return
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	private synchronized Lease listenToNode(String objectName) {
		Lease event = null;
		try {
			GPSOfficeRef node = (GPSOfficeRef) registry.lookup(objectName);
			if (node != null)
				event = node.addListener(GPSOfficeListener);

		} catch (NotBoundException exc) {
			System.out.println(" Registry entry not bound for office "+ objectName);
		} catch (RemoteException exc) {
			System.out.println(" Error RMI object for office "+ objectName);
		}

		return event;
	}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage() {
		System.err.println("Usage: java Log <host> <port>");
		System.err.println("<host> = Registry Server's host");
		System.err.println("<port> = Registry Server's port");
		System.exit(1);
	}

	/**
	 * Parse an integer command line argument.
	 * 
	 * @param arg
	 *            Command line argument.
	 * @param name
	 *            Argument name.
	 * 
	 * @return Integer value of <TT>arg</TT>.
	 * 
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if <TT>arg</TT> cannot be
	 *                parsed as an integer.
	 */
	private static int parseInt(String arg, String name) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException exc) {
			System.err.printf("HeadQuarters: Invalid <%s>: \"%s\"", name, arg);
			usage();
			return 0;
		}
	}
}