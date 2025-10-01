package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDateTime;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class Main {
    public static void main(String[] args) {

        ElpriserAPI elpriserAPI = new ElpriserAPI();
        Locale.setDefault(Locale.of("sv", "se"));
        // Test- array för att se att inläsning fungerar.
        //args = new String[]{"--zone", "SE2", "--charging", "4h", "--date", "2025-09-24", "--help", "--sorted"};

        String prisklass = "";
        int chargingTime = 0;
        String date = "";
        boolean sorted = false;


        // if för SE# eller switch-sats vi får se, för 'datum', för 'tidsintervall'
        // för 'sortering' priser fallande, störst överst och för 'help'


        if (args.length == 0 || !args[0].equals("--zone")) {
            printHelp();
            return;
        } else {

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--zone")) {
                    prisklass = checkPrisklass(args[i + 1]);
                    if (prisklass.isEmpty()) {
                        System.out.println("Invalid zone");
                        return;
                    }
                } else if (args[i].equals("--date")) { // en koll för att kolla rätt datumformatering
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    try {
                        LocalDate datum = LocalDate.parse(args[i + 1]);
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date, use format YYYY-MM-DD");
                        return;
                    }
                    date = args[i + 1];

                } else if (args[i].equals("--charging")) {
                    chargingTime = Integer.parseInt(args[i + 1].replaceAll("\\D", "")); // får endast ut siffran ur #h

                    if (chargingTime != 2 && chargingTime != 4 && chargingTime != 8) {
                        System.out.println("Invalid charging time");
                        return;
                    }

                } else if (args[i].equalsIgnoreCase("--help")) {
                    printHelp();
                    return;
                } else if (args[i].equals("--sorted")) {
                    sorted = true;
                }
            }
        }
        // Kollar om det inte finns något datum i argumenten och sätter "date" till dagens datum
        if (date.isEmpty()) {
            date = LocalDate.now().toString();
            System.out.println("Using today's date " + date);
        }

        // Skapar en lista som heter 'priser' som hämtar data från Elpris ifrån klassen ElpriserAPI
        List<ElpriserAPI.Elpris> priser = elpriserAPI.getPriser(date, ElpriserAPI.Prisklass.valueOf(prisklass));
//          ElpriserAPI.Elpris elpriser;
        int counter = 0;
        for (int i = 0; i < priser.size(); i++) {
            // System.out.println(priser.get(i));
            counter++;
        }
        System.out.println("totala elprisers: " + counter);
