package rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class AppareilImpl extends UnicastRemoteObject implements AppareilInterface {

    public AppareilImpl() throws RemoteException {
        super();
    }

    @Override
    public void eteindre() throws RemoteException {
        System.out.println("RMI: Appareil éteint !");
    }

    public static void main(String[] args) {
        try {
            // Try to get existing registry, else create one
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(1100);
                registry.list(); // test if registry is alive
                System.out.println("RMI registry déjà existante sur le port 1100");
            } catch (RemoteException e) {
                registry = LocateRegistry.createRegistry(1100);
                System.out.println("Nouvelle RMI registry créée sur le port 1100");
            }

            // Create server object
            AppareilImpl impl = new AppareilImpl();

            // Bind the object to the registry
            try {
                registry.bind("AppareilService", impl);
            } catch (AlreadyBoundException ex) {
                // If already bound, just rebind
                registry.rebind("AppareilService", impl);
            }

            System.out.println("Serveur RMI prêt sur AppareilService.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
