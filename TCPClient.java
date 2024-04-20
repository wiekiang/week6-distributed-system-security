
/**
 *
 * @author Wie Kiang
 */

import java.net.*;
import java.io.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public class TCPClient {

    private static PublicKey publicKey;

    final int serverPort;
    final String host;
    private final Socket clientSocket;

    public TCPClient(int serverPort, String host) {

        this.serverPort = serverPort;
        this.host = host;

        try {
            clientSocket = new Socket(host, serverPort);
            System.out.println("Client started");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create client socket", e);
        }
    }

    public Socket getSocket() {
        return this.clientSocket;
    }

    public byte[] encrypt(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherData = cipher.doFinal(message.getBytes("UTF-8"));
        return cipherData;
    }

    public static void main(String args[]) {

        byte[] bytesPublicKey = null;
        Socket s = null;
        String data;
        int pubKeyLength;
        try {
            TCPClient tcpClient = new TCPClient(7896, "localhost");

            s = tcpClient.getSocket();

            DataInputStream in = new DataInputStream(
                    s.getInputStream());
            DataOutputStream out = new DataOutputStream(
                    s.getOutputStream());

            // str.append(encodedPublicKey);
            out.writeUTF("Hello " + tcpClient.serverPort);

            // while ((data =in.readUTF()) != null) {
            data = in.readUTF();
            System.out.println("message from Server: " + data);

            if (data.equalsIgnoreCase("Hello")) {
                out.writeUTF("Key");
                // read the size PublicKey
                pubKeyLength = in.readInt();
                bytesPublicKey = new byte[pubKeyLength];
                // read the PublicKey in bytes sent from the sever
                in.readFully(bytesPublicKey, 0, pubKeyLength);
            }

            // generate the key speciifcation for encoding
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(bytesPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // extract the PublicKey
            publicKey = keyFactory.generatePublic(pubKeySpec);

            // ecrypt the password
            byte[] encodedmessage = tcpClient.encrypt("secret");
            out.writeUTF("message");

            // send the encrypted password length
            out.writeInt(encodedmessage.length);

            // send the encrypted password in bytes
            out.write(encodedmessage, 0, encodedmessage.length);

        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algorithm: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        } finally {
            if (s != null)
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
        }
    }
}
