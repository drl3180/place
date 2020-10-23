package place.client.bots;

import javafx.stage.Stage;
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

public class bot1 extends ConsoleApplication implements Observer<ClientModel, PlaceTile> {

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
     * command line parameters.
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
     * It is not like {@link javafx.application.Application#start(Stage)}.
     * That method returns as soon as the setup is done.
     * called indirectly from a model update from {@link NetworkClient}.
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
            PlaceColor c;
            do {
                try {
                    row=0;
                    col = 0;
                    for(PlaceColor i:PlaceColor.values())
                        if(i.getNumber()==0) {
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
            ConsoleApplication.launch(bot1.class, args);
    }
}
