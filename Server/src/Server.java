import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Scanner;


public class Server {

    private static ServerSocket listener;


    public static void main(String[] args) throws Exception {

        int clientNumber = 0;

        String serverAdress = "127.0.0.1";
        int serverPort = 5002;

        Scanner inputScanner = new Scanner(System.in);

        // Input et validation de l'adresse IP
        System.out.println(" Enter the server's IP address:");

        serverAdress = inputScanner.nextLine();
        while (!Server.validIPAddress(serverAdress)) {
            System.out.println(" Wrong IP Address. Try again");
            serverAdress = System.console().readLine();
        }

        System.out.println("Enter the server's port:");
        serverPort = inputScanner.nextInt();
        while (!Server.validatePort(serverPort)) {
            System.out.println("Invalid port. Try again.");
            serverPort = Integer.parseInt(System.console().readLine());
        }

        listener = new ServerSocket();
        listener.setReuseAddress(true);

        InetAddress serverIP = InetAddress.getByName(serverAdress);

        listener.bind(new InetSocketAddress(serverIP, serverPort));

        System.out.format("The server is running on %s:%d%n", serverAdress, serverPort);

        try {
            while (true) {
                new ClientHandler(listener.accept(), clientNumber++, serverAdress, serverPort).start();
            }
        } finally {
            listener.close();
        }

    }

    public static boolean validatePort(final int port) {
        return port >= 5002 && port <= 5049;
    }

    public static Boolean validIPAddress(String IP) {
        try {
            if (InetAddress.getByName(IP) instanceof InetAddress) {
                return true;
            }
		} catch (Exception e) {
            return false;
        }
        return false;
    }

}

class ClientHandler extends Thread {

    private final Socket socket;
    private int clientNumber;
	String serverAdress = "127.0.0.1";
	int serverPort = 5002;

    public ClientHandler(Socket socket, int clientNumber, String serverAdress, int serverPort) {
        this.socket = socket;
        this.clientNumber = clientNumber;
		this.serverAdress = serverAdress;
		this.serverPort = serverPort;
        System.out.println("New connection with client#" + clientNumber + " at " + socket);

    }

    public void commandCenter(DataInputStream in, DataOutputStream out) throws IOException {
        // Date et temps pour l'affichage
        LocalDate date = java.time.LocalDate.now();
        int hours = java.time.LocalTime.now().getHour(), minutes = java.time.LocalTime.now().getMinute(),
                seconds = java.time.LocalTime.now().getSecond();

        while (true) {
			String[] clientInputs = new String[] {};
			String input = "";
			try {
				input = in.readUTF();
				clientInputs = input.split(" ");
				System.out.println("[" + this.serverAdress + ":" + this.serverPort + " - " + date + "@" + hours + ":" + minutes + ":" + seconds + "] : " + input);
			} catch (Exception e) {}
            switch (clientInputs[0]) {
                case "cd":
                    // Todo cd function
                    // cd(clientInputs[1], out);
                    break;
                case "ls":
                    // Todo ls function
                    // ls(out);
                    break;
                case "mkdir":
                    // Todo mkdir function
                    // mkdir(clientInputs[1], out);
                    break;
                case "delete":
                    // Todo delete function
                    // delete(clientInputs[1], out);
                    break;
                case "upload":
                    // Todo upload function
                    // upload(clientInputs[1], out);
                    break;
                case "download":

                    // Todo Download function
                    // download(clientInputs[1], clientInputs[2], out);
                    break;
				default:
					out.writeUTF("Command not found, please try again.\n");
					break;
            }
			input = "";
			clientInputs = new String[] {};
        }


    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Hello from server- you are client#" + clientNumber);
            commandCenter(in, out);
            clientNumber++;

        } catch (IOException e) {
            System.out.println("Error handling client#" + clientNumber + " : " + e);

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");

            }
            System.out.println("Connection with client#" + clientNumber + "closed");
        }
    }
}