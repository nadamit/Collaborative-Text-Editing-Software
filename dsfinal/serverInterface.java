import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface serverInterface extends Remote {

	public String connect(String IpAddress) throws RemoteException,
			NotBoundException;

	public HashMap<String, Document> viewdocument() throws RemoteException,
			NotBoundException;

	public void createDocument(String docName, String IpAddress)
			throws RemoteException, NotBoundException;

	public Document joinToDocument(String docNametoJoin, String IpAddress)
			throws RemoteException, NotBoundException;

	public void synchronize(String docName, String textArea, String SavedbyIp)
			throws RemoteException, NotBoundException;

	public void updateDocuments(ArrayList<String> nodelist,
			HashMap<String, Document> hashmap) throws RemoteException,
			NotBoundException;

	public void closeDocument(String docName, String LOCALHOST_IP)
			throws RemoteException, NotBoundException;

	public void disconnectUser(String IpAddress) throws RemoteException,
			NotBoundException;

	public String openFile(String fileName, String Ip) throws RemoteException,
			NotBoundException;
}
