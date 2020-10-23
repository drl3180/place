
package place.server;

import place.PlaceException;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The Client class that represents a client as a thread
 * @author Cameron Myron
 */
public class Client extends Thread{
    /**
     * The server
     */
    public final PlaceServer SERVER;

    /**
     * The username of the client
     */
    private final String USERNAME;

    /**
     * The inputstream
     */
    private final ObjectInputStream IN;

    /**
     * The outputstream
     */
    private final ObjectOutputStream OUT;

    /**
     * The Constructor that takes in a PlayerServer, OutputStream, and an InputSteam
     * @param s The PlaceServer
     * @param out The OutputStream
     * @param in The InputStream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Client(PlaceServer s, ObjectOutputStream out,ObjectInputStream in) throws IOException,ClassNotFoundException{
        SERVER=s;
        IN = in;
        OUT = out;
        PlaceRequest ln;
        if((ln=(PlaceRequest)IN.readObject()).getType().equals(PlaceRequest.RequestType.LOGIN)) {
            USERNAME = (String) ln.getData();
        }
        else{
            System.err.println("Invalid Protocol");
            USERNAME=null;
        }
    }

    /**
     * The getter for the username
     * @return the username
     */
    public String getUSERNAME(){
        return USERNAME;
    }

    /**
     * Prints out when a tile changed
     * @param t The PlaceTile
     */
    public void tileChanged(PlaceTile t){
        try {
            OUT.writeObject(new PlaceRequest(PlaceRequest.RequestType.TILE_CHANGED, t));
        }catch(IOException e){
        }
    }

    /**
     * Runs the thread. If the tile needs to change, it waits .5 seconds. Once it finishes, the server closes.
     *
     */
    @Override
    public void run() {
        try{
            OUT.writeObject(new PlaceRequest(PlaceRequest.RequestType.LOGIN_SUCCESS, USERNAME));
            OUT.writeObject(new PlaceRequest(PlaceRequest.RequestType.BOARD, SERVER.getModel()));
            while(true){
                PlaceRequest ln;
                if ((ln = (PlaceRequest) IN.readObject()).getType().equals(PlaceRequest.RequestType.CHANGE_TILE)) {
                    SERVER.changeTile((PlaceTile) ln.getData());
                    try {
                        sleep(500);
                    }catch(InterruptedException ie){
                    }
                }
                else {
                    OUT.writeObject(new PlaceRequest(PlaceRequest.RequestType.ERROR, "Invalid Command: Terminating Connection"));
                    throw new PlaceException("");
                }
            }
        }catch(IOException|ClassNotFoundException|PlaceException e){
            try {
                OUT.close();
                IN.close();
            } catch (IOException ex){

            }
            SERVER.logout(this);
        }
    }
}
