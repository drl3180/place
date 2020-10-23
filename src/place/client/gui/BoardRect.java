package place.client.gui;

import javafx.event.Event;
import place.PlaceColor;
import java.io.IOException;

/**
 * Represents a board tile.
 * @author Daniel Lynch and Cameron Myron
 */
public class BoardRect extends ColorRect{
    /**
     * The client
     */
    private PlaceGUI client;

    /**
     * The row
     */
    private int row;

    /**
     * The column
     */
    private int col;

    /**
     * Creates the board tile as a rectangle.
     * @param s the size
     * @param c the color
     * @param client the client
     * @param row the row placement
     * @param col the column placement
     */
    public BoardRect(double s, PlaceColor c, PlaceGUI client, int row, int col) {
        super(s, c);
        this.client=client;
        this.row=row;
        this.col=col;
    }

    /**
     * The eventhandler that changes the tile.
     * @param e the event(a mouse click)
     */
    @Override
    public void event(Event e){
        try {
            client.getModel().getServerConn().changeTile(row, col);
            Thread.sleep(500);
        }catch(IOException | InterruptedException ioe){

        }
    }
}
