import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
  
    void EstablishConnection(ClientInterface client) throws RemoteException;
    void TellServer(Location oldLocation, Location newLocation) throws RemoteException;
    void SayGoodByeToServer() throws RemoteException;
}
