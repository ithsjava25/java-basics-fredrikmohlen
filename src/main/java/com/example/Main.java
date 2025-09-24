package com.example;

import com.example.api.ElpriserAPI;

import java.util.List;


public class Main {
    public static void main(String[] args) {

        ElpriserAPI elpriserAPI = new ElpriserAPI();

        args = new String[]{"--zone", "SE4", "--charging", "4h", "--date", "2025-09-24", "--help"};

        String prisKlass ="";
        int time;
        String date = "";
        boolean sort = false;
        // if för SE# eller switch-sats vi får se, för 'datum', för 'tidsintervall'
        // för 'sortering' priser fallande, störst överst och för 'help'
        //System.out.println(ElpriserAPI.Prisklass);

        if (args.length <= 0) {
            args[0] = "--help";
        } else {

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--zone")) {
                    prisKlass = checkPrisklass(args[i + 1]);
                    System.out.println(prisKlass);
                } else if (args[i].equals("--date")) {
                    date = args[i + 1];
                    System.out.println(date);
                } else if (args[i].equals("--charging")) {
                    time = Integer.parseInt(args[i + 1].replaceAll("\\D", "")); // får endast ut siffran ur #h
                    System.out.println(time);
                } else if (args[i].equalsIgnoreCase("--help")) {
                    System.out.println("""
                            Expected commandline arguments:
                            --zone SE1|SE2|SE3|SE4 (required)
                            --date YYYY-MM-DD (optional, defaults to current date)
                            --sorted (optional, to display prices in descending order)
                            --charging 2h|4h|8h (optional, to find optimal charging windows)
                            --help (optional, to display usage information)""");

                } else if (args[i].equals("--sorted")) {
                    sort = true;
                    //todo: fixa '--sorted' för sortering?
                }
            }
        }
    List<ElpriserAPI.Elpris> priser = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(prisKlass));
        // ElpriserAPI.Elpris elpriser;
//        for (int i = 0; i < priser.size(); i++) {
//            System.out.println(priser.get(i));
//        }
//        for (ElpriserAPI.Elpris elpriser : priser) {
//            System.out.println(elpriser);
//        }

    }
    public static String checkPrisklass(String prisklass) {
        switch (prisklass){
            case "SE1" ->  prisklass = ElpriserAPI.Prisklass.SE1.toString();
            case "SE2" ->  prisklass = ElpriserAPI.Prisklass.SE2.toString();
            case "SE3" ->  prisklass = ElpriserAPI.Prisklass.SE3.toString();
            case "SE4" ->  prisklass = ElpriserAPI.Prisklass.SE4.toString();
            default -> throw new IllegalArgumentException();
        }
        return prisklass;


        }

        public static void showHelp(){
            System.out.println("""
                            Expected commandline arguments:
                            --zone SE1|SE2|SE3|SE4 (required)
                            --date YYYY-MM-DD (optional, defaults to current date)
                            --sorted (optional, to display prices in descending order)
                            --charging 2h|4h|8h (optional, to find optimal charging windows)
                            --help (optional, to display usage information)""");

        }


