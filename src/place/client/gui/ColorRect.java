package place.client.gui;

import javafx.event.Event;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import place.PlaceColor;

/**
 * Represents a colored rectangle
 * @author Daniel Lynch and Cameron Myron
 */
public class ColorRect extends Rectangle{

    /**
     * The size of the colored rectangle
     */
    protected double s;

    /**
     * The color of the colored rectangle
     */
    protected PlaceColor c;

    /**
     * The constructor that colors the rectangle
     * @param s the size
     * @param c the color
     */
    public ColorRect(double s, PlaceColor c){
        super();
        this.s=s;
        this.c=c;
        setWidth(s);
        setHeight(s);
        setFill(Color.rgb(c.getRed(),c.getGreen(),c.getBlue()));
        setOnMouseClicked(this::event);
    }

    /**
     * The event handler for the ColorRectangle
     * @param e the event
     */
    public void event(Event e){
    }
}
