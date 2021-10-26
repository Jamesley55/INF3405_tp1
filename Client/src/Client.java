/**
 * Auteurs : Jamesley Joseph (1990552) , Manel Ben jemaa (1871842)  , Alex Hua (1994253)
 * Description: Serveur qui receveras des commandes du client
 *             dans le but de stocker des donnees ou de naviger
 *             dans un repertoire
 *  Date : 25 Octobre 2021
 **/
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Client {

	private static Socket socket;
	private static String currentRep = System.getProperty("user.dir");;


	public static void main(String []args) throws Exception {


		String serverAdress = "127.0.0.1";
		int port = 5002;


		Scanner inputScanner = new Scanner(System.in);

		System.out.println("Entrer l'adresse IP du serveur:");

		serverAdress = inputScanner.nextLine();
		while (!Client.validIPAddress(serverAdress)) {
			System.out.println("Adresse IP Invalide. Essayer de nouveau");
			serverAdress = System.console().readLine();
		}

		System.out.println("Veuilliez entrer le port du serveur:");
		port = inputScanner.nextInt();
		while (!Client.validatePort(port)) {
			System.out.println("port Invalide.Essayer de nouveau");
			port = Integer.parseInt(System.console().readLine());
		}

		socket = new Socket(serverAdress, port);

		System.out.format("Connection sur %s:%d%n",serverAdress , port);

		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		System.out.println(in.readUTF());

		while (true) {
			String command = inputScanner.nextLine();

			if (command.isEmpty()) continue;
			out.writeUTF(command);
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


	/**
	 * Fonction qui dicte quelle fonction appeler en fonction de input du client
	 * \param [DataInputStream: le input stream vers lequelle le client recevra les donnees du serveur ,
	 *        DataOutputStream: le output stream vers lequelle le serveur recevra les donnees du client]
	 * \return void
	 */
	public static void commandCenter(String command, DataInputStream in , DataOutputStream out) throws  Exception {
		String[] consoleInput = command.split(" ");

		if(consoleInput.length != 0) {
			switch(consoleInput[0]) {
				case "upload":
					upload(consoleInput[1] , out);
					break;
				case "download":
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

	/**
	 * Determiner si le port passer en parametre est valide
	 * \param [int: port]
	 * \return un boolean qui definie sur le port est valide ou non
	 */
	public static boolean validatePort(final int port) {
		return port >= 5002 && port <= 5049;
	}

	/**
	 * Determiner si l'adresse IP passer en parametre est valide
	 * \param [String: IP adress]
	 * \return un boolean qui definie sur l'adress IP est valide ou non
	 */
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

	/**
	 * \Description fonction qui permet de televerser un fichier du client vers le serveur de stockage
	 * \param String: le nom du nouveau du fichier a televerser
	 * \param DataOutputStream: le output stream vers lequelle le serveur envoyeras des donnees vers le client
	 * \return void
	 */
	public static void upload(String nomFichier, DataOutputStream out ) throws Exception{

		File  newFile = new File(currentRep + "/" + nomFichier);

		InputStream uploadedFile = new FileInputStream(newFile);
		int packetSize;

		int fileLenght = (int) newFile.length();
		out.writeInt(fileLenght);

		byte [] buffer = new byte[fileLenght];
		while((packetSize = uploadedFile.read(buffer)) != -1){
			out.write(buffer, 0, packetSize);
		}
		out.flush();
		uploadedFile.close();
	}

	/**
	 * \Description fonction qui permet de telecharger un fichier du serveur de stockage vers le client
	 * \param String: le nom du nouveau du fichier a televerser
	 * \param DataOutputStream: le output stream vers lequelle le serveur envoyeras des donnees vers le client
	 * \return void
	 */
	public static void download(String nomFichier, boolean isArchived, DataInputStream in) throws Exception{

		DataInputStream fin =  new DataInputStream(socket.getInputStream());

		if(isArchived){
			nomFichier = nomFichier.substring(0, nomFichier.indexOf(".")) + ".zip";
		}

		File  newFile = new File(currentRep + "/" + nomFichier);
		FileOutputStream fileOut = new FileOutputStream(newFile);

		if(isArchived) {
			FileOutputStream zipFile = new FileOutputStream(currentRep + "/" + nomFichier);
			ZipOutputStream zipFileOutput = new ZipOutputStream(zipFile);
			zipFileOutput.putNextEntry(new ZipEntry(newFile.getName()));
		}

		int size = in.readInt();
		byte[] buffer = new byte[size];
		int sizeOfPacket;

		while ((sizeOfPacket = fin.read(buffer)) != -1) {
			if(size <= 0)
				break;
			size -= sizeOfPacket;
			fileOut.write(buffer, 0, sizeOfPacket);
		}
		fileOut.flush();
		fileOut.close();
	}
}
