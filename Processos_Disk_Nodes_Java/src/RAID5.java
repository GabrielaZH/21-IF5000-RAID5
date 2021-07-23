import java.io.*;
import java.util.ArrayList;
import java.util.List;

/***************************************************************************************
 *
 *  Utility: Allows you to divide a file into chunks with redundancy, similar to RAID5
 *  @author GabrielaZH-JoseKatoche/21-IF5000-RAID5
 **************************************************************************************/

public class RAID5 {
    private static PrintWriter w;


    /**
     * Create disk folder where a piece of the file and its parity file will be inside
     * @param disks disks folder to create
     * @param path path where it will be saved
     */
    public void createDisks(List<String> disks, String path){
        File disk;
        for (String name : disks){
            disk = new File(path, name);
            if (!disk.exists()){
                if (!disk.mkdirs()) System.out.printf("%s não pode ser criado!\n", name);
            }
        }
    }
    /**
     * Change a file to a byte array
     * @param file to which you want to apply raid 5
     * @return bytes of file
     */
    public byte[] parseFileToByte(File file){
        int fileLength = (int)file.length();
        byte[] bytesArray = new byte[fileLength];
        FileInputStream fileLikeStream;

        try {
            fileLikeStream = new FileInputStream(file);
            fileLikeStream.read(bytesArray, 0, fileLength);

        } catch (FileNotFoundException fnfex) {
            System.out.println("Arquivo não encontrado!");
        } catch (IOException ioex) {
            System.out.println("Erro inesperado!");
        }

        return bytesArray;
    }
    /**
     * Save file with raid 5
     * @param bytesArray bytes of a file
     * @param numberDisk disks created
     * @param path path where it was saved
     */
    public void saveFileWithRAID5(byte[] bytesArray, int numberDisk, String path){
        //variaveis locais
        List<Byte> fileParity = new ArrayList();

        //CREA LAS LISTAS
        List<List<Byte>> superlist = new ArrayList<List<Byte>>();
        for (int i = 0; i < numberDisk; i++) {
            superlist.add(new ArrayList());
        }

        int countFile=numberDisk-1;
        for(byte element : bytesArray) {
            superlist.get(countFile).add(element);
            countFile--;
            if (countFile==-1){
                countFile=numberDisk-1;
            }
        }

        int major = getMajorSize(superlist);
        completeDisks(superlist,major);


        byte subtraction = 0;
        for (int i = 0; i < superlist.get(superlist.size()-1).size(); i++){
            subtraction = superlist.get(superlist.size()-1).get(i);

            for (int j = numberDisk-1; j > 0; j--) {
                subtraction= (byte) (subtraction-superlist.get(j-1).get(i));
            }

            fileParity.add(subtraction);
        }

        for (int i = 0; i < numberDisk; i++) {
            fillBytesFile(superlist.get(i), new File(path + "DISK"+i, "file"+i+".txt"));
            fillBytesFile(fileParity, new File(path + "DISK"+i, "fileParity"+i+".txt"));
        }
    }

    /**
     * Complete the number of disks if one was eliminated
     * @param superlist list of bytes
     * @param major the number mayor
     */
    public void completeDisks(List<List<Byte>> superlist, int major) {
        int substraction;

        for (int i = 0; i < superlist.size(); i++) {
            substraction = major - superlist.get(i).size();
            if(substraction!=0){
                for (int j = 0; j < substraction; j++) {
                    superlist.get(i).add((byte) 0);
                }
            }
        }
    }

    /**
     * Get major size
     * @param superlist list of bytes
     * @return int
     */
    public int getMajorSize(List<List<Byte>> superlist) {
        int major=superlist.get(0).size();
        for (int i = 1; i < superlist.size(); i++) {
            if(major<superlist.get(i).size()){
                major=superlist.get(i).size();
            }
        }
        return major;
    }

    /**
     * Fill the byte file
     * @param bytesArray list of bytes
     * @param file list of bytes
     */
    public void fillBytesFile(List<Byte> bytesArray, File file){
        try {
            w = new PrintWriter(file);
            for (byte value : bytesArray) w.println(value);
            w.close();
        } catch (FileNotFoundException e) {
            System.out.println("Não foi possível criar o arquivo solicitado!");
        }
    }
}
