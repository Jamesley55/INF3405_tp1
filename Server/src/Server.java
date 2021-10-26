/**
* Auteurs : Jamesley Joseph (1990552) , Manel Ben jemaa (1871842)  , Alex Hua (1994253)
* Description: Serveur qui receveras des commandes du client
*             dans le but de stocker des donnees ou de naviger
*             dans un repertoire
*  Date : 25 Octobre 2021
**/
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
        System.out.println("Entrer l'adresse IP du serveur:");
        System.out.println(System.getProperty("user.dir"));

        serverAdress = inputScanner.nextLine();
        while (!Server.validIPAddress(serverAdress)) {
            System.out.println("Adresse IP Invalide. Essayer de nouveau");
            serverAdress = System.console().readLine();
        }

        System.out.println("Veuilliez entrer le port du serveur:");
        serverPort = inputScanner.nextInt();
        while (!Server.validatePort(serverPort)) {
            System.out.println("port Invalide.Essayer de nouveau");
            serverPort = Integer.parseInt(System.console().readLine());
        }
        listener = new ServerSocket();
        listener.setReuseAddress(true);

        InetAddress serverIP = InetAddress.getByName(serverAdress);

        listener.bind(new InetSocketAddress(serverIP, serverPort));

        System.out.format("Connection sur %s:%d%n", serverAdress, serverPort);

        try {
            while (true) {
                new ClientHandler(listener.accept(), clientNumber++, serverAdress, serverPort).start();
            }
        } finally {
            listener.close();
        }

    }


    /**
     * Determiner si le port passer en parametre est valide
     * \param int: port
     * \return un boolean qui definie sur le port est valide ou non
     */
    public static boolean validatePort(final int port) {
        return port >= 5002 && port <= 5049;
    }

    /**
     * Determiner si l'adresse IP passer en parametre est valide
     * \param String: IP adress
     * \return un boolean qui definie sur l'adress IP est valide ou non
     */
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
        System.out.println("Nouveau connection avec le client#" + clientNumber + " a " + socket);

    }

    /**
     * Fonction qui dicte quelle fonction appeler en fonction de input recu du client
     * \param DataInputStream: le input stream vers lequelle le client envoyera des donnees vers le serveur
     * \param DataOutputStream: le output stream vers lequelle le serveur envoyeras des donnees vers le client
     * \return void
     */
    public void commandCenter(DataInputStream in, DataOutputStream out) throws Exception {
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
                    upload(clientInputs[1], in, out);
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


    /**
     * \Description Fonction qui permet de cree un socket du serveur vers le client et donne des updates lorsqu'un erreur se produit.
     * \return void
     */
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Bonjour du serveur- vous etes le client#" + clientNumber);
            commandCenter(in, out);
            clientNumber++;

        } catch (Exception e) {
            System.out.println("Client#"+ clientNumber + " a ete deconnecter");

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Impossibilite de fermer le socket!");

            }
            System.out.println("Connection avec le client#" + clientNumber + "fermer");
            clientNumber -= 1;
        }

    }
    /**
     *  \Description Fonction qui permet se deplacer dans un repertoire enfant d'un repertoir du serveur de stockage
     *  \param String: le nom du serveur auquelle ont veut se deplacer
     *  \param DataOutputStream: le output stream vers lequelle le serveur envoyeras des donnees vers le client
     * \return void
     */
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
            int lenght = newPath.split("/").length;
            name = newPath.split("/")[lenght -1];
        } else if (ls(out, true).contains(name)) {

            currentRep += "/" + name;
        } else {
            out.writeUTF(name + "ce dossier n'est pas present dans le Dossier parent");
            return;
        }
        out.writeUTF("Vous etes dans le dossier " + name);
    }

    /**
     *  \Description Fonction qui permet de chercher ou d'afficher tous les fichiers contenu dans le repertoir
     *               que le serveur de stockage se trouve actuellement
     *  \param DataOutputStream: le output stream vers lequelle le serveur envoyeras des donnees vers le client
     *  \param boolean : un parametre qui sert a savoir si on veut affichier le contenue au niveau du client
     *         ou si on veut faire un simple recherche au niveau du serveur. mis a False par default
     * \return List<String> : la liste des fichier se trouvant dans ce repertoire
     */
    public List<String> ls(DataOutputStream out, boolean isLookout) throws Exception {
        File newRep = new File(currentRep);
        File[] list = newRep.listFiles();
        List<String> lsList = new ArrayList<String>();

        for (int i = 0; i < list.length; i++) {
            if (isLookout) {
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

    /**
     *  \Description fonction qui permet de cree un nouveau repertoir dans le repertoir parents
     *              que le serveur de stockage se trouve actuellement
     *  \param String: le nom du nouveau repertoire qu'on veut cree
     *  \param DataOutputStream: le output stream vers lequelle le serveur envoyeras des donnees vers le client
     * \return void
     */
    public void mkdir(String name, DataOutputStream out) throws Exception {

        File newFile = new File(currentRep + "/" + name);
        if (newFile.mkdir()) {
            out.writeUTF("Le dossier " + name + " a ete cree");
        } else {
            out.writeUTF("Error: aucun dossier n'as ete cree. Essayer encore ");
        }
    }
    /**
     *  \Description fonction qui permet de supprimer un fichier ou un repertoire existant dans le repertoire parents
     *              que le serveur de stockage se trouve actuellement
     *  \param String: le nom du nouveau du fichier/repertoire qu'on veut supprimer
     *  \param DataOutputStream: le output stream vers lequelle le serveur envoyeras des donnees vers le client
     * \return void
     */

    public void delete(String name, DataOutputStream out) throws Exception {

        if (!ls(out, true).contains(name)) {
            out.writeUTF("Aucun dossier ou fichier de ce nom existe");
            return;
        }
        File newRep = new File(currentRep + "/" + name);
        newRep.delete();
        out.writeUTF( name + " a ete supprime.");
    }

    /**
     * \Description fonction qui permet de televerser un fichier du client vers le serveur de stockage
     * \param String: le nom du nouveau du fichier a televerser
     * \param DataInputStream: le input stream vers lequelle le client envoyera des donnees vers le serveur
     * \param DataOutputStream: le output stream vers lequelle le serveur envoyeras des donnees vers le client
     * \return void
     */

    public void upload(String nomFichier, DataInputStream in, DataOutputStream out) throws Exception {
        DataInputStream fin =  new DataInputStream(socket.getInputStream());

        System.out.println(currentRep + "/" + nomFichier);

        File newFile = new File(currentRep + "/" + nomFichier);
        FileOutputStream fileOut = new FileOutputStream(newFile);
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
        out.writeUTF("\n");
        out.writeUTF("Le fichier " + nomFichier + " a bien ete televerser.");
    }

    /**
     * \Description fonction qui permet de telecharger un fichier du serveur de stockage vers le client
     * \param String: le nom du nouveau du fichier a televerser
     * \param DataOutputStream: le output stream vers lequelle le serveur envoyeras des donnees vers le client
     * \return void
     */
    public void download(String nomFichier, DataOutputStream out) throws Exception {

        if (!ls(out, true).contains(nomFichier)) {
            out.writeUTF("Aucun dossier ou fichier de ce nom existe");
            return;
        }

        File newFile = new File(currentRep + "/" + nomFichier);
        InputStream uploadedFile = new FileInputStream(newFile);

        int packetSize;
        int fileLenght = (int) newFile.length();
        out.writeInt(fileLenght);

        byte [] buffer = new byte[fileLenght];
        while ((packetSize = uploadedFile.read(buffer)) != -1) {
            out.write(buffer, 0, packetSize);
        }
        out.flush();
        uploadedFile.close();
        out.writeUTF("\n");
        out.writeUTF("Le fichier " + nomFichier + " a bien ete televerser.");
    }

    /**
     * \Description fonction qui permet de cree un fichier zip a partir d'une fichier donnee
     * \param String: le nom du nouveau du fichier a zipper
     * \param DataOutputStream: le output stream vers lequelle le serveur envoyeras des donnees vers le client
     * \return void
     */
    public void Zip(String nomFichier, DataOutputStream out) throws Exception {

        if (!ls(out, true).contains(nomFichier)) {
            out.writeUTF("Aucun dossier ou fichier de ce nom existe");
            return;
        }

        File newFile = new File(currentRep + "/" + nomFichier);
        String zipFileName = nomFichier.substring(0, nomFichier.indexOf(".")) + ".zip";


        FileOutputStream zipFile = new FileOutputStream(currentRep + "/" + zipFileName);
        ZipOutputStream zipFileOutput = new ZipOutputStream(zipFile);
        zipFileOutput.putNextEntry(new ZipEntry(newFile.getName()));

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
    }

}