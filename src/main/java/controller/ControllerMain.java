package controller;

import corba.SmartHome.Temps;
import corba.SmartHome.TempsHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.rmi.Naming;

public class ControllerMain {
    public static void main(String[] args) {
        try {
            // init ORB
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            Temps temps = TempsHelper.narrow(ncRef.resolve_str("TempsService"));

            int heure = temps.getHeure();
            int jour = temps.getJour();
            int weekend = temps.getWeekend();

            System.out.println("CORBA renvoit: heure=" + heure + " jour=" + jour + " weekend=" + weekend);

            // Appeler la méthode qui appelle Python dans TempsImpl
            // Si tu as exposé une méthode analyser() dans le servant, utilise-la.
            // Ici on suppose TempsImpl a une operation analyserConsommation (non standard) :
            // String out = temps.analyserConsommation(heure, jour, weekend);
            // Simpler: call predict via separate Process (optionnel)

            // Ici je montre appel RMI & JMS quand prédiction ELEVEE
            // Exemple simulation: si heure >= 18 => ELEVEE (ou parse result)
            boolean elev = (heure >= 18);

            if (elev) {
                // RMI call
                rmi.AppareilInterface appareil = (rmi.AppareilInterface) Naming.lookup("rmi://localhost/AppareilService");
                appareil.eteindre();

                // JMS alert
                jms.NotificationJMS.envoyer("Alerte : consommation élevée détectée (" + heure + ")");
            } else {
                System.out.println("Consommation normale.");
            }

        } catch (Exception e) { e.printStackTrace(); }
    }
}
