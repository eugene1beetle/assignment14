package com.shpp.p2p.cs.ezhuk.assignment14;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * class Archiver, can compress and decompress files.
 */
class Archiver {

    // Path to directory in / out files
    private static final String PATH = "src/com/shpp/p2p/cs/ezhuk/assignment14/files/";

    // map for code / decode file bytes
    private HashMap<Byte,Byte> map = new HashMap<>();

    /**
     * The class constructor. Displays information, archives or unzips the file, saves the result
     *
     * @param isArchive   Indicates whether to archive
     * @param inFileName  Filename to read
     * @param outFileName Filename to write
     */
    Archiver(boolean isArchive, String inFileName, String outFileName) {
        byte[] file = readFile(inFileName);
        if (file.length == 0) {System.out.println("File is empty");System.exit(-1);}
        if (isArchive) {
            long workTime = System.currentTimeMillis();
            byte[] archive = archive(file);
            System.out.printf("File size %19d\n", file.length);
            System.out.printf("Archive size %16d\n", archive.length);
            System.out.printf("Compression efficiency%6s%.3f\n", " ", (float) file.length / archive.length);
            writeFile(archive, outFileName);
            System.out.printf("Archiving time = %10s %.3f", " ", (System.currentTimeMillis() - workTime) / 1000.0);
        }
        if (!isArchive) {
            long workTime = System.currentTimeMillis();
            byte[] unArchive = unArchive(file);
            System.out.printf("File size %19d\n", file.length);
            System.out.printf("Archive size %16d\n", unArchive.length);
            System.out.printf("Decompression efficiency%4s%.3f\n", " ", (float) unArchive.length / file.length);
            writeFile(unArchive, outFileName);
            System.out.printf("Unarchiving time %10s %.3f\n", " ", (System.currentTimeMillis() - workTime) / 1000.0);
        }
    }

    /**
     * Method to archive a file, reduces bytes from 8 to wordLength bits
     *
     * @param byteFile input file as byte[]
     * @return         archive file as byte[]
     */
    private byte[] archive(byte[] byteFile) {
        int wordLength = getWordLength(byteFile);
        ArrayList<Byte> archive = new ArrayList<>();
        StringBuilder binaryWord = new StringBuilder();
        for (byte b : byteFile) {
            binaryWord.append(getBinaryWord(map.get(b), wordLength));
            if (binaryWord.length() >= 8) {
                archive.add(myParseByte(binaryWord.substring(0, 8)));
                binaryWord.delete(0, 8);
            }
        }
        int lastByteLength = 0;
        if (binaryWord.length() > 0) {
            lastByteLength = binaryWord.length();
            archive.add(Byte.parseByte(binaryWord.toString(), 2));
        }
        System.out.printf("Map size %20d\n", map.size());
        return createArray(archive, wordLength, lastByteLength);
    }

    /**
     * Unzip the file, take a byte with the length of wordLength and extract a byte from the map with a length of 8
     *
     * @param file Archived file as byte[]
     * @return     Unarchived file as byte[]
     */
    private byte[] unArchive(byte[] file) {
        ArrayList<Byte> unArchive = new ArrayList<>();
        int mapSize = (byteToUnsignedInt(file[0]) + 1) * 2;
        int wordLength = byteToUnsignedInt(file[1]);
        int lastByteLength = byteToUnsignedInt(file[2]);
        for (int i = 0; i < mapSize; i += 2) map.put(file[i + 4], file[i + 3]);
        StringBuilder binaryWord = new StringBuilder();
        for (int i = mapSize + 3; i < file.length; i++) {
            if (i == file.length - 1  && lastByteLength > 0) binaryWord.append(getBinaryWord(file[i], lastByteLength));
            else binaryWord.append(getBinaryWord(file[i], 8));
            while (binaryWord.length() >= wordLength) {
                String byteStr = binaryWord.substring(0, wordLength);
                binaryWord.delete(0, wordLength);
                if (byteStr.length() == 8 && byteStr.charAt(0) == '1') unArchive.add(map.get(myParseByte(byteStr)));
                else unArchive.add(map.get((byte) Integer.parseInt(byteStr, 2)));
            }
        }
        return byteArrayListToArray(unArchive);
    }

