import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class SicTranslator {
    private String programLen;
    private File rawFile;
    private final File passOneFile = new File("../../../src/passOne.txt");
    private final File symbolTable = new File("../../../src/symbolTable.txt");
    private final File passTwoFile = new File("../../../src/passTwo.txt");
    private final File HTERecord = new File("../../../src/HTERecord.txt");
    private Map<String, String> symbolMap;
    private String base;
    private String modString = "";

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
        if (hex == null) {
            return "1111";
        }
        String pad = "0";
        if (hex.contains("-")){
            pad = "F";
            hex = hex.substring(1);
        }
        StringBuilder hexBuilder = new StringBuilder(hex);
        while (hexBuilder.length() < padding) {
            hexBuilder.insert(0, pad);
        }
        hex = hexBuilder.toString();
        return hex;
    }

    private String instructionSize(String inst) {
        if (inst.contains("+")) {
            return "4";
        }
        try {
            return Converter.OPTAB.get(inst)[0];
        } catch (NullPointerException ignored) {
            return "0";
        }
        // System.out.println(inst);
    }
    
    private String binaryToHex(String binary) {
        return Integer.toString(Integer.parseInt(binary, 2), 16);
    }
    
    private String indirect(String ref) {
        ref = symbolMap.get(ref);
        for (Map.Entry<String, String> entry : symbolMap.entrySet()) {
            if (ref.equals(entry.getValue())){
                ref = entry.getKey();
                break;
            }
        }
        return symbolMap.get(ref);
    }

    private String instToObjCode(String inst, String ref, String currentAddr) {
        String n = "1", i = "1", x = "0", b = "0", p = "0", e = "0";
        String format, opCode;
        boolean immediate = false;
         
        try {
            if (inst.contains("+")){
                format = "4";
                inst = inst.substring(1);
                opCode = Converter.OPTAB.get(inst)[1];
            }
            else {
                format = Converter.OPTAB.get(inst)[0];
                opCode = Converter.OPTAB.get(inst)[1];
            }
        } catch (Exception ex) {
            return "No obj. code";
        }

        String objCode = "0";

        if (ref.contains("#")) {
            n = "0";
            ref = ref.substring(1);
            immediate = true;
        }
        else if (ref.contains("@")) {
            i = "0";
            ref = ref.substring(1);
        }
        if (ref.contains(",X")) {
            x = "1";
            ref = ref.replace(",X", "");
        }
        
        switch (format) {
            case "3":
                String addr = "000";
                try {
                    if (immediate) {
                        if (symbolMap.containsKey(ref)){
                            ref = symbolMap.get(ref);
                        }
                        return stringPadding(hexAddition(opCode, binaryToHex((n+i))), 2) + binaryToHex((x+b+p+e)).toUpperCase() + stringPadding(ref, 3);
                    }

                    int disp = Integer.parseInt(symbolMap.get(ref), 16) - Integer.parseInt(hexAddition(currentAddr, format), 16);
                    if (-2048 <= disp && disp <= 2047){
                        p = "1";
                        if (disp < 0){
                            addr = (Integer.toHexString(disp)).toUpperCase();
                            addr = "-" + addr.replace("F", "");
                        }
                        else {
                            addr = Integer.toString(disp, 16);
                        }
                        addr = stringPadding(addr, 3);
                    }
                    else {
                        b = "1";
                        disp = Integer.parseInt(symbolMap.get(ref), 16) - Integer.parseInt(this.base, 16);
                        addr = Integer.toString(disp, 16);
                        addr = stringPadding(addr, 3);
                    }
                    objCode = stringPadding(hexAddition(opCode, binaryToHex((n+i))), 2) + binaryToHex((x+b+p+e)).toUpperCase() + addr.toUpperCase();
                    return objCode;
                } catch (Exception ignored) {
                    objCode = stringPadding(hexAddition(opCode, binaryToHex((n+i))), 2) + binaryToHex((x+b+p+e)).toUpperCase() + "000";
                    return objCode;
                }
            case "4":
                e = "1";
                if (immediate) {
                    if (symbolMap.containsKey(ref)){
                        ref = symbolMap.get(ref);
                    }
                    return stringPadding(hexAddition(opCode, binaryToHex((n+i))), 2) + binaryToHex((x+b+p+e)).toUpperCase() + stringPadding(ref, 5);
                }
                modString = modString + stringPadding(hexAddition(currentAddr, "1"), 4) +".05\nM^";
                objCode = stringPadding(hexAddition(opCode, binaryToHex((n+i))), 2) + binaryToHex((x+b+p+e)) + stringPadding(symbolMap.get(ref), 5);
                return objCode;
            case "2":
                String[] r = ref.split(",");
                if (r.length == 1) {
                    return opCode + registerToHex(r[0]);
                }
                else {
                    return opCode + registerToHex(r[0]) + registerToHex(r[1]);
                }
            case "1":
                return opCode;
            default:
                return "No obj. Code";
        }
    }
    
    public static String registerToHex(String r) {
       switch (r) {
            case"A":
                return "0";
            case"X":
                return "1";
            case"L":
                return "2";
            case"PC":
                return "8";
            case"SW":
                return "9";
            default:
                return r;
       } 
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
            if(Objects.equals(instruction.split(" ")[1].toUpperCase(), "START")){
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
                    switch (splitInstruction[1].toUpperCase()){
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
                        case "WORD":
                            break;
                        default:
                            additionAddr = instructionSize(splitInstruction[1]);
                            break;
                    }
                }
                // Checks format of the instruction incase it wasnt a reservation
                else {
                    additionAddr = instructionSize(splitInstruction[0]);
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
        symbolMap = symbolTableToMap();
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
                
                if (instruction.contains("BASE")) {
                    String baseAddr = splitInstruction[splitInstruction.length-1];
                    if (baseAddr.contains("#")) {
                        baseAddr = baseAddr.substring(1);
                        this.base = baseAddr;
                    }
                    else if (baseAddr.contains("@")) {
                        baseAddr = baseAddr.substring(1);
                        this.base = symbolMap.get(baseAddr);
                    }
                    else if (baseAddr.contains(",X")) {
                        baseAddr = baseAddr.replace(",X", "");
                        this.base = symbolMap.get(baseAddr);
                    }
                    else {
                        this.base = symbolMap.get(baseAddr);
                    }
                }

                // Recognizes labels
                if(splitInstruction.length == 4){
                    switch (splitInstruction[2].toUpperCase()) {
                        case "RESW":
                        case "RESB":
                            passTwoWriter.println(instruction + " " + "No obj. code");
                            break;
                        case "WORD":
                            opCodeAddr = Integer.toHexString(Integer.parseInt(splitInstruction[3]));
                            opCodeAddr = stringPadding(opCodeAddr, 6);
                            passTwoWriter.println(instruction + " " + opCodeAddr);
                            break;
                        case "BYTE":
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
                            break;
                        default:
                            passTwoWriter.println(instruction + " " + instToObjCode(splitInstruction[2], splitInstruction[3], splitInstruction[0]));
                            break;
                    }
                }
                else if (splitInstruction.length == 3) {
                    passTwoWriter.println(instruction + " " + instToObjCode(splitInstruction[1], splitInstruction[2], splitInstruction[0]));
                }
                else{
                    passTwoWriter.println(instruction + " " + instToObjCode(splitInstruction[1], "-", splitInstruction[0]));
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
                    if(instruction.contains("code")){continue;}
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
                        int length = 0;
                        for (String code: objCodes.toString().split(" ")) {
                            length += code.length()/2;
                        }
                        HTEWriter.print("^"+stringPadding(Integer.toHexString(length), 2)+"^"+ objCodes);
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
                int length = 0;
                for (String code: objCodes.toString().split(" ")) {
                    length += code.length()/3;
                }
                HTEWriter.print("^"+stringPadding(Integer.toHexString(length), 2)+"^"+ objCodes);
            }
            if(modString.length() > 0){
                HTEWriter.print("\nM^"+modString);
            }
            HTEWriter.print("E^"+ programStart);
            HTEWriter.close();
        }catch (IOException ignored){

        }
    }
}
