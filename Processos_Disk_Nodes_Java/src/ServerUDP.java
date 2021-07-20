
import huffman.HuffmanEncoding;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
/***************************************************************************************
 *
 *  Utility: Execution
 *  @author GabrielaZH-JoseKatoche/21-IF5000-RAID5
 **************************************************************************************/

/**
 * Where a socket is created
 */
public class ServerUDP {
   final int PORT = 16787;
   public DatagramSocket udpSocket; // Socket open for UDP
   public ArrayList<InetAddress> clientIPs; //List of IP addresses
   public ArrayList<Integer> clientPorts; //List of Ports
   public HashSet<String> connectedClients;//List of clients

   InetAddress currentClientIP;
   int currentClientPort ;
   DatagramPacket udpPacket;
   String filename="";
   String folderName = "";
   byte[] byteBuffer;
   int numNodos;

   String pathFiles="C:\\21-IF5000-RAID5\\Processos_Disk_Nodes_Java\\filesReceive\\";
   public ServerUDP() {
      try {
         System.out.println(InetAddress.getLocalHost().getHostAddress());
      } catch (UnknownHostException uhe) {
         System.err.println(uhe.getMessage());
      }

      try {
         //UDP
         udpSocket = new DatagramSocket(PORT);
         clientIPs = new ArrayList();
         clientPorts = new ArrayList();
         connectedClients = new HashSet();

         UDPThread udpThread = new UDPThread();
         udpThread.start();
         System.out.println("Waiting for UDP Client");

      } catch (IOException ioe) {
         System.err.println(ioe.getMessage());
      }

   }

   public static void main(String[] args) {
      ServerUDP cs = new ServerUDP();
   }

   /**
    * Where a socket is created and receives and sends to the Controller_Node_Java
    * In case you receive a file with the word nodes, then it extracts the number of nodes to create
    * In case of receiving a .txt then it takes the name of the file
    * In case of receiving a receiveRequest, send files to Controller_Node_Java
    * In case of not receiving a receiveRequest, save files in disks
    */
  public class UDPThread extends Thread {



      public void run() {

         while (true) {
            try {

               // sets/resets buffer size
               byteBuffer = new byte[60000];

               //create new Packet
               udpPacket = new DatagramPacket(byteBuffer, byteBuffer.length);

               //Receive
               udpSocket.receive(udpPacket);
               //New Timestamp of when the message was received;
               String newTimeStamp = "[" + getCurrentTime() + "]";

               //Get the IP and Port of where the message came from
                currentClientIP = udpPacket.getAddress();
                currentClientPort = udpPacket.getPort();
                String response ="";
                String receivedMsg = "";
                //Send send= new Send();

               receivedMsg= readFile(decodeFile(pathFiles+"fileEncode.txt",pathFiles+"fileDecode.txt"));//read data in file


               String nodos= "";

               if (receivedMsg.contains("nodes:")) {
                  nodos = receivedMsg.replaceAll("nodes:","");
                  nodos = nodos.replaceAll("\uFFFF","");
                  numNodos =  Integer.parseInt(nodos);

               // verify if the message is the name file
               }else if(receivedMsg.contains(".txt")){
                  filename=receivedMsg.trim();
                  filename = filename.replaceAll(".txt","");
                  filename = filename.replaceAll("\uFFFF","");
                  if(!filename.contains("file")){
                     folderName=filename;
                  }

                  RAID5 raid5 = new RAID5();
                  //Create folder book
                  File file = new File(pathFiles,folderName);

                  if (!file.exists()) {
                     file.mkdirs();
                  }

                  //Create folder disks
                  List<String> disks = new ArrayList<>();
                  for (int i = 0; i < numNodos; i++) {
                     disks.add("DISK"+i);
                  }

                  raid5.createDisks(disks,pathFiles + folderName);

                  //verify if the message is the content file for save
               } else if(!receivedMsg.contains("receiveRequest")){
                  String numberDisk = filename.substring(filename.length()-1);
                  decodeFile(pathFiles + "fileEncode.txt", pathFiles +folderName+"\\DISK"+numberDisk+"\\"+ filename + ".txt");

                  response = "[" + newTimeStamp + "] IP:" + currentClientIP + " : File Receive from server" ;
                  sendMessageToClients( (response).getBytes());

                  //verify if the message is send the file
               }else if (receivedMsg.contains("receiveRequest")){
                  File diskToDelete = new File(pathFiles + filename);
                  String[] contents = diskToDelete.list();

                  for (int i = 0; i < numNodos; i++) {
                     File finalFile;
                     File finalFileB;

                     if(new File(pathFiles+filename+"\\DISK"+i+"\\file"+i+".txt").exists()){
                        finalFile= new File(encodeFile(pathFiles+"fileEncode.txt",pathFiles+filename+"\\DISK"+i+"\\file"+i+".txt"));
                        FileInputStream source = new FileInputStream(finalFile);
                        byte buf[]=new byte[60000];
                        int j=0;
                        while(source.available()!=0)
                        {
                           buf[j]=(byte)source.read();
                           j++;
                        }
                        source.close();
                        sendMessageToClients(buf);

                        finalFileB= new File(encodeFile(pathFiles+"fileEncode.txt",pathFiles+filename+"\\DISK"+i+"\\fileParity"+i+".txt"));
                        FileInputStream sourceB = new FileInputStream(finalFileB);
                        byte bufB[]=new byte[60000];
                        j=0;
                        while(sourceB.available()!=0)
                        {
                           bufB[j]=(byte)sourceB.read();
                           j++;
                        }
                        sourceB.close();
                        sendMessageToClients(bufB);
                     }
                  }
               }
            }//end of try
            catch (IOException i ) {
               System.out.println(i.getMessage());
            }

         }//end of while

      }//end of run 
   }//end of UDP thread

