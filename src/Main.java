public class Main {
    public static void main(String[] args) {
        // ###########################################################
        // #   ___      _                                            #
        // #  / __|    (_)     __                                    #
        // #  \__ \    | |    / _|      Yehya Mohey Elashmawy        #
        // #  |___/   _|_|_   \__|_     211003968                    #
        // #_|"""""|_|"""""|_|"""""|                                 #
        // #"`-0-0-'"`-0-0-'"`-0-0-'                                 #
        // #    ___                     (_)                    _     #
        // #   | _ \    _ _    ___      | |    ___     __     | |_   #
        // #   |  _/   | '_|  / _ \    _/ |   / -_)   / _|    |  _|  #
        // #  _|_|_   _|_|_   \___/   |__/_   \___|   \__|_   _\__|  #
        // #_| """ |_|"""""|_|"""""|_|"""""|_|"""""|_|"""""|_|"""""| #
        // #"`-0-0-'"`-0-0-'"`-0-0-'"`-0-0-'"`-0-0-'"`-0-0-'"`-0-0-' #
        // ###########################################################
        SicTranslator sicCode = new SicTranslator("../../../src/"+args[0]);
        sicCode.passOne();
        sicCode.passTwo();
        sicCode.HTERecord();
    }
}
