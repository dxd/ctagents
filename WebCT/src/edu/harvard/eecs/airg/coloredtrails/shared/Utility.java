/*
	Colored Trails
	
	Copyright (C) 2006, President and Fellows of Harvard College.  All Rights Reserved.
	
	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package edu.harvard.eecs.airg.coloredtrails.shared;

import edu.harvard.eecs.airg.coloredtrails.shared.exceptions.ExceptionWriter;

import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A set of utility functions accessible statically throughout CT.
 *
 * @author Paul Heymann (ct3@heymann.be)
 */
public class Utility {
    public static final int CT3_SERVER_DEFAULT_PORT = 8088;
    public static final int CT3_CLIENT_DEFAULT_PORT = 8888;
    public static ArrayList<String> debugchars = new ArrayList<String>();

    /**
     * Turn an object into a byte array.
     *
     * @param o The original object.
     * @return A byte array.
     */
    public static byte[] objectToBytes(Object o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();

        } catch (UnsupportedEncodingException e) {
            System.out.println(
                    "Error in serialization process.  Quitting.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.out.println(
                    "Error in serialization process.  Quitting.");
            e.printStackTrace();
            System.exit(1);
        }

        return baos.toByteArray();
    }

    /**
     * Turn an array of bytes into an object.
     *
     * @param b The array of bytes.
     * @return The represented object.
     */
    public static Object bytesToObject(byte[] b) {
        ByteArrayInputStream bais = null;
        Object o = null;
        try {
            bais = new ByteArrayInputStream(b);
            ObjectInputStream ois = new ObjectInputStream(bais);
            o = (Object) ois.readObject();
            ois.close();
        } catch (UnsupportedEncodingException e) {
            System.out.println(
                    "Error in serialization process.  Quitting.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.out.println(
                    "Error in serialization process.  Quitting.");
            e.printStackTrace();
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.out.println(
                    "Error in serialization process.  Quitting.");
            e.printStackTrace();
            System.exit(1);
        }
        return o;
    }

    /**
     * Get the contents of a file as a byte array.
     *
     * @param file The file to be read.
     * @return The byte array represented by the file.
     * @throws IOException
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
	
        // Get the size of the file
        long length = file.length();
	
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
	
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
	
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                &&
                (numRead = is.read(bytes, offset, bytes.length - offset)) >=
                0) {
            offset += numRead;
        }
	
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException(
                    "Could not completely read file " + file.getName());
        }
	
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * Create a gui dialog representing an error which occurred.
     *
     * @param errormsg The error message to be shown with this dialog.
     */
    public static void guiError(String errormsg) {
        guiError(errormsg, "Error");
    }

    /**
     * Create a gui dialog representing an error which occurred.
     *
     * @param errormsg   The error message to be shown with this dialog.
     * @param errortitle The title of the error message to be shown in the
     *                   window.
     */
    public static void guiError(String errormsg, String errortitle) {

        JOptionPane.showMessageDialog(null,
                errormsg,
                errortitle,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Sleep for a given number of milliseconds.
     *
     * @param millis The number of milliseconds to sleep.
     */
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    /**
     * An XML-RPC wrapper method which calls the necessary code in
     * Apache XML-RPC.
     *
     * @param xmlrpchost The host to make a request on.
     * @param method     The remote method to call.
     * @param params     The remote method's parameters.
     * @param ew         The exceptionwriter to use if there is an error.
     * @return The result, as an object array, where the first object
     *         is a boolean representing success or failure, and the second
     *         object is the result of the remote method.
     *
    public static Object[] makeXmlRpcRequest(String xmlrpchost,
                                             String method,
                                             Vector params,
                                             ExceptionWriter ew) {
        Object returnval[] = new Object[2];
        returnval[0] = new Boolean(true);
        returnval[1] = null;
        try {
            XmlRpcClient xmlrpc = new XmlRpcClient(xmlrpchost);
            returnval[1] = xmlrpc.execute(method, params);
        } catch (MalformedURLException e) {
            ew.write("Broken XMLRPC URL", e);
            returnval[0] = new Boolean(false);
        } catch (XmlRpcException e) {
            ew.write("XMLRPC Failed xml", e);
            System.out.println("makeXmlRpcRequest fail! xmlrpchost: " + xmlrpchost + "method: " +method + "params: "+ params);
            returnval[0] = new Boolean(false);

        } catch (IOException e) {
            ew.write("XMLRPC Failed", e);
            returnval[0] = new Boolean(false);
        }
        return returnval;
    }
    */

    /**
     * Create an int from an Integer in a hash.
     *
     * @param key The key to look up in the hash.
     * @param h   The hash.
     * @return The int value of the Integer in the hash.
     */
    public static int intFromHash(Object key, Hashtable h) {
        return ((Integer) h.get(key)).intValue();
    }

    /**
     * Create a boolean from an Boolean in a hash.
     *
     * @param key The key to look up in the hash.
     * @param h   The hash.
     * @return The boolean value of the Boolean in the hash.
     */
    public static boolean booleanFromHash(Object key, Hashtable h) {
        return ((Boolean) h.get(key)).booleanValue();
    }

    public static void addDebugChar(String character) {
        debugchars.add(character);
    }

    public static void debugPrint(String character, String message) {
        if (debugchars.contains(character)) {
            System.err.println(message);
        }
    }

}
