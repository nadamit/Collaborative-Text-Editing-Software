import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class Client extends UnicastRemoteObject implements clientInterface,
		Serializable {

	public EditorGUI newGUI;
	Thread t1;
	public SyncManager syncManager;
	public HashMap<String, EditorGUI> EditorforDocument = new HashMap<String, EditorGUI>();
	public String LOCALHOST_IP;
	HashMap<String, Document> DocumentAtClient = new HashMap<String, Document>();
	HashMap<String, Document> viewDocumentsFromServer = new HashMap<String, Document>();

	protected Client() throws RemoteException {
		super();
		try {
			String HOST_IP = InetAddress.getLocalHost().getHostAddress();
			this.LOCALHOST_IP = HOST_IP;
			System.setProperty("sun.rmi.transport.proxy.connectionTimeout",
					"100000");
			// System.setProperty(sun.rmi.transport.proxy., value)

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	serverInterface serverobj;
	Registry registry;

	public static void main(String[] args) throws RemoteException,
			NotBoundException {

		boolean isConnectedToServer = false;
		Client clientObj = new Client();
		String command[];
		// getting the current Users of the document
		ArrayList<String> currentClientIp;

		Scanner sc = new Scanner(System.in);
		Registry clientregistry = LocateRegistry.createRegistry(6500);
		clientregistry.rebind("client", clientObj);

		while (true) {
			try {
				System.out.println("Enter the command");
				String line = sc.nextLine();
				command = line.split(" ");
				if (command[0].equals("connect")) {
					try {
						System.out.println("Enter the Server IP");
						String serverIP = sc.next();
						if (clientObj.connectServer(serverIP).equals(
								"connected")) {
							isConnectedToServer = true;
							System.out.println("Connected to the Server:-");
						}
					} catch (UnknownHostException e) {
						System.out.println("Please check the Ip address");
					}
				} else if (isConnectedToServer == true
						&& command[0].equals("view")) {
					clientObj.view(clientObj);
				} else if (isConnectedToServer == true
						&& command[0].equals("create")) {
					System.out.println("Enter the document name");
					String docName = sc.next();
					clientObj.create(docName, clientObj);

				} else if (isConnectedToServer == true
						&& command[0].equals("join")) {

					if (command.length == 1) {
						System.out
								.println("Please enter the document name to Join");
						String docNametoJoin = sc.nextLine();

						clientObj.join(docNametoJoin, clientObj);
					} else if (command.length == 2) {
						String docNametoJoin = command[1];
						clientObj.join(docNametoJoin, clientObj);
					} else {
						System.out.println("Wrong command");
						System.out
								.println("Join command format: join <filename without .txt>");
					}

				} else if (isConnectedToServer == true
						&& command[0].equals("open")) {

					if (command.length == 1) {
						System.out
								.println("Please give me the document Name to Open");
						String fileName = sc.nextLine();
						clientObj.openFileFromServer(fileName, clientObj);
					} else if (command.length == 2) {
						System.out.println("inside else of open");
						String fileName = command[1];
						clientObj.openFileFromServer(fileName, clientObj);
					} else {
						System.out
								.println("Open command format: open <filename without .txt");
					}
				}

				else if (isConnectedToServer == true
						&& command[0].equals("leave")) {
					clientObj.leaveFromServer();
					isConnectedToServer = false;
					System.exit(0);
				}

				else if (!isConnectedToServer) {
					System.out.println("Not Connected to the Server");
				}

			}

			catch (NotBoundException e) {
				// TODO: handle exception
				System.out.println("Retry to connect Connect to the server.");
				// e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO: handle exception
				System.out.println("Please check the given IP address");
				// e.printStackTrace();
			} catch (ConnectException e) {
				System.out
						.println("Connection not established with remote host");
			}
		}

	}

	private void openFileFromServer(String docName, Client clientObj) {
		// TODO Auto-generated method stub
		try {
			String text = serverobj.openFile(docName, LOCALHOST_IP);
			// for the new client/peer
			if (!this.DocumentAtClient.containsKey(docName)) {
				EditorGUI opengui;
				try {
					viewDocumentsFromServer = serverobj.viewdocument();

					this.DocumentAtClient.put(docName,
							viewDocumentsFromServer.get(docName));
					if (!DocumentAtClient.get(docName).currentUsers
							.contains(LOCALHOST_IP)) {
						DocumentAtClient.get(docName).currentUsers
								.add(LOCALHOST_IP);
					}
					opengui = new EditorGUI(docName,
							viewDocumentsFromServer.get(docName), clientObj);
					this.EditorforDocument.put(docName, opengui);
					this.EditorforDocument.get(docName).editorFrame.textArea
							.setText(text);
					this.displayCurrentPeers(docName,
							this.DocumentAtClient.get(docName).currentUsers);

				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}// for the client who already have the document
			else if (!this.EditorforDocument.containsKey(docName)) {

				this.DocumentAtClient.put(docName,
						viewDocumentsFromServer.get(docName));
				if (!DocumentAtClient.get(docName).currentUsers
						.contains(LOCALHOST_IP)) {
					DocumentAtClient.get(docName).currentUsers
							.add(LOCALHOST_IP);

					EditorGUI opengui;
					try {

						opengui = new EditorGUI(docName,
								this.DocumentAtClient.get(docName), clientObj);
						this.EditorforDocument.put(docName, opengui);
						this.EditorforDocument.get(docName).editorFrame.textArea
								.setText(text);
						this.displayCurrentPeers(docName,
								this.DocumentAtClient.get(docName).currentUsers);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block

						e.printStackTrace();
					}

				}
			} else {
				System.out.println("GUI for the same file is already open");
			}

		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NotBoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

	}

	public void leaveFromServer() {
		// TODO Auto-generated method stub
		try {
			serverobj.disconnectUser(LOCALHOST_IP);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// method to connect to the server
	public String connectServer(String serverIP) throws RemoteException,
			NotBoundException, UnknownHostException {
		registry = LocateRegistry.getRegistry(serverIP, 7000);
		serverobj = (serverInterface) registry.lookup("server");
		return serverobj.connect(LOCALHOST_IP);
	}

	// to view all the documents in the network/Server
	public void view(Client clientObj) throws RemoteException,
			NotBoundException {
		viewDocumentsFromServer = serverobj.viewdocument();
		if (viewDocumentsFromServer.isEmpty()) {
			System.out.println("-----------------------------------------");
			System.out.println("No documents at server");
			System.out.println("-----------------------------------------");
		} else {
			Collection<Document> keySet = viewDocumentsFromServer.values();
			Iterator<Document> itr = keySet.iterator();
			// System.out.println("#############################");
			// System.out.println("Documents at Sever:");
			System.out
					.println("-----------------------------------------------------------");
			System.out
					.println("Document Name\tDocCreator\tLastSavedBy\tIscurrentlyinuse");
			while (itr.hasNext()) {
				Document doc = itr.next();
				System.out.print(doc.fileName);
				System.out.print("\t");
				System.out.print(doc.fileCreatorIp);
				System.out.print("\t");
				System.out.print(doc.lastSavedIp);
				System.out.print("\t");
				System.out.println(!doc.currentUsers.isEmpty());
				System.out
						.println("----------------------------------------------------------");

			}
			// System.out.println("##############################");

		}
	}

	// to create a new document
	public void create(String docName1, Client clientObj)
			throws UnknownHostException, RemoteException, NotBoundException {

		boolean isfileCreated = false;
		Document doc = null;

		HashMap<String, Document> hashMap = serverobj.viewdocument();

		// Creating the 1st document of the system
		if (hashMap.isEmpty()) {

			serverobj.createDocument(docName1, LOCALHOST_IP);
			String docName = docName1 + ".txt";
			File file = new File(SystemPara.WIN_LOCAL_PATH, docName);
			try {
				isfileCreated = file.createNewFile();
				// System.out.println(isfileCreated);
			} catch (IOException e) {
				// e.printStackTrace();
				System.out
						.println("File not created at client side. Check the given FilePath");
			}
			if (isfileCreated) {
				doc = new Document(file, docName, LOCALHOST_IP);
				doc.setLastSavedIp(LOCALHOST_IP);
				doc.setCurrentUsers(LOCALHOST_IP);
				DocumentAtClient.put(docName1, doc);
				this.EditorforDocument.put(docName1, new EditorGUI(docName1,
						doc, clientObj));
				this.displayCurrentPeers(docName1,
						this.DocumentAtClient.get(docName1).currentUsers);
			} else {
				System.out
						.println("File not created at client side. Check the given FilePath");
			}

			// write the logic of creating a document at client side and also
			// for GUI part here
			// System.out.println(doc.fileCreatorIp);
			// System.out.println(doc.fileName);
			// System.out.println(doc.currentUsers.get(0));
			// once the GUI file is open and user starts writing keep a
			// track of pressed key and send the data to all the clients
			// connected to that document.

		} else {
			if (hashMap.containsKey(docName1)) {
				System.out.println("Document with this name already exists");
				System.out.println("Document Created by"
						+ hashMap.get(docName1).fileCreatorIp);

			}
			// Creating new documents after the first document.
			else {

				serverobj.createDocument(docName1, LOCALHOST_IP);
				String docName = docName1 + ".txt";
				File file = new File(docName);
				try {
					isfileCreated = file.createNewFile();
					System.out.println(isfileCreated);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("File not created at client side");
				}
				if (isfileCreated) {
					doc = new Document(file, docName, LOCALHOST_IP);
					doc.setLastSavedIp(LOCALHOST_IP);
					doc.setCurrentUsers(LOCALHOST_IP);
					DocumentAtClient.put(docName1, doc);
				} else {
					System.out.println("File Not created at client side");
				}

				// write the logic of creating a document at client side and
				// also
				// for GUI part here
				// System.out.println(doc.fileCreatorIp);
				// System.out.println(doc.fileName);
				// System.out.println(doc.currentUsers.get(0));
				this.EditorforDocument.put(docName1, new EditorGUI(docName1,
						doc, clientObj));
				this.displayCurrentPeers(docName1,
						this.DocumentAtClient.get(docName1).currentUsers);
				// once the GUI file is open and user starts writing keep a
				// track of pressed key and send the data to all the clients
				// connected to that document..
			}
		}
	}

	// join to a particular document
	public void join(String docNametoJoin, Client clientObj)
			throws RemoteException, NotBoundException {
		// getting the current Users of the document
		Document docToJoin = serverobj.joinToDocument(docNametoJoin,
				LOCALHOST_IP);

		// joining to the document
		if (docToJoin != null) {
			try {
				ArrayList<String> currentUsers = docToJoin.getCurrentUsers();
				// System.out
				// .println("Printing current users of the document other than localhost");
				for (int i = 0; i < currentUsers.size(); i++) {
					if (!currentUsers.get(i).equals(LOCALHOST_IP)) {
						// System.out.println(currentUsers.get(i));
						Registry reg = LocateRegistry.getRegistry(
								currentUsers.get(i), 6500);
						clientInterface obj = (clientInterface) reg
								.lookup("client");
						obj.updateNewPeer(docNametoJoin, LOCALHOST_IP);
					}
				}

				this.EditorforDocument.put(docNametoJoin, new EditorGUI(
						docNametoJoin, docToJoin, clientObj));
				this.DocumentAtClient.put(docNametoJoin, docToJoin);

				this.displayCurrentPeers(docNametoJoin,
						this.DocumentAtClient.get(docNametoJoin).currentUsers);

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("No documents with that Name");
		}

		String docCreator = this.DocumentAtClient.get(docNametoJoin).fileCreatorIp;

		// getting connected to co-ordinator if localhost is not doc creator and
		// doc creator is not alive.
		if (!this.DocumentAtClient.get(docNametoJoin).currentUsers
				.contains(docCreator)) {

			if (!this.DocumentAtClient.get(docNametoJoin).currentUsers
					.isEmpty()) {

				String Ip = this.DocumentAtClient.get(docNametoJoin).currentUsers
						.get(0);
				if (!Ip.equals(LOCALHOST_IP)) {
					System.out.println("inside if" + Ip);
					this.syncManager = new SyncManager(clientObj,
							docNametoJoin, docToJoin, Ip);
					t1 = new Thread(syncManager);
					t1.start();
				} else if (!this.DocumentAtClient.get(docNametoJoin).currentUsers
						.get(1).equals(null)) {

					Ip = this.DocumentAtClient.get(docNametoJoin).currentUsers
							.get(1);
					System.out.println("inside else" + Ip);

					this.syncManager = new SyncManager(clientObj,
							docNametoJoin, docToJoin, Ip);
					t1 = new Thread(syncManager);
					t1.start();
				}
			}
		}

		// getting connected to co-ordinator if localhost is not doc creator
		// and doc creator is still alive.
		else if (!this.DocumentAtClient.get(docNametoJoin).fileCreatorIp
				.equals(LOCALHOST_IP)) {
			System.out.println("Document creator alive"
					+ docToJoin.fileCreatorIp);
			this.syncManager = new SyncManager(this, docNametoJoin, docToJoin,
					docToJoin.fileCreatorIp);
			t1 = new Thread(syncManager);
			t1.start();
		}

		// getting connected to co-ordinator if localhost is doc creator

		else if (this.DocumentAtClient.get(docNametoJoin).fileCreatorIp
				.equals(LOCALHOST_IP)) {
			String Ip = this.DocumentAtClient.get(docNametoJoin).currentUsers
					.get(0);
			System.out
					.println("this is joining of creator again and get synchronized."
							+ Ip);
			this.syncManager = new SyncManager(clientObj, docNametoJoin,
					docToJoin, Ip);
			t1 = new Thread(syncManager);
			t1.start();
		}

	}

	private void displayCurrentPeers(String docNametoJoin,
			ArrayList<String> currentUsers) {
		// TODO Auto-generated method stub
		System.out.println("Inside displaying current users.");
		// JLabel infoBoard =
		// this.EditorforDocument.get(docNametoJoin).editorFrame.infoBoard;
		String Ip = "";
		String text = "<html>Peer Info List<br>" + "Current Users:-<br>";
		for (int i = 0; i < currentUsers.size(); i++) {
			Ip = Ip + currentUsers.get(i) + "<br>";
		}
		text = text + Ip + "</html>";
		this.EditorforDocument.get(docNametoJoin).editorFrame.infoBoard
				.setText(text);
	}

	public void synchronize(String docName, String textArea)
			throws RemoteException, NotBoundException, UnknownHostException {
		// System.out.println("Inside Synchronize method client");
		// System.out.println(docName);
		serverobj.synchronize(docName, textArea, LOCALHOST_IP);
	}

	public void sendTextToPeers(int keyword, int caretPos, String docName) {

		// this.managerObj = new SyncManager();
		//
		// managerObj.textSynchronizer(keyword, caretPos);

		ArrayList<String> UserstoSendKeyPressed = DocumentAtClient.get(docName).currentUsers;
		// System.out.println("Users to send the pressed key");
		//
		// for (int i = 0; i < UserstoSendKeyPressed.size(); i++) {
		// System.out.println(UserstoSendKeyPressed.get(i));
		// }

		if (!UserstoSendKeyPressed.isEmpty()) {
			for (int i = 0; i < UserstoSendKeyPressed.size(); i++) {
				try {
					if (!UserstoSendKeyPressed.get(i).equals(LOCALHOST_IP)) {

						// System.out.println("sending pressed key value to user "
						// + UserstoSendKeyPressed.get(i));
						Registry reg = LocateRegistry.getRegistry(
								UserstoSendKeyPressed.get(i), 6500);
						clientInterface obj = (clientInterface) reg
								.lookup("client");
						obj.getText(keyword, caretPos, docName);
					}
					// } else {
					// System.out.println("Local host"
					// + UserstoSendKeyPressed.get(0)
					// + " is the only user");
					// }
				}

				catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (NotBoundException e2) {
					// e2.printStackTrace();
					System.out.println("In the sendTextToPeers");
					System.out.println("Connection refused to"
							+ UserstoSendKeyPressed.get(i));
				}

			}
		} else {
			System.out.println("No current Users at the document");
		}

	}

	// I still have to edit this method.(Done)

	public void syncPeerText(String docName, Document currentDocument) {

		ArrayList currentUsers = this.DocumentAtClient.get(docName).currentUsers;
		Registry peerRegistry;
		String textArea = null;
		clientInterface clientRemoteObject = null;
		try {
			for (int i = 0; i < currentUsers.size(); i++) {

				if (!currentUsers.get(i).equals(LOCALHOST_IP)) {
					// write to code to get connected to all IPs
					// System.out.println("Getting text area from this client"
					// + currentUsers.get(i));
					peerRegistry = LocateRegistry.getRegistry(
							(String) currentUsers.get(i), 6500);
					clientRemoteObject = (clientInterface) peerRegistry
							.lookup("client");

					// System.out.println("Connected to new client");
					textArea = clientRemoteObject.getTextArea(docName,
							LOCALHOST_IP);
					break;
				}
			}

			this.EditorforDocument.get(docName).editorFrame.textArea
					.setText(textArea);
			clientRemoteObject.enableEditorAtPeers(docName);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NotBoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	public String getTextArea(String docName, String sendToThisIp)
			throws RemoteException, NotBoundException {
		// send TextArea of from the editor of that file.
		// this.newGUI.editorFrame.textArea.setEnabled(true);

		String textArea = "";
		if (this.DocumentAtClient != null && this.EditorforDocument != null) {

			ArrayList<String> currentUsers = this.DocumentAtClient.get(docName).currentUsers;
			if (!currentUsers.contains(sendToThisIp)) {
				this.DocumentAtClient.get(docName).currentUsers
						.add(sendToThisIp);
			}
			// this.EditorforDocument.get(docName).editorFrame.textArea
			// .setEnabled(false);
			// System.out.println("Before getting the text area");
			// System.out.println("Getting textArea from the Ip" +
			// LOCALHOST_IP);
			textArea = EditorforDocument.get(docName).editorFrame.textArea
					.getText();
		}
		return textArea;

	}

	@Override
	public synchronized void getText(int keyword, int caretpos, String docName)
			throws RemoteException, NotBoundException {
		// TODO Auto-generated method stub
		try {
			int currentcareat = this.EditorforDocument.get(docName).editorFrame.textArea
					.getCaretPosition();
			if (keyword == 8) {

				if (caretpos < currentcareat) {
					currentcareat = currentcareat
							- Character.toString((char) keyword).length();
				}
				String textArea = this.EditorforDocument.get(docName).editorFrame.textArea
						.getText();
				if (textArea.length() >= 0) {
					String subText = textArea.substring(0, caretpos)
							+ textArea.substring(caretpos + 1,
									textArea.length());
					this.EditorforDocument.get(docName).editorFrame.textArea
							.setText(subText);

					this.EditorforDocument.get(docName).editorFrame.textArea
							.setCaretPosition(currentcareat);
				}

			} else {

				if (caretpos < currentcareat) {
					currentcareat = currentcareat
							+ Character.toString((char) keyword).length();
				}

				if (caretpos >= 0) {
					this.EditorforDocument.get(docName).editorFrame.textArea
							.insert(Character.toString((char) keyword),
									caretpos);
				}
				this.EditorforDocument.get(docName).editorFrame.textArea
						.setCaretPosition(currentcareat);
			}
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("");
		} catch (IllegalArgumentException e) {
			System.out.println("");
		}

	}

	@Override
	public void enableEditorAtPeers(String docName) throws RemoteException,
			NotBoundException {
		// TODO Auto-generated method stub
		this.EditorforDocument.get(docName).editorFrame.textArea
				.setEnabled(true);
		// this.newGUI.editorFrame.textArea.setEnabled(true);

	}

	// calls the server method and remove user methods in other client
	public void closeDocument(String docName) {

		System.out.println("Inside the close method of client");
		ArrayList<String> currentUsers = null;
		// asking sever to remove my ip address from currentusers
		if (this.DocumentAtClient.get(docName).currentUsers
				.contains(LOCALHOST_IP)
				&& this.EditorforDocument.containsKey(docName)) {

			try {
				System.out.println("Asking server to remove" + LOCALHOST_IP);
				serverobj.closeDocument(docName, LOCALHOST_IP);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			currentUsers = this.DocumentAtClient.get(docName).currentUsers;
		}
		// asking other peers to remove my IP from currents users of document
		// and calling removeUserFromDoc method(remote method of peers).
		if (!currentUsers.isEmpty()) {
			for (int i = 0; i < currentUsers.size(); i++) {
				if (!currentUsers.get(i).equals(LOCALHOST_IP)) {
					System.out.println("Asking to remove" + LOCALHOST_IP
							+ "from current users of " + currentUsers.get(i));
					Registry reg;
					try {
						reg = LocateRegistry.getRegistry(currentUsers.get(i),
								6500);
						clientInterface obj = (clientInterface) reg
								.lookup("client");
						obj.removeUserFromDoc(docName, LOCALHOST_IP);

					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}

		if (this.syncManager != null) {
			this.syncManager.flag = false;
		}

		this.DocumentAtClient.get(docName).currentUsers.remove(LOCALHOST_IP);
		this.EditorforDocument.remove(docName);
	}

	// Removes the IPaddress from the currentUsers list in the document
	// 'docName'
	public void removeUserFromDoc(String docName, String IpAddress) {

		System.out
				.println("inside remove user from document method and ip to remove"
						+ IpAddress);
		System.out.println(t1);

		if (t1 != null) {

			if (t1.isAlive()) {
				this.syncManager.flag = false;
			}
			Document doc = this.DocumentAtClient.get(docName);
			// doc.currentUsers.remove(IpAddress);
			this.EditorforDocument.get(docName).editorFrame.textArea
					.setEnabled(false);
			doc.currentUsers.remove(IpAddress);
			this.EditorforDocument.get(docName).editorFrame.textArea
					.setEnabled(true);
			this.displayCurrentPeers(docName,
					this.DocumentAtClient.get(docName).currentUsers);
			if (!this.DocumentAtClient.get(docName).currentUsers.get(0).equals(
					LOCALHOST_IP)) {
				this.findNewPeertoSynchronize(docName);
			}

		} else {
			Document doc = this.DocumentAtClient.get(docName);
			// doc.currentUsers.remove(IpAddress);
			this.EditorforDocument.get(docName).editorFrame.textArea
					.setEnabled(false);

			doc.currentUsers.remove(IpAddress);
			this.displayCurrentPeers(docName,
					this.DocumentAtClient.get(docName).currentUsers);
			this.EditorforDocument.get(docName).editorFrame.textArea
					.setEnabled(true);
		}
	}

	// finding new co-ordinator to get synchronized frequently
	private void findNewPeertoSynchronize(String docNametoJoin) {
		// TODO Auto-generated method stub
		try {
			System.out.println("Inside find New Peer method");
			Document docToJoin = this.DocumentAtClient.get(docNametoJoin);
			String Ip = this.DocumentAtClient.get(docNametoJoin).currentUsers
					.get(0);
			System.out.println("IP here is(he will be new coordinator) " + Ip);
			t1 = new Thread(
					new SyncManager(this, docNametoJoin, docToJoin, Ip),
					"synThread");
			t1.start();
		} catch (Exception e) {
			System.out.println("Cating the exception.");
		}
	}

	@Override
	public void updateNewPeer(String docNametoJoin, String Ip)
			throws RemoteException, NotBoundException {
		// TODO Auto-generated method stub
		System.out.println(docNametoJoin);
		if (!this.DocumentAtClient.get(docNametoJoin).currentUsers.contains(Ip)) {
			// this.EditorforDocument.get(docNametoJoin).editorFrame.textArea
			// .setEnabled(false);
			this.DocumentAtClient.get(docNametoJoin).currentUsers.add(Ip);
			this.displayCurrentPeers(docNametoJoin,
					this.DocumentAtClient.get(docNametoJoin).currentUsers);
			// this.EditorforDocument.get(docNametoJoin).editorFrame.textArea
			// .setEnabled(true);

		}
	}

}
