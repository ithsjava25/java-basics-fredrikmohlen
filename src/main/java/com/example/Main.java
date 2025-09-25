package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


public class Main {
    public static void main(String[] args) {

        ElpriserAPI elpriserAPI = new ElpriserAPI();
         // Test- array för att se att inläsning fungerar.
        // args = new String[]{"--zone", "SE4", "--charging", "4h", "--date", "2025-09-24", "--help"};

        String prisklass = "";
        int chargingTime = 0 ;
        String date = "";
        boolean sort = false;
        LocalTime currentTime = LocalTime.now();

        // if för SE# eller switch-sats vi får se, för 'datum', för 'tidsintervall'
        // för 'sortering' priser fallande, störst överst och för 'help'
        //System.out.println(ElpriserAPI.Prisklass);

        if (args.length == 0) {
            printHelp();
        } else {
                    // byta till switch-sats??
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--zone")) {
                    prisklass = checkPrisklass(args[i + 1]);
                    System.out.println(prisklass);
                } else if (args[i].equals("--date")) { // en koll för att kolla rätt datumformatering
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    try {
                        LocalDate datum = LocalDate.parse(args[i+1]);
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date, use format YYYY-MM-DD");
                        return;
                    }
                    date = args[i + 1];
                    System.out.println(date);
                } else if (args[i].equals("--charging")) {
                    chargingTime = Integer.parseInt(args[i + 1].replaceAll("\\D", "")); // får endast ut siffran ur #h
                    System.out.println(chargingTime);
                } else if (args[i].equalsIgnoreCase("--help")) {
                    printHelp();
                } else if (args[i].equals("--sorted")) {
                    sort = true;
                    //todo: fixa '--sorted' för sortering?
                }
            }
        }
        if (date == null) {
            date = LocalDate.now().toString();
        }
        if (currentTime.isAfter(LocalTime.of(13, 0)));
            String dateNextDay = LocalDate.now().plusDays(1).toString();

        List<ElpriserAPI.Elpris> priser = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(prisklass));
//          ElpriserAPI.Elpris elpriser;
//        for (int i = 0; i < priser.size(); i++) {
//            System.out.println(priser.get(i));
//      }
        for (ElpriserAPI.Elpris elpriser : priser) {
            System.out.println(elpriser);
        }

    }

    public static String checkPrisklass(String prisklass) {
        //ElpriserAPI.Prisklass prisklass =
        switch (prisklass) {
            case "SE1" -> prisklass = ElpriserAPI.Prisklass.SE1.toString();
            case "SE2" -> prisklass = ElpriserAPI.Prisklass.SE2.toString();
            case "SE3" -> prisklass = ElpriserAPI.Prisklass.SE3.toString();
            case "SE4" -> prisklass = ElpriserAPI.Prisklass.SE4.toString();
            default -> throw new IllegalArgumentException();
        }
        return prisklass;


    }

    public static void printHelp() {
        System.out.println("""
                Expected commandline arguments:
                --zone SE1|SE2|SE3|SE4 (required)
                --date YYYY-MM-DD (optional, defaults to current date)
                --sorted (optional, to display prices in descending order)
                --charging 2h|4h|8h (optional, to find optimal charging windows)
                --help (optional, to display usage information)""");

    }
}