   /**
    * That sends an array of bytes via udp to Controller_Node_Java
    * @param outgoingByte bytes of the file to send
    */
   public void sendMessageToClients(byte[] outgoingByte)  {
    try {

       //Unique string to keep track of clients
       String currentClient = "Client IP: " + currentClientIP.toString() + " | Port: " + currentClientPort;

       //Add the client to list of connected clients,
       //And store their IP and PORT
       if (!connectedClients.contains(currentClient)) {
          connectedClients.add(currentClient);
          clientIPs.add(currentClientIP);
          clientPorts.add(currentClientPort);
          System.out.println("Added new client | " + currentClient);
       }

       //For every client connected
       for (int i = 0; i < clientIPs.size(); i++) {
          //get their IP and Port
          InetAddress clientIp = clientIPs.get(i);
          int clientPort = clientPorts.get(i);

          //construct Packet
          udpPacket = new DatagramPacket(outgoingByte, outgoingByte.length, clientIp, clientPort);
          //send message out
          udpSocket.send(udpPacket);

          System.out.println("Message sent to client #" + i);
       }//end of for

       System.out.println("\nSuccessfully sent message to all Clients!\n\n");

    }catch (IOException ioe) {
         System.out.println(ioe.getMessage());
      }
   }
   /**
    * Decode a file with huffman
    * @param pathFileEncode encoded file path
    * @param pathFileDecode decoded file path
    * @return decoded file path
    */
public String decodeFile(String pathFileEncode, String pathFileDecode){
   File fileEncode = new File(pathFileEncode);
   try (OutputStream os = new FileOutputStream(fileEncode)) {
      os.write(byteBuffer);
   } catch (Exception e) {
      throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
   }
   File file = new File(pathFileDecode);
   HuffmanEncoding huffman = new HuffmanEncoding();
   huffman.decode(pathFileEncode,pathFileDecode);
   return pathFileDecode;
}
   /**
    * Encode a file with huffman
    * @param pathFileEncoded encoded file
    * @param pathFileDecodeNameFile filename to encode
    * @return encoded file
    */
public String encodeFile(String pathFileEncoded, String pathFileDecodeNameFile){
   File fileSaved= new File(pathFileDecodeNameFile);
   HuffmanEncoding huffman = new HuffmanEncoding();//encode the file
   String fileOutPath=(pathFileEncoded);
   File fileOut= new File(fileOutPath);
   huffman.encoding(fileSaved.getPath(),fileOutPath);
   return pathFileEncoded;
}
   /**
    * Get current time
    * @return current time
    */
   public String getCurrentTime() {
      String time =
              new SimpleDateFormat("hh:mm:ss").format(Calendar.getInstance().getTime());
      return time;
   }

   /**
    * Read a specific file
    * @param path path where you will save the file
    * @return contents of a file
    */
   public String readFile(String path) {
    String text="";
      try {
         FileReader file=new FileReader(path);
         int c=0;
         while(c!=-1) {
            c=file.read();
            char content=(char)c;
            text+=content;
         }
         file.close();
      } catch (IOException e) {

         System.out.println("No se ha encontrado el archivo");
      }
      return text;
   }
}
