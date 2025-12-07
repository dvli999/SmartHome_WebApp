package corba;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

public class TempsImpl extends corba.SmartHome.TempsPOA {

    @Override
    public int getHeure() {
        return LocalDateTime.now().getHour();
    }

    @Override
    public int getJour() {
        // jour de la semaine 1=Monday ... 7=Sunday
        return LocalDateTime.now().getDayOfWeek().getValue();
    }

    @Override
    public int getWeekend() {
        int d = LocalDateTime.now().getDayOfWeek().getValue();
        return (d >= 6) ? 1 : 0;
    }

    // méthode utilitaire qui appelle Python et retourne la sortie
    public String appelerML(int heure, int jour, int weekend) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python", "ml/predict_ml.py",
                    String.valueOf(heure),
                    String.valueOf(jour),
                    String.valueOf(weekend)
            );
            pb.directory(new File(".")); // racine du projet
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String out = br.readLine();
            p.waitFor();
            return out; // ex: "78.345|ELEVEE"
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    // main pour publier l'objet CORBA
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(new String[] {
                    "-ORBInitialPort", "1050",
                    "-ORBInitialHost", "127.0.0.1"
            }, null);

            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            TempsImpl tempsServant = new TempsImpl();
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(tempsServant);

            // bind dans le NameService (orbd / tnameserv doit être lancé)
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            org.omg.CosNaming.NamingContextExt ncRef = org.omg.CosNaming.NamingContextExtHelper.narrow(objRef);
            ncRef.rebind(ncRef.to_name("TempsService"), ref);

            System.out.println("Serveur CORBA Temps prêt.");
            orb.run();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
