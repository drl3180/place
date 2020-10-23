package place.client;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.model.ClientModel;
import place.network.PlaceRequest;
import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;

/**
 * The client side network interface to a Place Server.
 *
 * @author Cameron Myron
 */
public class NetworkClient {

    /**
     * Turn on if standard output debug messages are desired.
     */
    private static final boolean DEBUG = false;

    /**
     * Print method that does something only if DEBUG is true
     *
     * @param logMsg the message to log
     */
    private static void dPrint( Object logMsg ) {
        if ( NetworkClient.DEBUG ) {
            System.out.println( logMsg );
        }
    }

    /**
     * The Socket used to communicate with the Place server.
     */
    private Socket sock;

    /**
     * The inputstream used to communicate with the Place server.
     */
    private ObjectInputStream networkIn;

    /**
     * The outputstream used to communicate with the Place server.
     */
    private ObjectOutputStream networkOut;

    /**
     * The ClientModel
     */
    private ClientModel model;

    /**
     * The name of the user
     */
    private String username;

    /**
     * The default color of a tile
     */
    private PlaceColor selectedColor = PlaceColor.WHITE;

    /**
     * Sentinel used to control the main game loop.
     */
    private boolean go;

    /**
     * Accessor that takes multithreaded access into account
     *
     * @return whether it ok to continue or not
     */
    private synchronized boolean goodToGo() {
        return this.go;
    }

    /**
     * Multithread-safe mutator
     */
    private synchronized void stop() {
        this.go = false;
    }

    /**
     * The Constructor for the netWork Client
     * @param hostname the name of the host running the server program
     * @param port the port of the server socket on which the server is listening
     * @param model the local object holding the state of the game that must be updated upon receiving server messages
     * @throws PlaceException If there is a problem opening the connection
     */
    public NetworkClient(String hostname, int port, String username, ClientModel model)
            throws PlaceException, IOException {
        try {
            this.username = username;
            this.model=model;
            sock = new Socket(hostname, port);
            networkIn  = new ObjectInputStream(sock.getInputStream());
            networkOut = new ObjectOutputStream(sock.getOutputStream());
            this.go = true;

            networkOut.writeObject(new PlaceRequest(PlaceRequest.RequestType.LOGIN,this.username));
            PlaceRequest ln;
            if((ln=(PlaceRequest)networkIn.readObject()).getType().equals(PlaceRequest.RequestType.LOGIN_SUCCESS))
                System.out.println("Login Successful As: "+ln.getData());
            else
                if(ln.getType().equals(PlaceRequest.RequestType.ERROR))
                    throw new PlaceException(ln.getData().toString());
                else
                    throw new PlaceException("Error: Unable To Login");

            if((ln=(PlaceRequest) networkIn.readObject()).getType().equals(PlaceRequest.RequestType.BOARD))
                model.setBoard((PlaceBoard)ln.getData());
            else
                throw new PlaceException("Error: Unable To Get Board");

            NetworkClient.dPrint( "Connected to server " + this.sock );
        }
        catch(IOException | ClassNotFoundException e ) {
            throw new PlaceException( e );
        }
    }

    /**
     * Starts the thread
     */
    public void startListener() {
        Thread netThread = new Thread(() -> this.run());
        netThread.start();
    }

    /**
     * Checks to see if a tile is changed
     * @param t the tile
     */
    public void tileChanged(PlaceTile t) {
        NetworkClient.dPrint( "! TILE_CHANGED, " + t.toString());

        // Update the board model.
        model.setTile(t);
    }

    /**
     * This method should be called at the end of the game to
     * close the client connection.
     */
    public void close() {
        try {
            this.sock.close();
        }
        catch( IOException ioe ) {
            // squash
        }
    }

    /**
     * UI wants to send a new move to the server.
     *
     * @param row the row
     * @param col the column
     */
    public void changeTile( int row, int col ) throws IOException {
        this.networkOut.writeObject(new PlaceRequest<>(PlaceRequest.RequestType.CHANGE_TILE,new PlaceTile(row,col,username,selectedColor)));
    }

    public void setSelectedColor(PlaceColor c){
        selectedColor = c;
    }

    public PlaceColor getSelectedColor(){
        return selectedColor;
    }

    public String getUser(){
        return username;
    }

    /**
     * Run the main client loop. Intended to be started as a separate
     * thread internally. This method is made private so that no one
     * outside will call it or try to start a thread on it.
     */
    private void run() {
        PlaceRequest ln;
        while (this.goodToGo()) {
            try {
                ln=(PlaceRequest)networkIn.readObject();
                NetworkClient.dPrint( "Net message in = \"" + ln.getType() + '"' );

                switch (ln.getType()) {
                    case LOGIN_SUCCESS:
                        assert false : "LOGIN_SUCCESS already happened?";
                        break;
                    case BOARD:
                        assert false : "BOARD already happened?";
                        model.setBoard((PlaceBoard)ln.getData());
                        break;
                    case TILE_CHANGED:
                        tileChanged((PlaceTile)ln.getData());
                        break;
                    case ERROR:
                        System.out.println(ln.getData());
                        stop();
                        break;
                    default:
                        System.err.println( "Unrecognized request: " + ln.getType());
                        this.stop();
                        break;
                }
            }
            catch( NoSuchElementException nse ) {
                // Looks like the connection shut down.
                System.err.println("Lost connection to server.");
                this.stop();
            }
            catch( Exception e ) {
                System.err.println(e.getMessage() + '?');
                this.stop();
            }
        }
        this.close();
    }
}
