import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface clientInterface extends Remote {

	public String connectServer(String serverIP) throws RemoteException,
			NotBoundException, UnknownHostException;

	public void view(Client clientObj) throws RemoteException,
			NotBoundException;

	// public void create(String docName) throws RemoteException,
	// NotBoundException, UnknownHostException;

	public void join(String docNametoJoin, Client clientObj)
			throws RemoteException, NotBoundException;

	public void getText(int keyword, int caretpos, String docName)
			throws RemoteException, NotBoundException;

	public String getTextArea(String docName, String IpAddress)
			throws RemoteException, NotBoundException;

	public void enableEditorAtPeers(String docName) throws RemoteException,
			NotBoundException;

	public void removeUserFromDoc(String docName, String IpAddress)
			throws RemoteException, NotBoundException;

	public void updateNewPeer(String docNametoJoin, String lOCALHOST_IP)
			throws RemoteException, NotBoundException;
}
