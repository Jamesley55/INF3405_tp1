import com.sun.tools.internal.ws.wsdl.document.Output;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Server {

    private static ServerSocket listener;


    public static void main(String[] args) throws Exception {

        int clientNumber = 0;

        String serverAdress = "127.0.0.1";
        int serverPort = 5002;

        Scanner inputScanner = new Scanner(System.in);

        // Input et validation de l'adresse IP
        System.out.println(" Enter the server's IP address:");
        System.out.println(System.getProperty("user.dir"));

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
    String serverAdress = "127.0.0.1";
    int serverPort = 5002;
    private int clientNumber;
    private String currentRep = System.getProperty("user.dir");

    public ClientHandler(Socket socket, int clientNumber, String serverAdress, int serverPort) {

        this.socket = socket;
        this.clientNumber = clientNumber;
        this.serverAdress = serverAdress;
        this.serverPort = serverPort;
        System.out.println("New connection with client#" + clientNumber + " at " + socket);

    }

    public void commandCenter(DataInputStream in, DataOutputStream out) throws Exception {
        // Date et temps pour l'affichage
        LocalDate date = java.time.LocalDate.now();
        int hours = java.time.LocalTime.now().getHour(), minutes = java.time.LocalTime.now().getMinute(),
                seconds = java.time.LocalTime.now().getSecond();

        while (true) {
            String[] clientInputs = new String[]{};
            String input = "";
            try {
                input = in.readUTF();
                clientInputs = input.split(" ");
                System.out.println("[" + this.serverAdress + ":" + this.serverPort + " - " + date + "@" + hours + ":" + minutes + ":" + seconds + "] : " + input);
            } catch (Exception e) {
            }
            switch (clientInputs[0]) {
                case "cd":
                    if (clientInputs.length == 1) {
                        out.writeUTF("Veuillez entree un nom de fichier");
                    } else {
                        cd(clientInputs[1], out);
                    }
                    break;
                case "ls":
                    ls(out, false);
                    break;
                case "mkdir":
                    mkdir(clientInputs[1], out);

                    break;
                case "delete":
                    delete(clientInputs[1], out);

                    break;
                case "upload":
                    upload(clientInputs[1], in);
                    break;
                case "download":
                    if (clientInputs.length == 3 && clientInputs[2].contains("-z")) {

                        Zip(clientInputs[1], out);
                    } else if(clientInputs.length == 3 && !clientInputs[2].contains("-z")) {

                        out.writeUTF("le format de fichier que vous voulez n'existe pas \n");
                   }else{
                        download(clientInputs[1], out);
                    }
                    break;
                default:
                    out.writeUTF("Command not found, please try again.\n");
                    break;
            }
            input = "";
            clientInputs = new String[]{};
        }


    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Hello from server- you are client#" + clientNumber);
            commandCenter(in, out);
            clientNumber++;

        } catch (Exception e) {
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

    public void cd(String name, DataOutputStream out) throws Exception {
        if (name.startsWith("..")) {
            String[] folderBackward = name.split("/");
            String[] splitPath = currentRep.split("/");
            String newPath = "";

            int nBack = splitPath.length - folderBackward.length;
            for (int i = 0; i < nBack; i++) {
                newPath += "/" + splitPath[i];
            }

            currentRep = newPath;
        } else if (ls(out, true).contains(name)) {

            currentRep += "/" + name;
        } else {
            out.writeUTF(name + "ce dossier n'est pas present dans le Dossier parent");
            return;
        }
        out.writeUTF("Vous êtes dans le dossier " + name);
    }

    public List<String> ls(DataOutputStream out, boolean isLookout) throws Exception {
        File newRep = new File(currentRep);
        File[] list = newRep.listFiles();
        List<String> lsList = new ArrayList<String>();

        for (int i = 0; i < list.length; i++) {
            if (isLookout) {
                if (list[i].isDirectory())
                    lsList.add(list[i].getName());
            } else {
                if (list[i].isDirectory()) {
                    out.writeUTF("[Folder] " + list[i].getName());

                } else {
                    out.writeUTF("[File] " + list[i].getName());

                }
            }
        }
        return lsList;
    }

    public void mkdir(String name, DataOutputStream out) throws Exception {

        File newFile = new File(currentRep + "/" + name);
        if (newFile.mkdir()) {
            out.writeUTF("Le dossier " + name + " a ete cree");
        } else {
            out.writeUTF("Error: aucun dossier n'as ete cree. Essayer encore ");
        }
    }

    public void delete(String name, DataOutputStream out) throws Exception {

        if (!ls(out, true).contains(name)) {
            out.writeUTF("Aucun dossier ou fichier de ce nom existe");
            return;
        }
        File newRep = new File(currentRep + "/" + name);
        newRep.delete();
        out.writeUTF("Le dossier " + name + " a été supprime.");
    }

    public void upload(String nomFichier, DataInputStream in) throws Exception {


        System.out.println(currentRep + "/" + nomFichier);

        File newFile = new File(currentRep + "/" + nomFichier);
        FileOutputStream fileOut = new FileOutputStream(newFile);
        int size = in.readInt();
        byte[] buffer = new byte[size];
        int sizeOfPacket;
        DataInputStream fin =  new DataInputStream(socket.getInputStream());
        while ((sizeOfPacket = fin.read(buffer)) != -1) {
            if(size <= 0)
                break;
            size -= sizeOfPacket;
            fileOut.write(buffer, 0, sizeOfPacket); // store les donne contenue dans le buffer et le met dans le fichier "newFile" cree
        }
        fileOut.close();
    }

    public void download(String nomFichier, DataOutputStream out) throws Exception {

        File newFile = new File(currentRep + "/" + nomFichier);
        InputStream uploadedFile = new FileInputStream(newFile);

        int packetSize;
        int fileLenght = (int) newFile.length();
        out.writeInt(fileLenght);

        byte [] buffer = new byte[fileLenght];

        while ((packetSize = uploadedFile.read(buffer)) != -1) {
            out.write(buffer, 0, packetSize);
            System.out.println("size " + packetSize);
        }
        out.writeInt(-1);
        uploadedFile.close();
    }

    public void Zip(String nomFichier, DataOutputStream out) throws Exception {

        File newFile = new File(currentRep + "/" + nomFichier);
        String zipFileName = nomFichier.substring(0, nomFichier.indexOf(".")) + ".zip";


        FileOutputStream zipFile = new FileOutputStream(currentRep + "/" + zipFileName);
        ZipOutputStream zipFileOutput = new ZipOutputStream(zipFile);
        zipFileOutput.putNextEntry(new ZipEntry(newFile.getName())); // copies les byte de ran.jpeg dans le fichier zip

        InputStream fileToZip = new FileInputStream(newFile);


        byte [] buffer = new byte[(int) newFile.length()];
        int packetSize;
        while ((packetSize = fileToZip.read(buffer)) != -1) {
            zipFileOutput.write(buffer, 0, packetSize);

        }
        zipFileOutput.flush();
        zipFileOutput.closeEntry();
        zipFileOutput.close();
        fileToZip.close();
        download(zipFileName, out);
        delete(zipFileName, out);
    }

}