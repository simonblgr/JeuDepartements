import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Jeu
{
    private final List<Departement> departements = new ArrayList<>();
    private final List<Departement> departementsATrouver = new ArrayList<>();
    private final List<Departement> departementsNonTrouves = new ArrayList<>();

    private int limite;

    void jouer() throws FileNotFoundException {

        System.out.println("JEU DEPARTEMENTS");
        System.out.println("But : Trouver le département depuis son numéro.");
        System.out.println("--------------------");
        System.out.println(" ");


        System.out.println("Jusqu'à quel numéro (inclus) aller ?");
        Scanner input = new Scanner(System.in);
        limite = Integer.parseInt(input.nextLine());


        chargerDepartements();
        lancerUneManche();
    }

    private void lancerUneManche() throws FileNotFoundException {
        if(departementsATrouver.isEmpty())
        {
            System.out.println("FINI !");
            int vScore = departements.size() - departementsNonTrouves.size();
            System.out.println("Score : " +  vScore + " / " + departements.size());

            if(vScore <  departements.size()) {
                System.out.println("Départements non trouvés du premier coup : ");
                for (Departement departementsNonTrouve : departementsNonTrouves) {
                    System.out.println(departementsNonTrouve.getNumero() + " - " + departementsNonTrouve.getNom());
                }
            }


            System.out.println("Recommencer (y/n) ?");
            Scanner input = new Scanner(System.in);
            if(input.next().equals("y"))
            {
                departements.clear();
                departementsATrouver.clear();
                departementsNonTrouves.clear();
                jouer();
            }
            else
            {
                System.exit(0);
            }
        }
        else {
            Departement vDepartement = getRandomDepartement();

            System.out.print(vDepartement.getNumero() + " : ");

            Scanner input = new Scanner(System.in);
            String valeurDonnee = input.nextLine();

            if (valeurDonnee.equals("stop")) {
                System.exit(0);
            } else {
                if (isProche(valeurDonnee, vDepartement)) {
                    System.out.println("OK ! " + vDepartement.getNom());
                    departementsATrouver.remove(vDepartement);
                } else {
                    System.out.println("Non, c'est : " + vDepartement.getNom());
                    if (!departementsNonTrouves.contains(vDepartement)) {
                        departementsNonTrouves.add(vDepartement);
                    }
                }
                System.out.println(" ");
                lancerUneManche();
            }
        }
    }

    private void chargerDepartements() throws FileNotFoundException
    {
        try (Scanner scanner = new Scanner(new File("data.csv")))
        {
            while (scanner.hasNextLine())
            {
                Departement vDep = getRecordFromLine(scanner.nextLine());

                //tant pis pour la corse
                if(vDep.getNumero().equals("2A") || vDep.getNumero().equals("2B") || Integer.parseInt(vDep.getNumero()) <= limite)
                {
                    departements.add(vDep);
                }
                else
                {
                    System.out.println("Ok , on ne fait pas le " + (limite + 1) + " qui est : " + vDep.getNom());
                    System.out.println(" ");
                    break;
                }
            }
        }
        departementsATrouver.addAll(departements);
    }

    private Departement getRecordFromLine(String line)
    {
        Departement vDep = new Departement();
        try (Scanner rowScanner = new Scanner(line))
        {
            rowScanner.useDelimiter(",");
            vDep.setNumero(rowScanner.next());
            vDep.setNom(rowScanner.next());
        }
        return vDep;
    }

    private Departement getRandomDepartement(){
        return departementsATrouver.get(new Random().nextInt(departementsATrouver.size()));
    }

    private boolean isProche(String aValeurDonnee, Departement aDepartement)
    {
        boolean vRet;

        //Parfaitement égal : OK
        if(aValeurDonnee.equalsIgnoreCase(aDepartement.getNom()))
        {
            vRet = true;
        }
        else
        {
            //Calcul de similarité avec la réponse
            double vSimilariteAvecReponse = similarity(aValeurDonnee.toLowerCase(), aDepartement.getNom().toLowerCase());
            //Inf à 0.6 : c'est pas ça du tout
            if(vSimilariteAvecReponse < 0.6)
            {
                vRet = false;
            }
            else
            {
                vRet = true;
                //On vérifie qu'un autre département ne soit pas plus proche que la réponse donnée
                for (Departement departement : departements)
                {
                    double vSimilarite = similarity(aValeurDonnee.toLowerCase(), departement.getNom().toLowerCase());
                    if(vSimilarite > vSimilariteAvecReponse)
                    {
                        vRet = false;
                        break;
                    }
                }
            }
        }
        return vRet;
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
        return (longerLength - distance(longer, shorter)) / (double) longerLength;
    }

    public static int distance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}