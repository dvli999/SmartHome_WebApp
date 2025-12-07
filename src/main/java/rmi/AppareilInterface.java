package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
public interface AppareilInterface extends Remote {
    void eteindre() throws RemoteException;
}
