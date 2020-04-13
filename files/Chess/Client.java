
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JOptionPane;




public class Client implements ClientInterface {

    Client() {}
    
   
    
    private ServerInterface ServerStub;
    
     	
	public void register(String host) {

		try {

			
			Registry registry = LocateRegistry.getRegistry(host);
			ServerStub = (ServerInterface) registry.lookup("ServerInterface");

			Client obj = new Client();
		    ClientInterface ClientStub = (ClientInterface) UnicastRemoteObject.exportObject(obj, 0);
			
		    ServerStub.EstablishConnection(ClientStub);
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,  "Could not Connect. Make sure that you enter a valid IP-Address" + '\n' + "See Console for details." ,"Connection Error",JOptionPane.ERROR_MESSAGE);
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
			GameState.exit(false);
		}

	}    	
     	
  
    
	public void send(final Location oldLocation, final Location newLocation) {

		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {

				
				try {
					
					ServerStub.TellServer(oldLocation, newLocation);

				} catch (Exception e) {
					JOptionPane.showMessageDialog(null,  e.getLocalizedMessage() + "." + '\n' + "See Console for details." ,"Connection Error",JOptionPane.ERROR_MESSAGE);
					System.err.println("Client exception: " + e.toString());
					e.printStackTrace();
					GameState.restartMessage();
				}


				
				
			}
		});
		
		thread.start();
		
	}
    	
	
	public void TellClient(Location oldLocation, Location newLocation) {

		Thread newMoveThread = new moveUnit(Board.getUnitAtLocation(oldLocation), newLocation);
		newMoveThread.start();
		
	}
	
	public void TellServer(Location oldLocation, Location newLocation) {

		

		final Location oldLocation_fin = oldLocation;
		final Location newLocation_fin = newLocation;
		
		Thread moveThread = new Thread(new Runnable() {

			@Override
			public void run() {

				send(oldLocation_fin,newLocation_fin);

			}
		});

		moveThread.start();

	}



	/** Call when Server prematurel leaves the game.
	 * 
	 * 
	 * 
	 */
	public void SayGoodByeToServer() {
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {

				try {
					ServerStub.SayGoodByeToServer();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			
				
			}
		});
		
		thread.start();	
	}
	
	public void SayGoodByeToClient() throws RemoteException {
		
		JOptionPane.showMessageDialog(null,  "Server (White) has left the game." ,"GoodBye",JOptionPane.INFORMATION_MESSAGE);
		GameState.restartMessage();
		
		
		
	}
	
	

}

