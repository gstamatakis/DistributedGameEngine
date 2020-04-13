import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ClientInterface extends Remote {

	void TellClient(Location oldLocation, Location newLocation) throws RemoteException;
	 void SayGoodByeToClient() throws RemoteException;
	
}
