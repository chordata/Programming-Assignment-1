import java.io.IOException;
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
<port> is the port number to which the Registry Server is listening.
<name> is the name of the city where the GPS office is located.
<X> is the GPS office's X coordinate (type double).
<Y> is the GPS office's Y coordinate (type double).
 *
 */
public class GPSOffice implements GPSOfficeRef {
	
	private String host;
	private int port;
	private String name;
	private double X;
	private double Y;
	private RegistryProxy registry;
	private ArrayList<String> neighbors;
	private ArrayList<Double> neighbourdist;
	
	
	public GPSOffice
	(String[] args)
	throws IOException
	{
	// Parse command line arguments.
	if (args.length != 5)
		{
		throw new IllegalArgumentException
			("Usage: java Start GPSOffice <host> <port> <name> <X> <Y>");
		}
	host = args[0];
	port = parseInt(args[1], "port");
	 name= args[2];
	X = parseDouble(args[3],"X");
	X = parseDouble(args[4],"Y");

		// Get a proxy for the Registry Server.
	registry = new RegistryProxy (host, port);

	// Export this node.
	UnicastRemoteObject.exportObject(this,0);

	// Bind this node into the Registry Server.
	try
		{
		registry.bind (name, this);
		}
	catch (AlreadyBoundException exc)
		{
		try
			{
			UnicastRemoteObject.unexportObject (this, true);
			}
		catch (NoSuchObjectException exc2)
			{
			}
		throw new IllegalArgumentException
			("Node(): <myid> = \""+name+"\" already exists");
		}
	catch (RemoteException exc)
		{
		try
			{
			UnicastRemoteObject.unexportObject (this, true);
			}
		catch (NoSuchObjectException exc2)
			{
			}
		throw exc;
		}
	
	List<String> lookuplist=this.getList();
    
	HashMap<String,Double> GPSOfficeDist=new HashMap<String, Double>();
	
	for (String string : lookuplist) {
		
		GPSOfficeRef office=lookup(string);
		Double distance=distance(office);
		GPSOfficeDist.put(string,distance);
		
	}
    
	Collection<Double> officedistset=GPSOfficeDist.values();
	
     List<Double> dist=new ArrayList<Double>(officedistset);
     
     Collections.sort(dist);
    
	}
	
	
	

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
		throw new IllegalArgumentException
			("GPSOffice(): Invalid <"+name+">: \""+arg+"\"");
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
		throw new IllegalArgumentException
			("Node(): Invalid <"+name+">: \""+arg+"\"");
		}
	}

	
	
	public double[] Coordinates() throws RemoteException {
		
		double[] coordinates=new double[2];
		coordinates[0]=this.X;
		coordinates[1]=this.Y;
		return coordinates;
	}
	
    public List<String> getList() 
    {
    	try {
			return registry.list();
		} catch (RemoteException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
    }
	
    public GPSOfficeRef lookup(String name)
    {
		 GPSOfficeRef GPSneighbour=null;
		try {
			GPSneighbour = (GPSOfficeRef)registry.lookup(name);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		return GPSneighbour;
		    	
    }
   
 public double distance(GPSOfficeRef GPSneighbour)
 {
	 double distance=0;
	 
	 double[] point2 = null;
	try {
		point2 = GPSneighbour.Coordinates();
	} catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 
	 distance=Math.pow(Math.pow((this.X-point2[0]),2)+Math.pow((this.Y-point2[1]),2),.5);
	 
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




public boolean replaceneighbor(String toreplace, String replacewith) {
	// TODO Auto-generated method stub
	
	if(neighbors.contains(toreplace))
	{
		int index=neighbors.indexOf(toreplace);
	    neighbors.set(index, replacewith);
	   
	}
	
	return false;
	
}




public boolean addneighbor(String add) {
	// TODO Auto-generated method stub
	return false;
}
    

}
