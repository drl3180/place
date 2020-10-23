package place.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.model.ClientModel;
import place.model.Observer;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * The GUI of the PLACE game/application
 * @author Daniel Lynch and Cameron Myron
 *
 */
public class PlaceGUI extends Application implements Observer<ClientModel, PlaceTile> {

    /**
     * The BorderPane of the GUI
     */
    private BorderPane boarderPane = new BorderPane();

    /**
     * The Top GridPane of the GUI
     */
    private GridPane gridPane = new GridPane();

    /**
     * The Top GridPane of the GUI
     */
    private GridPane PlaceColorSelect = new GridPane();

    /**
     * The ClientModel
     */
    private ClientModel model;

    /**
     * Initialization of the GUI.
     */

    private Tooltip tooltip;

    public void init(){
        // Get host info from command line
        List<String> args = getParameters().getRaw();

        try {
            model = new ClientModel(args);
        }catch(PlaceException e){

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The start of the GUI.
     * @param primaryStage the primary stage of the GUI
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setOnCloseRequest((WindowEvent e)->{
            model.close();
        });
        primaryStage.setResizable(false);
        primaryStage.setWidth(425);
        primaryStage.setHeight(475);
        List< String > args = getParameters().getRaw();
        primaryStage.setTitle("Place: " + args.get(2));
        Scene scene = new Scene(boarderPane);
        primaryStage.setScene(scene);
        primaryStage.show();
        model.addObserver(this);
        updateBoard();
    }

    /**
     * The default update method for the GUI.
     * @param model the client model
     * @param tile the tile that is being updated
     */
    @Override
    public void update(ClientModel model, PlaceTile tile) {
        Platform.runLater(()->{
            System.out.println(model.getBoard());
            updateBoard();
        });
    }

    /**
     * The modified update that actually updates the board.
     */
    public void updateBoard(){
        for(PlaceColor i:PlaceColor.values()){
            PlaceColorSelect.add(new SelectorRect(25, i, this),i.getNumber(),0);
        }
        boarderPane.setBottom(PlaceColorSelect);
        PlaceTile[][] b = model.getBoard().getBoard();
        for(int i=0;i<b.length;i++)
            for(int j=0;j<b[i].length;j++) {
                BoardRect temp;
                if (b[i][j] == null)
                    temp = new BoardRect(405.0 / b.length, PlaceColor.WHITE, this, i, j);
                else
                    temp = new BoardRect(405.0 / b.length, b[i][j].getColor(), this, i, j);
                Tooltip t = new Tooltip("(" + j + "," + i + ")\n" + b[i][j].getOwner() + "\n" + new Date(b[i][j].getTime()).toString());
                t.setGraphic(new ColorRect(50, b[i][j].getColor()));
                Tooltip.install(temp, t);
                gridPane.add(temp, j, i);
            }
        boarderPane.setCenter(gridPane);
    }

    /**
     * Creates a rectangle that responds to gestures on a touch screen or
     * trackpad and logs the events that are handled.
     *
     * @return Rectangle to show
     *
     */

    /**
     * The main method for the GUI
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceGUI host port username");
            System.exit(-1);
        } else {
            Application.launch(args);
        }
    }

    /**
     * The ClientModel getter.
     * @return the client model
     */
    public ClientModel getModel() {
        return model;
    }
}