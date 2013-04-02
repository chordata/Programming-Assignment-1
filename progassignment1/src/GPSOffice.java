import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventGenerator;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.AlreadyBoundException;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 */

/**
 * <host> is the name of the host computer where the Registry Server is running.
 * <port> is the port number to which the Registry Server is listening. <name>
 * is the name of the city where the GPS office is located. <X> is the GPS
 * office's X coordinate (type double). <Y> is the GPS office's Y coordinate
 * (type double).
 * 
 */
public class GPSOffice implements GPSOfficeRef {

	private String host;
	private int port;
	private String name;
	private double X;
	private double Y;
	private RegistryProxy registry;
	private ArrayList<String> neighbors = new ArrayList<String>();
	private RemoteEventGenerator<GPSOfficeEvent> eventGenerator;
	private ScheduledExecutorService reaper;

	public GPSOffice(String[] args)  {
		// Parse command line arguments.
		if (args.length != 5) {
			throw new IllegalArgumentException(
					"Usage: java Start GPSOffice <host> <port> <name> <X> <Y>");
		}
		host = args[0];
		port = parseInt(args[1], "port");
		name = args[2];
		X = parseDouble(args[3], "X");
		Y = parseDouble(args[4], "Y");

		// Get a proxy for the Registry Server.
		try {
			registry = new RegistryProxy(host, port);
		} catch (RemoteException e) {
			
			System.out.println(" Cannot bind to Registry Server");
			e.printStackTrace();
		}

		// Export this node.
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			System.out.println("Unable to export GPSOffice for RMI");
			e.printStackTrace();
		}