//        for (ElpriserAPI.Elpris elpriser : priser) {
//            System.out.println(elpriser);
//        }
        // todo: calculate and display the mean price for the current 24h in the "elpris"
        double summa = 0.0;
        for (int i = 0; i < priser.size(); i++) {
            summa += priser.get(i).sekPerKWh();
        }// kollar om listan är tom då blir medelvärdet 0.0
        double medelpris = priser.isEmpty() ? 0.0 : summa / priser.size();
        if (medelpris < 1) {
            medelpris = medelpris * 100;
            System.out.printf("Medelpris: %.2f öre/kWh%n", medelpris);
        } else
            System.out.printf("Medelpris: %.2f kr/kwh%n", medelpris);

        if (priser == null || priser.isEmpty()) {
            System.out.println("Ingen data hittades för zon " + prisklass + " på datum " + date);
            return;
        } else {

            // todo: Identify cheapest and most expensive hour -> print
            //  - if two h same price, select earliest hour.
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH");
            ElpriserAPI.Elpris cheapestPrice = priser.getFirst();
            ElpriserAPI.Elpris mostExpensivePrice = priser.getFirst();
            for (int i = 1; i < priser.size(); i++) {
                ElpriserAPI.Elpris tempPrice = priser.get(i);
                if (tempPrice.sekPerKWh() < cheapestPrice.sekPerKWh())
                    cheapestPrice = tempPrice;
                if (tempPrice.sekPerKWh() > mostExpensivePrice.sekPerKWh())
                    mostExpensivePrice = tempPrice;
            }
            ;
            // tillfällig utskrift med '*100' för att få godkänt i testet
            System.out.printf("Lägsta pris: %.2f öre (%s)%n",
                    cheapestPrice.sekPerKWh() * 100,
                    cheapestPrice.timeStart().format(formatter) + "-" + cheapestPrice.timeEnd().format(formatter));
            System.out.printf("Högsta pris: %.2f öre (%s)%n",
                    mostExpensivePrice.sekPerKWh() * 100,
                    mostExpensivePrice.timeStart().format(formatter) + "-" + mostExpensivePrice.timeEnd().format(formatter));
        }
        // skriv ut sorterade priser, fallande + dagen efter om kl efter 13.00
        // Skapa en kopia av listan
        // todo: kolla om det finns en lista för
        //  morgondagen och lägga in den sorterad efter första
        //  listan, kolla övningar.
        //
        String dateNextDay = LocalDate.parse(date).plusDays(1).toString();
        if (sorted) {
            List<ElpriserAPI.Elpris> kopiaAvPriser = new ArrayList<>(priser);
            List<ElpriserAPI.Elpris> pricesOfTomorrow = elpriserAPI.getPriser(dateNextDay, ElpriserAPI.Prisklass.valueOf(prisklass));
            // Sortera kopian i fallande ordning + sortera morgondagens
            kopiaAvPriser.addAll(pricesOfTomorrow);
            kopiaAvPriser.sort((a, b) -> Double.compare(b.sekPerKWh(), a.sekPerKWh()));


            for (ElpriserAPI.Elpris pris : kopiaAvPriser) {
                System.out.printf("%s-%s %.2f öre \n",
                        pris.timeStart().format(DateTimeFormatter.ofPattern("HH")),
                        pris.timeEnd().format(DateTimeFormatter.ofPattern("HH")),
                        pris.sekPerKWh() * 100);
            }

        }
        // Hitta bästa laddningspris för 2h, 4h, och 8h (chargingTime)
        double minSumChargingPrice = 0.0;
        String billigasteStarttid = "";
        double averageChargingPrice = 0.0;

        List<ElpriserAPI.Elpris> kopiaAvPriser = new ArrayList<>(priser);
        List<ElpriserAPI.Elpris> priserImorgon = elpriserAPI.getPriser(dateNextDay, ElpriserAPI.Prisklass.valueOf(prisklass));

        kopiaAvPriser.addAll(priserImorgon);

        for (int i = 0; i < chargingTime; i++){
            minSumChargingPrice = minSumChargingPrice + kopiaAvPriser.get(i).sekPerKWh();
        }
            double windowSumChargingPrice = minSumChargingPrice;
            billigasteStarttid = kopiaAvPriser.get(0).timeStart().format(DateTimeFormatter.ofPattern("HH:mm"));
        if (priserImorgon.isEmpty()){
            for (int i = chargingTime; i < priser.size(); i++){
                windowSumChargingPrice = windowSumChargingPrice + (kopiaAvPriser.get(i).sekPerKWh() - kopiaAvPriser.get(i-chargingTime).sekPerKWh());

                if (windowSumChargingPrice < minSumChargingPrice){
                    minSumChargingPrice = windowSumChargingPrice;
                    billigasteStarttid = kopiaAvPriser.get(i+1-chargingTime).timeStart().format(DateTimeFormatter.ofPattern("HH:mm"));
                }
            }
        } else if (priserImorgon.size() < chargingTime-1) {
            for (int i = chargingTime; i < priser.size() + priserImorgon.size(); i++) {
                windowSumChargingPrice = windowSumChargingPrice + (kopiaAvPriser.get(i).sekPerKWh() - kopiaAvPriser.get(i - chargingTime).sekPerKWh());

                if (windowSumChargingPrice < minSumChargingPrice) {
                    minSumChargingPrice = windowSumChargingPrice;
                    billigasteStarttid = kopiaAvPriser.get(i - chargingTime + 1).timeStart().format(DateTimeFormatter.ofPattern("HH:mm"));
                }
            }
        }
            else{
            for (int i = chargingTime; i < priser.size()+chargingTime-1; i++){
                windowSumChargingPrice = windowSumChargingPrice + (kopiaAvPriser.get(i).sekPerKWh() - kopiaAvPriser.get(i-chargingTime).sekPerKWh());

                if (windowSumChargingPrice < minSumChargingPrice){
                    minSumChargingPrice = windowSumChargingPrice;
                    billigasteStarttid = kopiaAvPriser.get(i-chargingTime+1).timeStart().format(DateTimeFormatter.ofPattern("HH:mm"));
                }
            }
        }
        averageChargingPrice = (minSumChargingPrice/chargingTime) * 100;

        System.out.println("Påbörja laddning kl " + billigasteStarttid);
        System.out.println("Medelpris för fönster: " +  String.format("%.2f",averageChargingPrice) + " öre");
    }

    public static String checkPrisklass(String prisklass) {
        switch (prisklass.toUpperCase()) {
            case "SE1":
                return ElpriserAPI.Prisklass.SE1.toString();
            case "SE2":
                return ElpriserAPI.Prisklass.SE2.toString();
            case "SE3":
                return ElpriserAPI.Prisklass.SE3.toString();
            case "SE4":
                return ElpriserAPI.Prisklass.SE4.toString();
            default:
                return "";
        }
    }

    public static void printHelp() {
        System.out.println("""
                Expected commandline arguments:
                "Usage"
                --zone SE1|SE2|SE3|SE4 (required)
                --date YYYY-MM-DD (optional, defaults to current date)
                --sorted (optional, to display prices in descending order)
                --charging 2h|4h|8h (optional, to find optimal charging windows)
                --help (optional, to display usage information)""");

    }

    public static void findOptimalChargingHours(){


    }
}

