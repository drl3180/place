package place.model;

import place.PlaceBoard;
import place.PlaceException;
import place.PlaceTile;
import place.client.NetworkClient;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

/**
 * The client side model that is used as the "M" in the MVC paradigm.  All client
 * side applications (PTUI, GUI, bots) are observers of this model.
 *
 * @author Sean Strout @ RIT CS
 */
public class ClientModel{
    /** the actual board that holds the tiles */
    private PlaceBoard board;

    /**
     * Connection to network interface to server
     */
    private NetworkClient serverConn;


    /** observers of the model (PlacePTUI and PlaceGUI - the "views") */
    private List<Observer<ClientModel, PlaceTile>> observers = new LinkedList<>();

    public ClientModel(List<String> args) throws PlaceException, IOException {
        // Get host info from command line
        String host = args.get( 0 );
        int port = Integer.parseInt( args.get( 1 ) );
        String username = args.get(2);

        // Create the network connection.
        this.serverConn = new NetworkClient(host, port, username, this);
    }

    public void close(){
        serverConn.close();
    }

    public PlaceBoard getBoard(){
        return board;
    }

    public NetworkClient getServerConn(){
        return serverConn;
    }

    /**
     * Add a new observer.
     *
     * @param observer the new observer
     */
    public void addObserver(Observer<ClientModel, PlaceTile> observer) {
        this.observers.add(observer);
        serverConn.startListener();
    }

    /**
     * Notify observers the model has changed.
     */
    private void notifyObservers(PlaceTile tile){
        for (Observer<ClientModel, PlaceTile> observer: observers) {
            try {
                observer.update(this, tile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setBoard(PlaceBoard board) {
        this.board=board;
    }

    public void setTile(PlaceTile t) {
        board.setTile(t);
        notifyObservers(t);
    }
}