		// Bind this node into the Registry Server.
		try {
			registry.bind(name, this);
		} catch (AlreadyBoundException exc) {
			try {

				registry.rebind(name, this);
			} catch (NoSuchObjectException exc2) {
			} catch (RemoteException e) {
				System.out.println(" Cannot connect to registry server for rebind");
			}
			throw new IllegalArgumentException("GPSOffice(): <myid> = \"" + name
					+ "\" already exists");
		} catch (RemoteException exc) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException exc2) {
			}
			
		}

		eventGenerator = new RemoteEventGenerator<GPSOfficeEvent>();
		reaper = Executors.newScheduledThreadPool(100);

	}

	private static int parseInt(String arg, String name) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("GPSOffice(): Invalid <" + name
					+ ">: \"" + arg + "\"");
		}
	}

	private void neighbourfinder()  {
		neighbors.clear();
		List<String> lookuplist = this.getList();

		if (lookuplist.size() > 1) {

			HashMap<Double, ArrayList<String>> GPSOfficedistance = new HashMap<Double, ArrayList<String>>();

			for (String string : lookuplist) {

				GPSOfficeRef office = lookup(string);

				if (!string.equals(name) && office != null) {

					Double distance = null;
					try {
						distance = distance(office);
					} catch (RemoteException e) {
						System.out.println(" Cannot connect to "+ office + " for computing distance");
					} catch (NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (distance != -1) {
						if (!GPSOfficedistance.containsKey(distance)) {

							ArrayList<String> locations = new ArrayList<String>();

							GPSOfficedistance.put(distance, locations);

						}

						GPSOfficedistance.get(distance).add(string);

					}

				}
			}

			Collection<Double> officedistset = GPSOfficedistance.keySet();

			List<Double> dist = new ArrayList<Double>(officedistset);

			Collections.sort(dist);
			int sortedindex = 0;
			double nearest = dist.get(sortedindex);

			ArrayList<String> nearestlist = GPSOfficedistance.get(nearest);

			// neighbors.clear();
			while (neighbors.size() < 3
					&& neighbors.size() != (lookuplist.size() - 1)) {

				if (!nearestlist.isEmpty()) {
					neighbors.add(nearestlist.get(0));
					// neighbourdist.add(nearest);
					nearestlist.remove(0);

				}

				else {
					sortedindex++;
					nearest = dist.get(sortedindex);
					nearestlist = GPSOfficedistance.get(nearest);
				}
			}

		}

		

	}

	private static double parseDouble(String arg, String name) {
		try {
			return Double.parseDouble(arg);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("GPSOffice(): Invalid <" + name
					+ ">: \"" + arg + "\"");
		}
	}

	public double[] Coordinates()  {

		double[] coordinates = new double[2];
		coordinates[0] = this.X;
		coordinates[1] = this.Y;
		return coordinates;
	}

	public List<String> getList() {
		try {
			return registry.list();
		} catch (RemoteException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public GPSOfficeRef lookup(String name) {
		GPSOfficeRef GPSneighbour = null;
		try {
			GPSneighbour = (GPSOfficeRef) registry.lookup(name);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return GPSneighbour;

	}

	public double distance(GPSOfficeRef GPSneighbour) throws NotBoundException,
			RemoteException {
		double distance = 0;

		double[] point2 = null;
		point2 = GPSneighbour.Coordinates();

		distance = Math
				.pow(Math.pow((X - point2[0]), 2)
						+ Math.pow((Y - point2[1]), 2), .5);

		return distance;

	}

	public ArrayList<String> getneighbors() {
		// TODO Auto-generated method stub
		return neighbors;
	}

	public String getname() {
		// TODO Auto-generated method stub
		return name;
	}

	/**
	 * Forward the given query to this node. This method is called by another
	 * node that is forwarding the query to this node.
	 * 
	 * @param query
	 *            Query.
	 * 
	 * @return Article contents for the given title, or null if the given title
	 *         does not exist.
	 * @throws NotBoundException
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	public void forward(long trackingno, double x, double y)
			 {

		// If this node has the article, return the contents.

		String message = "Package number " + trackingno + " recieved at "
				+ name + " office";

		eventGenerator.reportEvent(new GPSOfficeEvent(name, message, trackingno,
				null));
		slowDown();
		neighbourfinder();
		ArrayList<Double> destinationdistance = new ArrayList<Double>();
		double[] p1 = { x, y };
		double[] p2 = { this.X, this.Y };
		destinationdistance.add(distancecalculator(p1, p2));

		for (String name : this.neighbors) {
			GPSOfficeRef neighbor = null;
			try {
				neighbor = (GPSOfficeRef) registry.lookup(name);
			} catch (RemoteException e) {
				System.out.println("RMI to " + name + " office not working");
			} catch (NotBoundException e) {
				System.out.println(" RMI not bound for " + name + " office");
			}
			try {
				p2 = neighbor.Coordinates();
			} catch (RemoteException e) {
System.out.println(" unable to retrieve coordinates due to error in RMI");
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
			System.out.println(" Object not bound in registry");
			}
			destinationdistance.add(distancecalculator(p1, p2));
		}

		int index = 0;
		for (double d : destinationdistance) {

			if (d < destinationdistance.get(index)) {
				index = destinationdistance.indexOf(d);

			}
		}
		if (index == 0) {
			message = "Package number " + trackingno + " delivered by " + name
					+ " to (" + x + "," + y + ")";
			eventGenerator.reportEvent(new GPSOfficeEvent(name, message, trackingno,
					null));
			slowDown();
		}

		// If this node doesn't have the article, and this node has not seen the
		// query before, forward the query.
		else {
			message = "Package number " + trackingno + " departed from " + name
					+ " office";

			eventGenerator.reportEvent(new GPSOfficeEvent(name, message, trackingno,
					neighbors.get(index - 1)));
			slowDown();

			forwardQueryTo(trackingno, neighbors.get(index - 1), x, y);

		}

		// If this node has seen the query before, do not forward the query.

	}

	/**
	 * Forward the given query to the node with the given ID.
	 * 
	 * @param id
	 *            Destination node ID.
	 * @param query
	 *            Query.
	 * 
	 * @return Article contents for the given title, or null if the given title
	 *         does not exist or the query could not be forwarded.
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	private void forwardQueryTo(long tracking, String id, double x, double y) {
		try {
			// System.out.println("nxthop:" + id);
			GPSOfficeRef node = (GPSOfficeRef) registry.lookup(id);
			node.forward(tracking, x, y);
		} catch (Exception exc) {

			String message = "Package number " + tracking + " lost by " + name
					+ " office";
			eventGenerator.reportEvent(new GPSOfficeEvent(name, message, tracking,
					id));

		}
	}

	/**
	 * Slow down the calling thread.
	 */
	private void slowDown() {
		try {
			Thread.sleep(3000L);
		} catch (InterruptedException exc) {
		}

	}

	private static double distancecalculator(double[] p1, double[] p2) {
		return Math
				.pow(Math.pow((p1[0] - p2[0]), 2)
						+ Math.pow((p1[1] - p2[1]), 2), .5);
	}

	public Lease addListener(RemoteEventListener<GPSOfficeEvent> listener)
			 {
		try {
			return eventGenerator.addListener(listener);
		} catch (RemoteException e) {
			System.out.println(" unable to add listener to office");
			e.printStackTrace();
		}
		return null;
	}

	public long sendpackage(final double X, final double Y)
			 {

		final long trackingno = System.currentTimeMillis();
		reaper.schedule(new Runnable() {
			public void run()  {
				// TODO Auto-generated method stub

				forward(trackingno, X, Y);
			}
		}, 3, TimeUnit.SECONDS);

		return trackingno;

		// TODO Auto-generated method stub

	}

}
