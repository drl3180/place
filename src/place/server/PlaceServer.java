package place.server;

import place.PlaceBoard;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;

/**
 * The Place server is run on the command line as:
 *
 * $ java PlaceServer port DIM
 *
 * Where port is the port number of the host and DIM is the square dimension
 * of the board.
 *
 * @author Sean Strout @ RIT CS
 * @author Cameron Myron
 * @author Daniel Lynch
 */
public class PlaceServer {

    /**
     * The PlaceBoard model
     */
    private PlaceBoard model;

    /**
     * A Hashtable of clients and their names
     */
    private Hashtable<String,Client> clients;
    /**
     *
     */
    private final int PORT;

    /**
     * The port number
     */
    private final int MAX_CLIENTS;

    /**
     * The number of the maximum clients
     */

    AccessList accessList = new AccessList("src/place/server/whitelist.txt", AccessList.Type.WHITELIST);

    /**
     * An access list. Blacklist means anything on the list is not allowed to join, while whitelist means anything
     * not on the list is not allowed to join. If you are grading this by connecting from another machine either add
     * the IP to the whitelist or set the file to blacklist.txt and the type to BLACKLIST.
     */

    /**
     * Constructor for Place Server
     * @param DIM the dimension of the board
     * @param port the port number
     * @param maxClients the number of maximum clients
     */
    public PlaceServer(int DIM, int port, int maxClients){
        model = new PlaceBoard(DIM);
        clients = new Hashtable<String,Client>();
        PORT = port;
        MAX_CLIENTS = maxClients;
    }

    /**
     * The function that creates a new client
     * @param s the socket
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public synchronized void login(Socket s) throws IOException,ClassNotFoundException{
        ObjectOutputStream o = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream i = new ObjectInputStream(s.getInputStream());
        if(!accessList.allowed(s.getLocalAddress().toString().substring(1))){
            o.writeObject(new PlaceRequest(PlaceRequest.RequestType.ERROR, "You Are Not Allowed On This Server!"));
            o.close();
            i.close();
            s.close();
        }
        else if (clients.size() < MAX_CLIENTS) {
            Client temp = new Client(this, o, i);
            if (!(temp.getUSERNAME() == null || clients.containsKey(temp.getUSERNAME()))) {
                clients.put(temp.getUSERNAME(), temp);
                System.out.println(s.getLocalAddress() + " Joined As " + temp.getUSERNAME()+": "+clients.size()+" Online");
                temp.start();
            } else {
                o.writeObject(new PlaceRequest(PlaceRequest.RequestType.ERROR, "Username Already In Use!"));
                o.close();
                i.close();
                s.close();
            }
        } else {
            o.writeObject(new PlaceRequest(PlaceRequest.RequestType.ERROR, "Server Is Full!"));
            o.close();
            i.close();
            s.close();
        }
    }

    /**
     * The function that runs the server
     */
    public void run(){
        try(
                ServerSocket server = new ServerSocket(PORT)
        ){
            while(true){
                try{
                    Socket s = server.accept();
                    login(s);
                }catch(IOException|ClassNotFoundException e){
                    System.out.println("ERROR");
                }
            }
        }catch(IOException e){
            System.out.println(e);
        }
    }

    /**
     * The changeTile function than changes a Tile on the board
     * @param t a PlaceTile tile
     */
    public synchronized void changeTile(PlaceTile t){
        if(t==null||!model.isValid(t))
            return;
        t.setTime(new Date().getTime());
        model.setTile(t);
        for(Client i:clients.values())
            i.tileChanged(t);
    }

    /**
     * The logout function that remove a client and prints out a logout message
     * @param c the client that logs out
     */
    public synchronized void logout(Client c){
        clients.remove(c.getUSERNAME());
        System.out.println(c.getUSERNAME()+" Logged Out: "+clients.size()+" Online");
    }

    /**
     * A getter method for the PlaceBoard
     * @return the PlaceBoard
     */
    public PlaceBoard getModel(){
        return model;
    }

    /**
     * The main method starts the server and spawns client threads each time a new
     * client connects.
     *
     * @param args the command line arguments
     *
     * Note: If the max number of clients isn't specified (third argument) 100 is the default
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            PlaceServer srvr = new PlaceServer(Integer.parseInt(args[1]), Integer.parseInt(args[0]), 100);
            srvr.run();
        }
        else if(args.length == 3){
            PlaceServer srvr = new PlaceServer(Integer.parseInt(args[1]), Integer.parseInt(args[0]), Integer.parseInt(args[2]));
            srvr.run();
        }
        else {
            System.out.println("Usage: java PlaceServer port DIM <maxClients>");
        }
    }
}