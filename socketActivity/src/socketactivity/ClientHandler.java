package socketactivity;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private File route_to_send;

    public ClientHandler(Socket socket, File route_to_send) {
        this.clientSocket = socket;
        this.route_to_send = route_to_send;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());

            out.println(route_to_send.getName());
            out.println(String.valueOf(route_to_send.length()));

            String inputData;

            while ((inputData = in.readLine()) != null) {
                System.out.println("MENSAJE DEL CLIENTE ANDROID: " + inputData);

                if (inputData.equals("DOWNLOAD")) {
                    System.out.println(route_to_send.getName());

                    // Calcular y enviar checksum antes de enviar el archivo
                    String checksum = calculateChecksum(route_to_send, "SHA-256");
                    
                    if (sendFile(clientSocket, route_to_send, dataOut)) {
                        out.println(checksum);  // Enviar checksum al cliente
                        System.out.println("TRANSFERENCIA COMPLETA - Checksum: " + checksum);
                        out.close();
                        break;
                    } else {
                        out.println("ERROR AL ENVIAR ARCHIVO");
                    }
                }

                if (inputData.equalsIgnoreCase("exit")) {
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, "Error en la comunicación con el cliente", ex);
        }
    }

    private boolean sendFile(Socket clientSocket, File route_to_send, DataOutputStream dataOut) {
        byte[] buffer = new byte[4096];
        try (FileInputStream fis = new FileInputStream(route_to_send)){

            long fileSize = route_to_send.length();
            int bytesRead;
            long totalBytesRead = 0;

            while (totalBytesRead < fileSize && (bytesRead = fis.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            dataOut.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Error enviando archivo: " + e.getMessage());
        }
        return false;
    }

    // Método para calcular el checksum del archivo
    private String calculateChecksum(File file, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[4096];
            int bytesCount;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }

            fis.close();
            byte[] bytes = digest.digest();

            // Convertir el byte array a formato hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}