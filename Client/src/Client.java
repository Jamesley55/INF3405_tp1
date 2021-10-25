
import javafx.scene.SubScene;
import sun.lwawt.macosx.CSystemTray;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Client {

	private static Socket socket;
	private static String currentRep;


	public static void main(String []args) throws Exception {


		String serverAdress = "127.0.0.1";
		int port = 5002;


		Scanner inputScanner = new Scanner(System.in);

		// Input et validation de l'adresse IP
		System.out.println(" Enter the server's IP address:");

		serverAdress = inputScanner.nextLine();
		while (!Client.validIPAddress(serverAdress)) {
			System.out.println(" Wrong IP Address. Try again");
			serverAdress = System.console().readLine();
		}

		System.out.println("Enter the server's port:");
		port = inputScanner.nextInt();
		while (!Client.validatePort(port)) {
			System.out.println("Invalid port. Try again.");
			port = Integer.parseInt(System.console().readLine());
		}

		socket = new Socket(serverAdress, port);

		System.out.format("The server is running on %s:%d%n",serverAdress , port);

		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		System.out.println(in.readUTF());

		while (true) {
			String command = inputScanner.nextLine();

			if (command.isEmpty()) continue;
			out.writeUTF(command); // Envoie vers le serveur
			commandCenter(command, in, out);

			TimeUnit.MILLISECONDS.sleep(100);

			while(in.available() != 0)
			{
				String serverResponse = in.readUTF();
				if (serverResponse.isEmpty()) break;
				System.out.println(serverResponse);
			}
		}
	}

	public static void commandCenter(String command, DataInputStream in , DataOutputStream out) throws  Exception {
		String[] consoleInput = command.split(" ");

		if(consoleInput.length != 0) {
			switch(consoleInput[0]) {
				case "upload":
					// Todo Download function
					upload(consoleInput[1] , out);
					break;
				case "download":

					// Todo Download function
					boolean isArchived = false;
					if(consoleInput.length == 3){
						isArchived = true;
					}

					download(consoleInput[1],isArchived, in);
					break;
				case "exit":
					System.exit(0);
					break;
				default:
					break;
			}
		} else return;

	}

	public static boolean validatePort(final int port) {
		return port >= 5002 && port <= 5049;
	}
	public static Boolean validIPAddress(String IP) {
		try {
			if (InetAddress.getByName(IP) instanceof InetAddress) {
				return true;
			};
		} catch(Exception e) {
			return false;
		}
		return false;
	}

	public static void upload(String nomFichier, DataOutputStream out ) throws Exception{

		String currentRep = System.getProperty("user.dir");
		File  newFile = new File(currentRep + "/" + nomFichier);

		InputStream uploadedFile = new FileInputStream(newFile);
		int packetSize;

		int fileLenght = (int) newFile.length();
		out.writeInt(fileLenght);

		byte [] buffer = new byte[fileLenght];
		while((packetSize = uploadedFile.read(buffer)) != -1){
			out.write(buffer, 0, packetSize);

		}
		uploadedFile.close();
	}

	public static void download(String nomFichier, boolean isArchived, DataInputStream in) throws Exception{

		String currentRep = System.getProperty("user.dir");
		DataInputStream fin =  new DataInputStream(socket.getInputStream());

		if(isArchived){
			nomFichier = nomFichier.substring(0, nomFichier.indexOf(".")) + ".zip";
		}
		System.out.println("nom Fichier " + nomFichier);
		File  newFile = new File(currentRep + "/" + nomFichier);
		FileOutputStream fileOut = new FileOutputStream(newFile);

		if(isArchived) {
			FileOutputStream zipFile = new FileOutputStream(currentRep + "/" + nomFichier);
			ZipOutputStream zipFileOutput = new ZipOutputStream(zipFile);
			zipFileOutput.putNextEntry(new ZipEntry(newFile.getName())); // copies les byte de ran.jpeg dans le fichier zip
		}
		int size = in.readInt();
		byte[] buffer = new byte[size];
		int sizeOfPacket;
		while ((sizeOfPacket = fin.read(buffer)) != -1) {   // store packet de donne dans le fichier
			if(size <= 0)
				break;
			size -= sizeOfPacket;
			fileOut.write(buffer, 0, sizeOfPacket);   // store les donne contenue dans le buffer et le met dans le fichier "newFile" cree
		}
		fileOut.flush();
		fileOut.close();
	}
}
