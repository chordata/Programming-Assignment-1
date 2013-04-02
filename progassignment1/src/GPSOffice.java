import java.io.IOException;
import java.lang.reflect.Array;
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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
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
	//private ArrayList<Double> neighbourdist = new ArrayList<Double>();
	private RemoteEventGenerator<NodeEvent> eventGenerator;
	private ScheduledExecutorService reaper;

	public GPSOffice(String[] args) throws IOException, NotBoundException,
			IndexOutOfBoundsException {
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
		registry = new RegistryProxy(host, port);

		// Export this node.
		UnicastRemoteObject.exportObject(this, 0);

		// Bind this node into the Registry Server.
		try {
			registry.bind(name, this);
		} catch (AlreadyBoundException exc) {
			try {
			     
				registry.rebind(name, this);
			} catch (NoSuchObjectException exc2) {
			}
			throw new IllegalArgumentException("Node(): <myid> = \"" + name
					+ "\" already exists");
		} catch (RemoteException exc) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException exc2) {
			}
			throw exc;
		}

		eventGenerator = new RemoteEventGenerator<NodeEvent>();
		reaper = Executors.newScheduledThreadPool(100);

	}

	/*private void checkneighborlist(List<String> lookuplist)
			throws RemoteException, NotBoundException {

		for (String office : lookuplist) {

			GPSOfficeRef info = lookup(office);
			if (!office.equals(name)&& info!=null) {

				

				ArrayList<String> neighborslist = info.getneighbors();

				if (neighborslist.size() < 3) {
					info.addneighbor(name);
				} else {

					double[] far_neighbourinfo = info.maxneighbordist();

					double dist_from_office = this.distance(info);

					if (dist_from_office < far_neighbourinfo[0])
					{
						info.replaceneighbor(
								neighborslist.get((int) far_neighbourinfo[1]),
								name);
					    
					}
					}
			}
		}

	}
*/
	private static int parseInt(String arg, String name) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("GPSOffice(): Invalid <" + name
					+ ">: \"" + arg + "\"");
		}
	}

	
	
	private void neighbourfinder() throws RemoteException, NotBoundException
	{
		neighbors.clear();
		List<String> lookuplist = this.getList();

		if (lookuplist.size() > 1) {

			HashMap<Double, ArrayList<String>> GPSOfficedistance = new HashMap<Double, ArrayList<String>>();

			for (String string : lookuplist) {
				
					
					GPSOfficeRef office = lookup(string);
					
				
				if (!string.equals(name) && office!=null) {

					
					Double distance = distance(office);
                   if(distance!=-1)
                   {
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

			//neighbors.clear();
			while (neighbors.size() < 3
					&& neighbors.size() != (lookuplist.size() - 1)) {

				if (!nearestlist.isEmpty()) {
					neighbors.add(nearestlist.get(0));
					//neighbourdist.add(nearest);
					nearestlist.remove(0);

				}

				else {
					sortedindex++;
					nearest = dist.get(sortedindex);
					nearestlist = GPSOfficedistance.get(nearest);
				}
			}

		}

	//	checkneighborlist(lookuplist);

		List<String> printlist = getList();

		/*for (String string : printlist) {

			GPSOfficeRef info = lookup(string);
			System.out.print(info.getname() + ":");
			for (String string2 : info.getneighbors()) {

				System.out.print(string2 + " ");
			
			}
			
			System.out.println();
			
		}*/

		}
	
	private static double parseDouble(String arg, String name) {
		try {
			return Double.parseDouble(arg);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("Node(): Invalid <" + name
					+ ">: \"" + arg + "\"");
		}
	}

	public double[] Coordinates() throws RemoteException {

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
		try {
		
			point2 = GPSneighbour.Coordinates();

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			
		
			return -1;
		}
		// System.out.println(GPSneighbour.getname()+ " : "+ point2[0] + " " +
		// point2[1] + " "+ this.X + " "+this.Y + " "+ this.Y + name + X+" "+
		// Y);
		distance = Math
				.pow(Math.pow((X - point2[0]), 2)
						+ Math.pow((Y - point2[1]), 2), .5);

		return distance;

	}

	public ArrayList<String> getneighbors() {
		// TODO Auto-generated method stub
		return neighbors;
	}

/*	public ArrayList<Double> getneighborDistance() {
		// TODO Auto-generated method stub
		return neighbourdist;
	}*/

/*	public boolean replaceneighbor(String toreplace, String replacewith)
			throws RemoteException, NotBoundException {
		// TODO Auto-generated method stub

		if (neighbors.contains(toreplace)) {
			int index = neighbors.indexOf(toreplace);
			neighbors.set(index, replacewith);
           System.out.println("replacing : "+ index+ " "+ replacewith + " " + toreplace);
			GPSOfficeRef office = lookup(replacewith);
			double newneighbordist = distance(office);
	           System.out.println("distance : "+ index+ " "+ newneighbordist + " " );
			neighbourdist.set(index, newneighbordist);

			return true;
		} 
			return false;

	}

	public boolean addneighbor(String add) throws NotBoundException,
			RemoteException {

		neighbors.add(add);
		GPSOfficeRef office = lookup(add);
		double newneighbordist = distance(office);
		neighbourdist.add(newneighbordist);
		return true;

	}
*/
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
	public  void forward(long trackingno, double x, double y)
			throws RemoteException, NotBoundException {

		// If this node has the article, return the contents.

		
		String message = " package " + trackingno + "recieved by " + name;

		System.out.println(message);
		
		eventGenerator.reportEvent(new NodeEvent(name, message, trackingno,
				null));
        slowDown();
        neighbourfinder();
        System.out.println(neighbors.toArray().toString());
		ArrayList<Double> destinationdistance = new ArrayList<Double>();
		double[] p1 = { x, y };
		double[] p2 = { this.X, this.Y };
		destinationdistance.add(distancecalculator(p1, p2));

		for (String name : this.neighbors) {
			GPSOfficeRef neighbor = (GPSOfficeRef) registry.lookup(name);
			p2 = neighbor.Coordinates();
			destinationdistance.add(distancecalculator(p1, p2));
		}

		int index = 0;
		for (double d : destinationdistance) {

		
			if (d < destinationdistance.get(index)) {
				index = destinationdistance.indexOf(d);
			
			}
		}
		if (index == 0) {
			message = " package" + trackingno + " delivered  by " + name;
			eventGenerator.reportEvent(new NodeEvent(name, message, trackingno,null));
            slowDown();
			System.out.println(message);
		}

		// If this node doesn't have the article, and this node has not seen the
		// query before, forward the query.
		else {
			message = " package" + trackingno + " departed  office " + name
					+ " to" +neighbors.get(index-1) ;
			System.out.println(message);
			
			eventGenerator.reportEvent(new NodeEvent(name, message, trackingno,
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
	private  void forwardQueryTo(long tracking, String id, double x, double y) {
		try {
			System.out.println("nxthop:" + id);
			GPSOfficeRef node = (GPSOfficeRef) registry.lookup(id);
			node.forward(tracking, x, y);
		} catch (Exception exc) {

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

	public Lease addListener(RemoteEventListener<NodeEvent> listener)
			throws RemoteException {
		return eventGenerator.addListener(listener);
	}

	public long sendpackage(final double X, final double Y)
			throws RemoteException, NotBoundException {

		// Wait one second.
		// slowDown();

		final long trackingno = System.currentTimeMillis();
		reaper.schedule(new Runnable() {public void run() {
			// TODO Auto-generated method stub
			
			
			try {
				
				forward(trackingno, X, Y);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	},3,TimeUnit.SECONDS);
			
			
		

		return trackingno;

		// TODO Auto-generated method stub

	}

	/*public double[] maxneighbordist() throws RemoteException {
		// TODO Auto-generated method stub

		double[] officeinfo = new double[2];
		officeinfo[0] = 0;
		officeinfo[1] = 0;
		for (double dist : neighbourdist) {
			{
				if (dist > officeinfo[0]) {
					officeinfo[0] = dist;
					officeinfo[1] = neighbourdist.indexOf(dist);
				}
			}

		}

		return officeinfo;
	}*/

/*	public void deletenode(String node) throws RemoteException, NotBoundException {
		
		if(neighbors.contains(node))
		{
			
			System.out.println("contains node:"+ node);
			double distance=Double.MAX_VALUE;
			double temp=0;
			String newnieghbour=null;
			for (String office : registry.list()) {
				GPSOfficeRef officeref=(GPSOfficeRef)registry.lookup(office);
				if(!office.equals(name) && !office.equals(node)&& officeref!=null)
				{
								
				temp=distance(officeref);
				
				if(temp<distance)
				{
					distance=temp;
					newnieghbour=office;
				}
				}
				}
		
			System.out.println("DeletingNode: " + node+ " "+ newnieghbour);
			
			System.err.println(replaceneighbor(node, newnieghbour));

		}
		
			}*/
		// TODO Auto-generated method stub
		
}

