package place.client.gui;

import javafx.event.Event;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import place.PlaceColor;

/**
 * Represents a color selector rectangle
 * @author Daniel Lynch and Cameron Myron
 */
public class SelectorRect extends StackPane{

    /**
     * The Client
     */
    private PlaceGUI client;
    /**
     * The color
     */
    private PlaceColor c;

    /**
     * The constructor for the selected Rectangle
     * @param s the size
     * @param c the color
     * @param client the client
     */
    public SelectorRect(double s, PlaceColor c, PlaceGUI client) {
        Text txt = new Text(c.toString());
        if(c.getName().equals("white") || c.getName().equals("yellow") || c.getName().equals("silver") || c.getName().equals("grey") || c.getName().equals("lime") || c.getName().equals("aqua"))
            txt.setFill(Color.BLACK);
        else
            txt.setFill(Color.WHITE);
        txt.setStyle("-fx-font: "+(int)(s/1.7)+" arial;");
        getChildren().addAll(new ColorRect(s,c), txt);
        this.client=client;
        this.c=c;
        setOnMouseClicked(this::event);
    }

    /**
     * The event handler that tells the server what color the user is going to place
     * @param e the event
     */
    public void event(Event e) {
        System.out.println("Selected: "+c.toString());
        client.getModel().getServerConn().setSelectedColor(c);
    }
}
