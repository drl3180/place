package place.server;

import java.io.*;
import java.util.HashSet;

/**
 * The AccessList of the PlaceServer
 * Represents Blacklists and Whitelists
 *
 * @author Cameron Myron
 */

public class AccessList {

    private HashSet<String> list = new HashSet<String>();
    private String file;
    private Type type;

    public enum Type{
        BLACKLIST,
        WHITELIST
    }

    public AccessList(String file, Type type){
        this.type=type;
        this.file=file;
        try(
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)));
        ){
            String ln;
            while((ln=in.readLine())!=null)
                list.add(ln);
        }catch(IOException e){
            System.err.println(e);
        }
    }

    /**
     * Used to save the current list of IPs in a txt file
     *
     * @return Successfully writes the updated list in the current directory
     */
    public boolean printList(){
        try(
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.file)),true);
        ){
            for(String i:list)
                out.println(i);
        }catch(IOException e){
            return false;
        }
        return true;
    }

    /**
     *
     * @param ip String representation of the IP you are checking the permissions of
     * @return If the given IP is allowed on the server
     */
    public boolean allowed(String ip){
        if(type==Type.BLACKLIST)
            return !onList(ip);
        return onList(ip);
    }

    /**
     *
     * @return String representation of the current file directory (where it will print updated lists)
     */
    public String getFile(){
        return file;
    }

    /**
     *
     * @param file String representation of the new file directory (can be used to print the new list in another txt file)
     */
    public void setFile(String file){
        this.file=file;
    }

    /**
     *
     * @param ip String that represents the IP you want to check for on the list
     * @return If the IP is on the list
     */
    public boolean onList(String ip){
        return list.contains(ip);
    }

    /**
     *
     * @param ip String that represents the IP you want to add to the list
     * @return If the IP was added (meaning it wasn't there and now it is)
     */
    public boolean add(String ip){
        return list.add(ip);
    }

    /**
     *
     * @param ip String that represents the IP you want to remove from the list
     * @return If the IP was removed (meaning it was there and now it isn't)
     */
    public boolean remove(String ip){
        return list.remove(ip);
    }

    /**
     *
     * @return Number of IPs currently on the list
     */
    public int size(){
        return list.size();
    }

    /**
     *
     * @return String representation of IPs currently on the list
     */
    public String toString(){
        String rtrn="";
        for(String i:list)
            rtrn+=i+"\n";
        return rtrn;
    }

    /**
     * Tester method
     *
     * @param args
     */
    public static void main(String[] args){
        if(args.length!=2){
            System.out.println("Usage: java AccessList file listType");
        }
        Type listType;
        if(args[1].equalsIgnoreCase("whitelist"))
            listType = Type.WHITELIST;
        else
            listType = Type.BLACKLIST;
        AccessList test = new AccessList(args[0], listType);
        test.add("127.0.0.1");
        test.printList();
    }
}
