
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;


import java.io.DataInputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {

	private static Socket socket;

 
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

			if (command == "") continue;
			out.writeUTF(command);
			commandCenter(command, in);

			TimeUnit.MILLISECONDS.sleep(100);

			while(in.available() != 0)
			{
				String serverResponse = in.readUTF();
				if (serverResponse.isEmpty()) break;
				System.out.println(serverResponse);
			}
		}
	}

	public static void commandCenter(String command, DataInputStream in) {
		String[] consoleInput = command.split(" ");

		if(consoleInput.length != 0) {
			switch(consoleInput[0]) {
				case "cd":
					// Todo cd function
					// cd(consoleInput[1]);
					break;
				case "ls":
					// Todo ls function
					// ls();
					break;
				case "mkdir":
					// Todo mkdir function
					// mkdir(consoleInput[1]);
					break;
				case "delete":
					// Todo delete function
					// delete(consoleInput[1]);
					break;
				case "upload":
					// Todo upload function
					// upload(consoleInput[1]);
					break;
				case "download":
					Boolean zip = false;
					if(consoleInput[2] =="-Z"){
						zip = true;
					}
					// Todo Download function
					// download(consoleInput[1], zip);
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
}
