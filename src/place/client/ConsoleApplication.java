package place.client;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * The Console Application
 * @author Cameron Myron
 */
public abstract class ConsoleApplication {

    /**
     * The command line arguments
     */
    private String[] cmdLineArgs;

    /**
     * The event thread
     */
    private Thread eventThread;

    /**
     * Run a console application.
     * @param ptuiClass the class object that refers to the class to
     *             be instantiated
     */
    public static void launch(
            Class< ? extends ConsoleApplication > ptuiClass
    ) {
        launch( ptuiClass, new String[ 0 ] );
    }

    /**
     * Run a console application, with command line arguments.
     * @param ptuiClass the class object that refers to the class to be instantiated
     * @param args the array of strings from the command line
     */
    public static void launch(
            Class< ? extends ConsoleApplication > ptuiClass,
            String[] args
    ) {
        try {
            ConsoleApplication ptuiApp = ptuiClass.newInstance();
            ptuiApp.cmdLineArgs = Arrays.copyOf( args, args.length );

            try {
                ptuiApp.init();
                ptuiApp.eventThread = new Thread( new Runner( ptuiApp ) );
                ptuiApp.eventThread.start();
                ptuiApp.eventThread.join();
            }
            catch( InterruptedException ie ) {
                System.err.println( "Console event thread interrupted" );
            }
            finally {
                ptuiApp.stop();
            }
        }
        catch( InstantiationException ie ) {
            System.err.println( "Can't instantiate Console App:" );
            System.err.println( ie.getMessage() );
        }
        catch( IllegalAccessException iae ) {
            System.err.println( iae.getMessage() );
        }
    }

    /**
     *
     */
    private static class Runner implements Runnable {
        private final ConsoleApplication ptuiApp;
        public Runner( ConsoleApplication ptuiApp ) { this.ptuiApp = ptuiApp; }

        public void run() {
            PrintWriter out = null;
            try {
                try ( BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))) {
                    do {
                        try {
                            out = new PrintWriter(
                                    new OutputStreamWriter( System.out ), true );
                            ptuiApp.go( consoleIn, out );
                            out = null;
                        }
                        catch( Exception e ) {
                            e.printStackTrace();
                            if ( out != null ) {
                                out.println( "\nRESTARTING...\n" );
                            }
                        }
                    } while ( out != null );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fetch the application's command line arguments
     * @return the string array that was passed to launch, if any, or else an empty array
     */
    public List< String > getArguments() {
        return Arrays.asList( this.cmdLineArgs );
    }

    /**
     * A do-nothing setup method that can be overwritten by subclasses
     * when necessary
     */
    public void init() {}

    /**
     * The method that is expected to run the main loop of the console
     * application, prompting the user for text input and displaying
     * text output.
     * @param consoleIn  the source of the user input
     * @param consoleOut the destination where text output should be printed
     */
    public abstract void go(BufferedReader consoleIn, PrintWriter consoleOut );

    /**
     * A do-nothing teardown method that can be overwritten by subclasses
     * when necessary.
     */
    public void stop(){}

}
