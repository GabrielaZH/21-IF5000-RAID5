import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RAID5 {
    private static PrintWriter w;


    /*Crea carpetas temporales de discos donde iran el archivo con el trozo del  libro y su paridad*/
    public void createDisks(List<String> disks, String path){
        File disk;
        for (String name : disks){
            disk = new File(path, name);
            if (!disk.exists()){
                if (!disk.mkdirs()) System.out.printf("%s não pode ser criado!\n", name);
                else System.out.printf("%s criado com sucesso!\n", name);
            }
        }
    }

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

    public int getMajorSize(List<List<Byte>> superlist) {
        int major=superlist.get(0).size();
        for (int i = 1; i < superlist.size(); i++) {
            if(major<superlist.get(i).size()){
                major=superlist.get(i).size();
            }
        }
        return major;
    }

    public void fillBytesFile(List<Byte> bytesArray, File file){
        try {
            w = new PrintWriter(file);
            for (byte value : bytesArray) w.println(value);
            w.close();
            System.out.printf("Arquivo %s criado com sucesso!\n", file.getName());
        } catch (FileNotFoundException e) {
            System.out.println("Não foi possível criar o arquivo solicitado!");
        }
    }
}
