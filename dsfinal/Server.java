import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Server extends UnicastRemoteObject implements serverInterface,
		Serializable {

	ArrayList<String> nodeList = new ArrayList<String>();
	// key is filename which is unique and respective document object as value
	HashMap<String, Document> hashMap = new HashMap<String, Document>();
	boolean event = false;

	protected Server() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws RemoteException {
		Server obj = new Server();
		Registry registry = LocateRegistry.createRegistry(7000);
		registry.rebind("server", obj);
		System.out.println("Server Started");

		/*
		 * System.out.println("Give me the Ip Address of the Secondary server");
		 * Scanner sc = new Scanner(System.in); Registry registry1 =
		 * LocateRegistry.getRegistry(sc.next(), 7000); serverInterface
		 * secondaryServerObj; try { secondaryServerObj = (serverInterface)
		 * registry1 .lookup("secondaryserver");
		 * 
		 * while (obj.event) { // call secondary server
		 * secondaryServerObj.updateDocuments(obj.nodeList, obj.hashMap); } }
		 * catch (NotBoundException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	}

	@Override
	public String connect(String Ipaddress) {

		if (nodeList.contains(Ipaddress)) {
			return "Client already in the List";
		} else {
			nodeList.add(Ipaddress);
			System.out
					.println("New Client with IP " + Ipaddress + " connected");
			this.event = true;
			return "connected";
		}
	}

	@Override
	public HashMap<String, Document> viewdocument() {

		return hashMap;
	}

	@Override
	public void createDocument(String docName, String IpAddress) {

		String docName1 = docName + ".txt";

		File file = new File(SystemPara.LINUX_SERVER_PATH, docName1);
		boolean isfileCreated = false;
		try {
			isfileCreated = file.createNewFile();
			System.out.println("Is file created " + isfileCreated);

			if (isfileCreated) {
				// System.out.println("File name while creating at server "
				// + docName);
				Document doc = new Document(file, docName1, IpAddress);
				doc.setLastSavedIp(IpAddress);
				doc.currentUsers.add(IpAddress);
				hashMap.put(docName, doc);
				// System.out.println(hashMap.get(docName).currentUsers.get(0));
				this.event = true;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Document joinToDocument(String docNametojoin, String IpAddress) {

		// System.out.println("File name while joining" + docNametojoin);

		if (hashMap.containsKey(docNametojoin)) {

			System.out.println("Server contains the file");
			// System.out.println("Ip address of the client who needs to join"
			// + IpAddress);
			ArrayList<String> currentUsers = hashMap.get(docNametojoin).currentUsers;

			if (!currentUsers.contains(IpAddress)) {
				hashMap.get(docNametojoin).currentUsers.add(IpAddress);
				if (hashMap.get(docNametojoin).currentUsers.contains(IpAddress))
					System.out.println(IpAddress + "Added to the document"
							+ docNametojoin);

			}

			this.event = true;
			return hashMap.get(docNametojoin);
		} else {
			return null;
		}

	}

	public void synchronize(String docName, String textArea, String SavedbyIp) {
		String fileName = docName + ".txt";

		// savedFile is the new will from synchronize which replaces already
		// existing file at server
		try {
			File savedFile = new File(SystemPara.LINUX_SERVER_PATH, fileName);
			PrintWriter log = new PrintWriter(new FileWriter(savedFile));
			log.println(textArea);
			log.close();

			// System.out
			// .println("In synchronize method and printing last IP who synchronized to server");
			// System.out.println(SavedbyIp);

			if (!hashMap.containsKey(docName)) {
				hashMap.put(docName, new Document(savedFile, fileName,
						SavedbyIp));
				hashMap.get(docName).setLastSavedIp(SavedbyIp);
				if (!hashMap.get(docName).currentUsers.contains(SavedbyIp))
					hashMap.get(docName).currentUsers.add(SavedbyIp);
			} else {
				hashMap.get(docName).setLastSavedIp(SavedbyIp);
				if (!hashMap.get(docName).currentUsers.contains(SavedbyIp))
					hashMap.get(docName).currentUsers.add(SavedbyIp);
			}
			this.event = true;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Check for the Path given to save file.");
		}
	}

	// This method is written for Secondary server. Does Nothing in Main Server.
	@Override
	public void updateDocuments(ArrayList<String> nodelist,
			HashMap<String, Document> hashmap) throws RemoteException,
			NotBoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeDocument(String docName, String IpAddress)
			throws RemoteException, NotBoundException {
		// TODO Auto-generated method stub
		this.hashMap.get(docName).currentUsers.remove(IpAddress);
		// System.out.println("current users of the document after removing"
		// + IpAddress);
		// for (int i = 0; i < this.hashMap.get(docName).currentUsers.size();
		// i++) {
		// System.out.println(this.hashMap.get(docName).currentUsers.get(i));
		// }

	}

	@Override
	public void disconnectUser(String IpAddress) throws RemoteException,
			NotBoundException {
		// TODO Auto-generated method stub
		if (nodeList.contains(IpAddress))
			this.nodeList.remove(IpAddress);

	}

	@Override
	public String openFile(String fileName, String Ip) throws RemoteException,
			NotBoundException {

		String filenametxt = fileName + ".txt";
		String text = "";

		ArrayList<String> line = new ArrayList<String>();

		// BufferedReader br = new BufferedReader(new FileReader(
		// "/home/stu12/s12/drk4074/Dsfinalproj/filesatserver/"
		// + fileName + ".txt"));
		//
		// while ((text = br.readLine()) != null) {
		// line.add(text);
		// text = "";
		// for (int i = 0; i < line.size(); i++) {
		// text += line.get(i);
		// }
		// br.close();
		// }

		// System.out.println("Inside open server");
		Scanner fileReader;
		try {
			System.out.println(filenametxt);
			fileReader = new Scanner(new File(
					"/home/stu12/s12/drk4074/Dsfinalproj/filesatserver/"
							+ filenametxt));
			// System.out.println(fileReader);
			while (fileReader.hasNext()) {
				// System.out.println("Inside while loop");

				String item = fileReader.nextLine();
				// Print or store the string, read in the next line
				// System.out.println("Inside while loop");
				// System.out.println("First thing: " + item);
				line.add(item);
			}
			for (int i = 0; i < line.size(); i++) {
				text = text + line.get(i) + "\n";
			}
			fileReader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("File Path not found");
		}

		if (!this.hashMap.get(fileName).currentUsers.contains(Ip)) {
			this.hashMap.get(fileName).currentUsers.add(Ip);
		}

		// catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// System.out.println("File Not found at the server");
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// System.out
		// .println("Input Output Exception at the Sever to open the file.");
		// e.printStackTrace();
		// }

		return text;
		// finally {
		// System.out.println("File not open");
		// }

	}
}
