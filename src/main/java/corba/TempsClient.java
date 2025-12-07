package corba;

import corba.SmartHome.Temps;
import corba.SmartHome.TempsHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class TempsClient {

    public static void main(String[] args) {
        try {
            // --- Initialisation de l'ORB ---
            ORB orb = ORB.init(new String[] {
                    "-ORBInitialPort", "1050",
                    "-ORBInitialHost", "127.0.0.1"
            }, null);

            // --- Récupération du NameService ---
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // --- Récupération de l'objet TempsImpl enregistré ---
            String name = "TempsService"; // Nom avec lequel le serveur s'est enregistré
            Temps temps = TempsHelper.narrow(ncRef.resolve_str(name));

            // --- Appel des méthodes ---
            int heure = temps.getHeure();
            int jour = temps.getJour();
            int semaine = temps.getWeekend();

            System.out.println("=== Informations dynamiques CORBA ===");
            System.out.println("Heure : " + heure);
            System.out.println("Jour : " + jour);
            System.out.println("Semaine : " + semaine);

            // --- Ici, tu peux envoyer ces données à ton module ML ---
            // Exemple (pseudo-code) :
            // double prediction = MLModule.predire(heure, jour, semaine);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
