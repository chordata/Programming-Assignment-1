import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface GPSOfficeRef extends Remote
{
/**
 * Query this GPSOffice for its coordinates. This method is called by the
 * query client program to originate a query at this node.
 *
 * @param  title  Article title.
 *
 * @return  Article contents for the given title, or null if the given title
 *          does not exist.
 *
 * @exception  RemoteException
 *     Thrown if a remote error occurred.
 */
public double[] Coordinates
	()
	throws RemoteException;
public ArrayList<String> getneighbors();
public ArrayList<Double> getneighborDistance();

public boolean replaceneighbor(String toreplace , String replacewith);

public boolean addneighbor(String add);


}