    /**
     * Gets int from byte, ignore last (minus) bit
     *
     * @param b The byte from which we extract
     * @return  Unsigned int
     */
    private int byteToUnsignedInt(byte b) {
        String byteStr = Integer.toBinaryString(b);
        if (byteStr.length() >= 8) byteStr = byteStr.substring(byteStr.length() - 8);
        return Integer.parseInt(byteStr,2);
    }

    /**
     * Translate binary word into byte
     *
     * @param binaryWord Binary word as string
     * @return           Byte, include last (minus) bit
     */
    private byte myParseByte(String binaryWord) {
        byte result = Byte.parseByte(binaryWord.substring(1),2);
        if (binaryWord.length() == 8 && binaryWord.charAt(0) == '1') result -= 128;
        return result;
    }

    /**
     * Translate byte into binary word
     *
     * @param b          Byte that translate
     * @param wordLength Desired binary word length
     * @return           Binary word as string
     */
    private String getBinaryWord(byte b, int wordLength) {
        String byteStr = Integer.toBinaryString(b);
        if (byteStr.length() > 8) byteStr = byteStr.substring(byteStr.length() - 8);
        if (byteStr.length() < wordLength) byteStr = String.format("%0" + wordLength + "d", Integer.parseInt(byteStr));
        return byteStr;
    }

    /**
     * Creates an array that contains wordLength, mapSize, lastByteLength and map<oldByte,newByte>
     *
     * @param archive        Archive bytes
     * @param wordLength     Binary word length
     * @param lastByteLength Last byte length
     * @return               ArrayList to translate into array
     */
    private byte[] createArray(ArrayList<Byte> archive, int wordLength, int lastByteLength) {
        for (Map.Entry<Byte,Byte> e : map.entrySet()) {
            archive.add(0,e.getValue());
            archive.add(0,e.getKey());
        }

        archive.add(0, (byte) lastByteLength);
        archive.add(0, (byte) wordLength);
        archive.add(0, getMapSize());

        return byteArrayListToArray(archive);
    }

    /**
     * Translate ArrayList into byte array
     *
     * @param arrayList ArrayList to translate into an array
     * @return          Array, created from ArrayList
     */
    private byte[] byteArrayListToArray(ArrayList<Byte> arrayList) {
        byte[] result = new byte[arrayList.size()];
        for (int i = 0; i < result.length; i++) result[i] = arrayList.get(i);
        return result;
    }

    /**
     * Method for gets map size as byte (-1 because some times map size is 256. To save it needs 9 bits)
     *
     * @return Map size to save in archive
     */
    private byte getMapSize() {
        String mapSize = Integer.toBinaryString(map.size() - 1);
        if (mapSize.length() < 8) return Byte.parseByte(mapSize,2);
        else {
            byte b = Byte.parseByte(mapSize.substring(1),2);
            b -= 128;
            return b;
        }
    }

    /**
     * The method recognizes the length of the binary word for encoding
     * (And fill map of unique bytes)
     *
     * @param bytes Input file as byte[]
     * @return      New binary word length
     */
    private int getWordLength(byte[] bytes) {
        ArrayList<Byte> byteArrayList = new ArrayList<>();
        for (byte aByte : bytes)
            if (!byteArrayList.contains(aByte)) byteArrayList.add(aByte);
        double len = Math.log(byteArrayList.size()) / Math.log(2);
        if (len / (int) len != 1) len = ((int) len) + 1;
        System.out.printf("Unique bytes %16d\nWord length %17d\n",byteArrayList.size(),(int)len);

        byte counter = 0;
        for (byte b : byteArrayList) {
            map.put(b,counter);
            counter++;
        }

        return (int) len;
    }

    /**
     * Read file as byte[]
     *
     * @param filename filename
     * @return         file as byte[]
     */
    private byte[] readFile(String filename) {
        try {
            System.out.println("Path: " + Paths.get(PATH + filename));
            return Files.readAllBytes(Paths.get(PATH + filename));
        } catch (Exception e) {
            System.out.println("Can't read file");
            System.exit(-1);
            return null;
        }
    }

    /**
     * Method for save file in directory PATH
     *
     * @param file        File as byte[]
     * @param outFileName Filename to save
     */
    private void writeFile(byte[] file, String outFileName) {
        try (FileOutputStream outputStream = new FileOutputStream(PATH + outFileName)) {
            outputStream.write(file);
        } catch (IOException e) {
            System.out.println("Can't write file\nPath: " + (PATH + outFileName));
        }
    }
}