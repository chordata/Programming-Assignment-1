import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import edu.rit.ds.registry.AlreadyBoundException;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
	private ArrayList<String> neighbors=new ArrayList<String>();
	private ArrayList<Double> neighbourdist=new ArrayList<Double>() ;
	

	public GPSOffice(String[] args) throws IOException, NotBoundException {
		// Parse command line arguments.
		if (args.length != 5) {
			throw new IllegalArgumentException(
					"Usage: java Start GPSOffice <host> <port> <name> <X> <Y>");
		}
		host = args[0];
		port = parseInt(args[1], "port");
		name = args[2];
		X = parseDouble(args[3], "X");
		X = parseDouble(args[4], "Y");

		// Get a proxy for the Registry Server.
		registry = new RegistryProxy(host, port);

		// Export this node.
		UnicastRemoteObject.exportObject(this, 0);

		// Bind this node into the Registry Server.
		try {
			registry.bind(name, this);
		} catch (AlreadyBoundException exc) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
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

		List<String> lookuplist = this.getList();

		if(lookuplist.size()>1)
		{
		
		HashMap<Double,ArrayList<String>> GPSOfficedistance=new HashMap<Double, ArrayList<String>>();
		
		for (String string : lookuplist) {

			if(!string.equals(name))
			{
			
			GPSOfficeRef office = lookup(string);
			Double distance = distance(office);
			
			if(!GPSOfficedistance.containsKey(distance))
			{
				
				ArrayList<String> locations=new ArrayList<String>();
				
				GPSOfficedistance.put(distance, locations);
			
			}
			
				GPSOfficedistance.get(distance).add(string);
					
			}
			
		}
		
		Collection<Double> officedistset = GPSOfficedistance.keySet();

		List<Double> dist = new ArrayList<Double>(officedistset);

		Collections.sort(dist);
		
		double nearest=dist.get(0);
		
		ArrayList<String> nearestlist=GPSOfficedistance.get(nearest);
       
		checkneighborlist(nearestlist,nearest);
 		
		}
		
		
		List<String> printlist=getList();
		
		for (String string : printlist) {
			
			GPSOfficeRef info=lookup(string);
			System.out.print(info.getname() + ":" );
			for (String string2 : info.getneighbors()) {
				
				System.out.print(string2 + " ");
			}
			System.out.println();
			
		}
			
		}
		
		

	
	private void checkneighborlist(ArrayList<String> list, Double nearest)
			throws RemoteException, NotBoundException {
		int connectionscreated=0;
		
		for (String office : list) {
           
			System.out.println("RMI: "+office );
			GPSOfficeRef info = lookup(office);
			ArrayList<Double> distances = info.getneighborDistance();
			ArrayList<String> neighborslist = info.getneighbors();

			if (distances.size() == 3 && neighbors.size()<3) {
				int index = -1;
				for (Double dist : distances) {

					if (dist < nearest)
						index = distances.indexOf(dist);

				}
				if (index != -1&& neighbors.size()<3 ) {
					info.replaceneighbor(neighborslist.get(index), name);
					neighbors.add(info.getname());
					neighbourdist.add(distance(info));
					connectionscreated++;
				}

			}
			
			else if(connectionscreated<3)
			{
				info.addneighbor(name);
				neighbors.add(info.getname());
				neighbourdist.add(distance(info));
				connectionscreated++;
			}

		}
	   
   }
	private static int parseInt(String arg, String name) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("GPSOffice(): Invalid <" + name
					+ ">: \"" + arg + "\"");
		}
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

	public double distance(GPSOfficeRef GPSneighbour) throws NotBoundException {
		double distance = 0;

		double[] point2 = null;
		try {
			point2 = GPSneighbour.Coordinates();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		distance = Math.pow(
				Math.pow((this.X - point2[0]), 2)
						+ Math.pow((this.Y - point2[1]), 2), .5);

		return distance;

	}

	public ArrayList<String> getneighbors() {
		// TODO Auto-generated method stub
		return neighbors;
	}

	public ArrayList<Double> getneighborDistance() {
		// TODO Auto-generated method stub
		return neighbourdist;
	}

	public boolean replaceneighbor(String toreplace, String replacewith)
			throws RemoteException, NotBoundException {
		// TODO Auto-generated method stub

		if (neighbors.contains(toreplace)) {
			int index = neighbors.indexOf(toreplace);
			neighbors.set(index, replacewith);

			GPSOfficeRef office = lookup(replacewith);
			double newneighbordist = distance(office);
			neighbourdist.set(index, newneighbordist);

			return true;
		} else
			return false;

	}

	public boolean addneighbor(String add) throws NotBoundException {

		neighbors.add(add);
		GPSOfficeRef office = lookup(add);
		double newneighbordist = distance(office);
		neighbourdist.add(newneighbordist);
		return true;

	}




	public String getname() {
		// TODO Auto-generated method stub
		return name;
	}

}
