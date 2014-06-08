import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class SyncManager implements Runnable {
	Client clientObj;
	String docNametoJoin;
	Document docToJoin;
	String LOCALHOST_IP;
	String fileCreator;
	public boolean flag = false;

	public SyncManager(Client clientObj, String docNametoJoin,
			Document docToJoin, String Ip) {
		// TODO Auto-generated constructor stub
		this.clientObj = clientObj;
		this.docNametoJoin = docNametoJoin;
		this.fileCreator = Ip;
		try {
			this.LOCALHOST_IP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		long time = 0;
		flag = true;
		while (flag) {
			this.clientObj.EditorforDocument.get(docNametoJoin).inc_times = 0;
			this.clientObj.EditorforDocument.get(docNametoJoin).dec_times = 0;

			if (!clientObj.EditorforDocument.get(docNametoJoin).equals(null)) {
				int currentcareat = clientObj.EditorforDocument
						.get(docNametoJoin).editorFrame.textArea
						.getCaretPosition();
				long sec = System.currentTimeMillis() / 15000;
				if (sec != time) {
					Registry peerRegistry;
					try {
						peerRegistry = LocateRegistry.getRegistry(fileCreator,
								6500);
						clientInterface clientRemoteObject = (clientInterface) peerRegistry
								.lookup("client");

						// System.out.println("Connected to creator of document "
						// + fileCreator);
						String textArea = clientRemoteObject.getTextArea(
								docNametoJoin, LOCALHOST_IP);
						clientObj.EditorforDocument.get(docNametoJoin).editorFrame.textArea
								.setText(textArea);
						clientObj.EditorforDocument.get(docNametoJoin).editorFrame.textArea
								.setCaretPosition(currentcareat
										+ this.clientObj.EditorforDocument
												.get(docNametoJoin).inc_times
										- this.clientObj.EditorforDocument
												.get(docNametoJoin).dec_times);

						// System.out.println("care at position " +
						// currentcareat);
						// System.out.println("increase times "
						// + this.clientObj.EditorforDocument
						// .get(docNametoJoin).inc_times);
						// System.out.println("Decrease times "
						// + this.clientObj.EditorforDocument
						// .get(docNametoJoin).dec_times);
						// clientRemoteObject.enableEditorAtPeers(docNametoJoin);
						time = sec;
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotBoundException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						// System.out
						// .println("Not connected to peers at SyncManager.");
					} catch (IllegalArgumentException e) {
						System.out.println("");
					} catch (Exception e) {
						// System.out
						// .println("File closed so not getting synchronized with co-ordinator.");
						this.flag = false;
					}

				}
			}
		}

	}
}
