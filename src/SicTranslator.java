import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class SicTranslator {
    private String programLen;
    private File rawFile;
    private final File passOneFile = new File("./passOne.txt");
    private final File symbolTable = new File("./symbolTable.txt");
    private final File passTwoFile = new File("./passTwo.txt");
    private final File HTERecord = new File("./HTERecord.txt");


    // Gets the file and checks its existence
    public SicTranslator(String inputFileName)
    {
        File file = new File(inputFileName);

        if(file.isFile()){
            this.rawFile = file;
            System.out.println("File was found!");
        }
        else{
            System.out.println("File not found!");
        }
    }

    private static String hexAddition(String hex1, String hex2){
        return Integer.toHexString(Integer.parseInt(hex1, 16) + Integer.parseInt(hex2, 16)).toUpperCase();
    }

    private static String programLengthCalculator(String startHex, String endHex){
        return Integer.toHexString(Integer.parseInt(endHex, 16) - Integer.parseInt(startHex, 16)).toUpperCase();
    }

    // Takes the generated Symbol Table from pass one and makes a hashtable from it
    private Map<String, String> symbolTableToMap(){
        try
        {
            Scanner scanner = new Scanner(symbolTable);
            Map<String, String> symbolMap = new HashMap<>();
            String[] line;
            while (scanner.hasNextLine()){
                line = scanner.nextLine().split(" ");
                symbolMap.put(line[0], line[1]);
            }
            return symbolMap;
        }
        catch (IOException e){
            System.out.println("Error in symbol conversion has occurred");
        }
        return null;
    }

    // Resolves bytes that are normal letters for example "EOF" to its corresponded hex representation
    private String lettersToHex(String letters){
        StringBuilder hex = new StringBuilder();
        for (char c : letters.toCharArray()){
            hex.append(Integer.toHexString(c));
        }

        return hex.toString();
    }

    // Pads the hex value passed with padding*0s to the left
    private String stringPadding(String hex, int padding){
        StringBuilder hexBuilder = new StringBuilder(hex);
        while (hexBuilder.length() != padding) {
            hexBuilder.insert(0, "0");}
        hex = hexBuilder.toString();
        return hex;
    }

    public void passOne(){
        String additionAddr;
        String startAddr;
        String currentAddress;

        try
        {
            // Gets read and write access into the files
            Scanner rawInputReader = new Scanner(rawFile);
            PrintWriter passOneWriter = new PrintWriter(passOneFile);
            PrintWriter symbolTableWriter = new PrintWriter(symbolTable);

            // Recognize Starting address
            String instruction = rawInputReader.nextLine();
            if(Objects.equals(instruction.split(" ")[1], "Start")){
                currentAddress = instruction.split(" ")[2];
                startAddr = currentAddress;
                passOneWriter.println(instruction);
            } else {
                System.out.println("Start instruction not found");
                return;
            }

            // Loop over file contents
            while (rawInputReader.hasNextLine()) {
                instruction = rawInputReader.nextLine();
                additionAddr = "3";
                // Adds address and instruction
                passOneWriter.println(currentAddress + " " + instruction);
                // Calculate if there are any special cases for addresses
                // Resolves labels
                String[] splitInstruction = instruction.split(" ");
                if(splitInstruction.length > 2){
                    symbolTableWriter.println(splitInstruction[0] + " " + currentAddress);
                    // Checks if the labels are memory reservations
                    switch (splitInstruction[1]){
                        case "RESW":
                            additionAddr = Integer.toHexString(3*Integer.parseInt(splitInstruction[2]));
                            break;
                        case "RESB":
                            additionAddr = Integer.toHexString(Integer.parseInt(splitInstruction[2]));
                            break;
                        case "BYTE":
                            String targetByte = splitInstruction[2];
                            if(targetByte.toCharArray()[0] == 'C'){
                                additionAddr = Integer.toHexString(targetByte.toCharArray().length-3);
                                break;
                            }
                            else if(targetByte.toCharArray()[0] == 'X'){
                                additionAddr = Integer.toHexString((targetByte.toCharArray().length-3)/2);
                                break;
                            }
                        default:
                            break;
                    }
                }
                // If there is no more lines make sure to save the program length
                if(!rawInputReader.hasNextLine()){
                    this.programLen = programLengthCalculator(startAddr, currentAddress);
                    System.out.println(this.programLen);
                    break;
                }
                // Increment the location counter appropriately
                currentAddress = stringPadding(hexAddition(currentAddress, additionAddr),4);
            }

            // Closes read and write pipes
            rawInputReader.close();
            passOneWriter.close();
            symbolTableWriter.close();
        }
        catch (IOException e) {
            System.out.println("Error in passOne has occurred");
        }
    }

    public void passTwo()
    {
        // Generates symbol map
        Map<String, String> symbolMap = symbolTableToMap();
        if(symbolMap == null){
            System.out.println("Error in symbol conversion, pass two has occurred");
            return;
        }

        try
        {
            // Gets read and write into files
            Scanner passOneReader = new Scanner(passOneFile);
            PrintWriter passTwoWriter = new PrintWriter(passTwoFile);

            String instruction;
            passTwoWriter.println(passOneReader.nextLine());

            while(passOneReader.hasNextLine()){
                instruction = passOneReader.nextLine();
                String[] splitInstruction = instruction.split(" ");
                String opCodeAddr;

                // Recognizes labels
                if(splitInstruction.length > 3){
                    if(Objects.equals(splitInstruction[2], "RESW") || Objects.equals(splitInstruction[2], "RESB")){
                        passTwoWriter.println(instruction + " " + "No obj. code");
                    }
                    // Process object codes for memory
                    else if(Objects.equals(splitInstruction[2], "WORD")){
                        opCodeAddr = Integer.toHexString(Integer.parseInt(splitInstruction[3]));
                        opCodeAddr = stringPadding(opCodeAddr, 6);
                        passTwoWriter.println(instruction + " " + opCodeAddr);
                    }
                    else if (Objects.equals(splitInstruction[2], "BYTE")){
                        String value = splitInstruction[3];
                        if(value.contains("C'")){
                            value = value.substring(2, value.length() - 1);
                            value = lettersToHex(value);
                        }
                        else {
                            value = value.substring(2, value.length() - 1);
                        }
                        value = stringPadding(value, 6);
                        passTwoWriter.println(instruction + " " + value);
                    }
                    else {
                        String finalAddr;
                        // Checks addressing mode and finds address
                        if(splitInstruction[3].contains(",X")){
                            finalAddr = splitInstruction[3].replace(",X", "");
                            finalAddr = symbolMap.get(finalAddr);
                            finalAddr = hexAddition(finalAddr, "1");
                        }
                        else {
                            finalAddr = symbolMap.get(splitInstruction[3]);
                        }

                        opCodeAddr = splitInstruction[2];
                        try{
                            passTwoWriter.println(instruction + " " + (Converter.OPTAB.get(opCodeAddr)[1]) + stringPadding(finalAddr,4));
                        }
                        catch (Exception ignored){

                        }
                    }

                }
                else {
                    String finalAddr;
                    if(splitInstruction[2].contains(",X")){
                        finalAddr = splitInstruction[2].replace(",X", "");
                        finalAddr = symbolMap.get(finalAddr);
                        finalAddr = hexAddition(finalAddr, "1");
                    }
                    else {
                        finalAddr = symbolMap.get(splitInstruction[2]);
                    }
                    opCodeAddr = splitInstruction[1];
                    try{
                        passTwoWriter.println(instruction+ " " + Converter.OPTAB.get(opCodeAddr)[1]+ stringPadding(finalAddr,4));
                    }
                    catch (Exception ignored){

                    }
                }
            }

            // Closes read and write pipes
            passOneReader.close();
            passTwoWriter.close();
        }
        catch (IOException e) {
            System.out.println("Error in passOne has occurred");
        }
    }

    public void HTERecord(){
        try {
            Scanner passTwoReader = new Scanner(passTwoFile);
            PrintWriter HTEWriter = new PrintWriter(HTERecord);

            String[] splitInstruction;

            StringBuilder objCodes = new StringBuilder();
            boolean inRecord = false;
            int counter = 0;

            //Grabs first line
            String instruction = passTwoReader.nextLine();
            String programStart = stringPadding(instruction.split(" ")[2], 6);
            // Adds program start
            HTEWriter.print("H^"+instruction.split(" ")[0]+"^"+ programStart +"^"+ stringPadding(this.programLen, 6));
            // Loops over file
            while (passTwoReader.hasNextLine()){
                instruction = passTwoReader.nextLine();
                splitInstruction = instruction.split(" ");

                // Checks if a T record start or continuation is valid
                if(!(instruction.contains("RESW") || instruction.contains("RESB")) && counter<10){
                    // If in a T record increment obj counter and add the objCode to the string
                    if(inRecord){
                        counter++;
                        objCodes.append(" ").append(splitInstruction[splitInstruction.length - 1]);
                    }
                    // If not in a T record start a new one and add the objCode to the string and set the flag to true
                    else {
                        HTEWriter.print("\nT^"+stringPadding(splitInstruction[0],6));
                        objCodes.append(" ").append(splitInstruction[splitInstruction.length - 1]);
                        counter++;
                        inRecord = true;
                    }
                }
                // Process at the end of a T record
                else {
                    // Adds obj codes with their lengths
                    if(objCodes.toString().split(" ").length-1 != 0){
                        HTEWriter.print("^"+stringPadding(Integer.toHexString((objCodes.toString().split(" ").length-1)*3), 2)+"^"+ objCodes);
                    }
                    // This is important to not lose the opj code when exiting on counter condition
                    if(counter>=10){
                        if(!instruction.contains("code")){
                            HTEWriter.print("\nT^"+stringPadding(splitInstruction[0],6));
                            objCodes = new StringBuilder(" " + splitInstruction[splitInstruction.length - 1]);
                        }
                    }
                    else {
                        inRecord = false;
                        objCodes = new StringBuilder();
                    }
                    counter = 0;
                }
            }
            if(objCodes.length() > 0){
                HTEWriter.print("^"+stringPadding(Integer.toHexString((objCodes.toString().split(" ").length-1)*3), 2)+"^"+ objCodes);
            }
            HTEWriter.print("\nE^"+ programStart);
            HTEWriter.close();
        }catch (IOException ignored){

        }
    }
}
