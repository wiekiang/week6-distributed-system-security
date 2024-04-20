
/**
 *
 * @author Wie Kiang
 */

import java.net.*;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class TCPServer {
    private final KeyPairGenerator keyPairGen;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public TCPServer() throws NoSuchAlgorithmException {
        keyPairGen = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGen.genKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public KeyPairGenerator getKeyPairGen() {
        return keyPairGen;
    }

    public String decrypt(byte[] encodedMessage) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey, cipher.getParameters());
        return new String(cipher.doFinal(encodedMessage));
    }

    public static void main(String args[]) {
        int serverPort = 7896;
        AtomicInteger clientCount = new AtomicInteger(0);

        try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
            int i = 0;
            TCPServer tcpServer = new TCPServer();
            PublicKey publicKey = tcpServer.getPublicKey();

            while (true) {
                Socket clientSocket = listenSocket.accept();
                int count = clientCount.incrementAndGet();

                Connection c = new Connection(clientSocket, i++, count, publicKey, tcpServer.getPrivateKey());
                System.out.println("Thread " + i + " is created " + c.getName());
            }
        } catch (IOException e) {
            System.out.println("Listen :" + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }
}

class Connection extends Thread {
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Socket socket;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public Connection(Socket aClientSocket, int tn, int client, PublicKey key, PrivateKey privateKey)
            throws IOException {
        this.socket = aClientSocket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.publicKey = key;
        this.privateKey = privateKey;
        this.start();
    }

    public String decrypt(byte[] encodedMessage) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey, cipher.getParameters());
        return new String(cipher.doFinal(encodedMessage));
    }

    public void run() {
        try {
            String data;
            // generate the encoded key
            byte[] bytesPubKey = publicKey.getEncoded();
            System.out.println("PublicKey size in bytes: " + bytesPubKey.length);

            while ((data = in.readUTF()) != null) {

                System.out.println("Message from client: " + data);
                if (data.startsWith("Hello"))
                    out.writeUTF("Hello");
                if (data.equalsIgnoreCase("Key")) {
                    // send the keysize;
                    out.writeInt(bytesPubKey.length);
                    // send the key in bytes
                    out.write(bytesPubKey, 0, bytesPubKey.length);
                }
                if (data.equalsIgnoreCase("message"))
                    break;
            }
            // read the size of encrypted message to be sent from client
            int messageLength = in.readInt();
            byte[] encodedmessage = new byte[messageLength];
            // read the encryped password sent from client
            in.read(encodedmessage, 0, encodedmessage.length);
            // decrypt password.
            System.out.println("Decrypted Password: " + decrypt(encodedmessage));
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algorithm: " + e.getMessage());
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("invalid key spec: " + e.getMessage());
        } catch (InvalidKeyException e) {
            System.out.println("invalid key: " + e.getMessage());
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
