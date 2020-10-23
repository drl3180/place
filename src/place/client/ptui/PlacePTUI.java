package place.client.ptui;

import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.client.ConsoleApplication;
import place.client.NetworkClient;
import place.model.ClientModel;
import place.model.Observer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class PlacePTUI extends ConsoleApplication implements Observer<ClientModel, PlaceTile> {

    /**
     * The ClientModel
     */
    private ClientModel model;

    /**
     * What to read to see what user types
     */
    private BufferedReader userIn;

    /**
     * Where to send text that the user can see
     */
    private PrintWriter userOut;

    @Override
    public void update(ClientModel model, PlaceTile tile){
        refresh();
    }
    /**
     * Create the board model, create the network connection based on
     * command line parameters, and use the first message received to
     * allocate the board size the server is also using.
     */
    @Override
    public void init() {
        try {
            model = new ClientModel(super.getArguments());
        }
        catch( PlaceException |
                ArrayIndexOutOfBoundsException |
                NumberFormatException e ) {
            System.out.println( e );
            throw new RuntimeException( e );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method continues running until the game is over.
     * This method waits for a notification, called indirectly from a model update from {@link NetworkClient}.
     *
     * @param userIn what to read to see what user types
     * @param userOut where to send messages so user can see them
     */
    @Override
    public synchronized void go( BufferedReader userIn, PrintWriter userOut ){

        this.userIn = userIn;
        this.userOut = userOut;

        // Connect UI to model. Can't do it sooner because streams not set up.
        this.model.addObserver( this );

        // Manually force a display of all board state, since it's too late
        // to trigger update().
        this.refresh();
        while (true) {
            boolean done = false;
            int row;
            int col;
            do {
                try {
                    String[] parameters = userIn.readLine().split(" ");
                    if ((row = Integer.parseInt(parameters[0])) == -1) {
                        model.close();
                        break;
                    }
                    col = Integer.parseInt(parameters[1]);
                    for(PlaceColor i:PlaceColor.values())
                        if(i.getNumber()==Integer.parseInt(parameters[2])) {
                            this.model.getServerConn().setSelectedColor(i);
                            break;
                        }

                    if (this.model.getBoard().isValid(new PlaceTile(row, col, model.getServerConn().getUser(), model.getServerConn().getSelectedColor()))) {
                        this.model.getServerConn().changeTile(row, col);
                        done = true;
                    }
                }catch(Exception e){

                }
            } while (!done);
            try {
                Thread.sleep(500);
            }catch(InterruptedException e){
            }
        }
    }

    /**
     * GUI is closing, so close the network connection. Server will
     * get the message.
     */
    @Override
    public void stop(){
        try {
            this.userIn.close();
            this.userOut.close();
            this.model.close();
        }catch(IOException e){
        }
    }

    /**
     * Update all GUI Nodes to match the state of the model.
     */
    private void refresh(){
        userOut.println(model.getBoard()+"\n");
    }

    public static void main(String[] args){
        if (args.length != 3)
            System.out.println("Usage: java PlaceClient host port username");
        else
            ConsoleApplication.launch(PlacePTUI.class, args);
    }
}
