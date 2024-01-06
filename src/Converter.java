import java.util.HashMap;
import java.util.Map;

public class Converter {

    public static final Map<String, String[]> OPTAB = new HashMap<>(){
        {
            put("FIX", new String[]{"1", "C4"});
            put("FLOAT", new String[]{"1", "C0"});
            put("HIO", new String[]{"1", "F4"});
            put("NORM", new String[]{"1", "C8"});
            put("SIO", new String[]{"1", "F0"});
            put("TIO", new String[]{"1", "F8"});
            put("ADDR", new String[]{"2", "90"});
            put("CLEAR", new String[]{"2", "B4"});
            put("COMPR", new String[]{"2", "A0"});
            put("DIVR", new String[]{"2", "9C"});
            put("MULR", new String[]{"2", "98"});
            put("RMO", new String[]{"2", "AC"});
            put("SHIFTL", new String[]{"2", "A4"});
            put("SHIFTR", new String[]{"2", "A8"});
            put("SUBR", new String[]{"2", "94"});
            put("SVC", new String[]{"2", "B0"});
            put("TIXR", new String[]{"2", "B8"});
            put("ADD", new String[]{"3", "18"});
            put("ADDF", new String[]{"3", "58"});
            put("AND", new String[]{"3", "40"});
            put("COMP", new String[]{"3", "28"});
            put("COMPF", new String[]{"3", "88"});
            put("DIV", new String[]{"3", "24"});
            put("DIVF", new String[]{"3", "64"});
            put("J", new String[]{"3", "3C"});
            put("JEQ", new String[]{"3", "30"});
            put("JGT", new String[]{"3", "34"});
            put("JLT", new String[]{"3", "38"});
            put("JSUB", new String[]{"3", "48"});
            put("LDA", new String[]{"3", "00"});
            put("LDB", new String[]{"3", "68"});
            put("LDCH", new String[]{"3", "50"});
            put("LDF", new String[]{"3", "70"});
            put("LDL", new String[]{"3", "08"});
            put("LDS", new String[]{"3", "6C"});
            put("LDT", new String[]{"3", "74"});
            put("LDX", new String[]{"3", "04"});
            put("LPS", new String[]{"3", "D0"});
            put("MUL", new String[]{"3", "20"});
            put("MULF", new String[]{"3", "60"});
            put("OR", new String[]{"3", "44"});
            put("RD", new String[]{"3", "D8"});
            put("RSUB", new String[]{"3", "4C"});
            put("SSK", new String[]{"3", "EC"});
            put("STA", new String[]{"3", "0C"});
            put("STB", new String[]{"3", "78"});
            put("STCH", new String[]{"3", "54"});
            put("STF", new String[]{"3", "80"});
            put("STI", new String[]{"3", "D4"});
            put("STL", new String[]{"3", "14"});
            put("STS", new String[]{"3", "7C"});
            put("STSW", new String[]{"3", "E8"});
            put("STT", new String[]{"3", "84"});
            put("STX", new String[]{"3", "10"});
            put("SUB", new String[]{"3", "1C"});
            put("SUBF", new String[]{"3", "5C"});
            put("TD", new String[]{"3", "E0"});
            put("TIX", new String[]{"3", "2C"});
            put("WD", new String[]{"3", "DC"});

        }
    };

}
