
package socketactivity;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 *
 * @author WPOSS
 */
public class SocketActivity {
      //C:\Users\WPOSS\Downloads\new9220-normal-dev-03.02.42-fw.img
    public static final int PORT = 5000; 
    public static File imgFile;
    public static Scanner scanner = new Scanner(System.in);
    public static String route;
    
    public static void main(String[] args) throws IOException {     
        
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor escuchando en el puerto: " + PORT);
            
            System.out.println("INGRESA LA RUTA DEL ARCHIVO A ENVIAR");
            route = scanner.next(); 
            imgFile = new File(route.replace(" ", ""));
           
            while (true) {
               
                Socket clientSocket = serverSocket.accept();
                if(clientSocket != null){
                  
                   System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
               new Thread(new ClientHandler(clientSocket, imgFile)).start();
                }else {
                    System.out.println("SOCKET CERRADO ++SOCKETACTIVITY");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR AL ESCUCHAR EN EL PUERTO " + PORT);
            e.printStackTrace();
        }
    }
}
