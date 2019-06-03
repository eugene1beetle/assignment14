package com.shpp.p2p.cs.ezhuk.assignment14;

/**
 * Archiver. Accepts arguments.
 * Read file, get unique bytes => get binary word length, get map<newByte,oldByte>, create archive
 *
 * Example arguments:
 * -a test.txt testarchive.par
 * test (.txt archived to .par)
 * -u test.par
 * -a test.txt test.par
 * -a test.par test.par1
 */
public class Assignment14 {
    public static void main(String[] args) {
        Assignment14 a14 = new Assignment14(); // for non static methods
        boolean isArchive = a14.checkParamsForArchive(args); // true = archive
        String[] IOFileName = a14.getFileNames(args, isArchive); // [0] - File in; [1] - File out
        System.out.printf("%s file \"%s\" to file \"%s\"\n",
                isArchive? "Archived" : "Unarchived", IOFileName[0], IOFileName[1]);

        Archiver archiver = new Archiver(isArchive, IOFileName[0], IOFileName[1]);
    }

    /**
     * Determines whether to archive
     *
     * @param args Input arguments
     * @return     True if archive
     */
    private boolean checkParamsForArchive(String[] args) {
        if (args.length == 0) printAndBreak("No input arguments");
        if (args[0].equals("-u") || args[0].equals("-a")) return args[0].equals("-a");
        if (args[0].contains(".")) return !args[0].split("\\.")[1].equals("par");
        else return true;
    }

    /**
     * Gets filenames for read / save from arguments
     *
     * @param args      Input arguments
     * @param isArchive Indicates whether to archive
     * @return          fileNames[2] {"read-filename" , "save-filename"}
     */
    private String[] getFileNames(String[] args, boolean isArchive) {
        if (args.length > 3) printAndBreak("Too many arguments");
        String[] fileNames = new String[2];
        if (args[0].equals("-u") || args[0].equals("-a")) {
            if (args.length == 1) {
                fileNames[0] = fileNames[1] = "test";
                if (isArchive) {
                    fileNames[0] += ".txt";
                    fileNames[1] += ".par";
                } else  {
                    fileNames[0] += ".par";
                    fileNames[1] += ".txt";
                }
            }

            if (args.length == 2) {
                if (args[1].contains(".")) fileNames[0] = args[1];
                else {
                    if (isArchive) fileNames[0] = args[1] + ".txt";
                    else fileNames[0] = args[1] + ".par";
                }
                if (isArchive) fileNames[1] = fileNames[0].split("\\.")[0] + ".par";
                else fileNames[1] = fileNames[0].split("\\.")[0] + ".txt";
            }

            if (args.length == 3) {
                if (args[1].contains(".")) fileNames[0] = args[1];
                else {
                    fileNames[0] = args[1];
                    fileNames[0] += isArchive? ".txt" : ".par";
                }
                if (args[2].contains(".")) fileNames[1] = args[2];
                else {
                    fileNames[1] = args[2];
                    fileNames[1] += isArchive? ".par" : ".txt";
                }
            }
        } else {
            if (args.length == 1) {
                if (args[0].contains(".")) fileNames[0] = args[0];
                else {
                    if (isArchive) fileNames[0] = args[0] + ".txt";
                    else fileNames[0] = args[0] + ".par";
                }
                if (isArchive) fileNames[1] = fileNames[0].split("\\.")[0] + ".par";
                else fileNames[1] = fileNames[0].split("\\.")[0] + ".txt";
            }

            if (args.length == 2) {
                if (args[0].contains(".")) fileNames[0] = args[0];
                else {
                    fileNames[0] = args[0];
                    fileNames[0] += isArchive ? ".txt" : ".par";
                }
                if (args[1].contains(".")) fileNames[1] = args[1];
                else {
                    fileNames[1] = args[1];
                    fileNames[1] += isArchive ? ".par" : ".txt";
                }
            }
        }
        return fileNames;
    }

    /**
     * Method that displays a message on the screen and terminates the program with a status of -1
     *
     * @param toPrint message
     */
    private void printAndBreak(String toPrint) {
        System.out.println(toPrint);
        System.exit(-1);
    }
}