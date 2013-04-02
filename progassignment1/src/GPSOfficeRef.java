import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;

public interface GPSOfficeRef extends Remote {
	/**
	 * Query this GPSOffice for its coordinates. This method is called by the
	 * query client program to originate a query at this node.
	 * 
	 * @param title
	 *            Article title.
	 * 
	 * @return Article contents for the given title, or null if the given title
	 *         does not exist.
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	public double[] Coordinates() throws RemoteException,NotBoundException;

//	public ArrayList<String> getneighbors()throws RemoteException,NotBoundException;

//	public ArrayList<Double> getneighborDistance()throws RemoteException,NotBoundException;

//	public boolean replaceneighbor(String toreplace, String replacewith)
//			throws RemoteException, NotBoundException;

//	public boolean addneighbor(String add)throws RemoteException,NotBoundException;
	public String getname()throws RemoteException,NotBoundException;

	

	
	public long sendpackage(double X , double Y) throws  RemoteException, NotBoundException;

	public  void forward(long trackingno,double x, double y) throws RemoteException, NotBoundException;

	public Lease addListener(RemoteEventListener<NodeEvent> nodeListener) throws RemoteException;
//	public double[] maxneighbordist() throws RemoteException;;

//	public void deletenode(String node) throws RemoteException, NotBoundException;
}