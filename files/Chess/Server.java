

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JOptionPane;



public class Server implements ServerInterface {
	
	private static ClientInterface stubClient;
	private static Registry registry;
	
    public Server() {}
    

    /**
     * Register the server with the registry.
     */
	
    public void startServer() {
	
	try {

		
		System.setProperty("java.rmi.server.codebase", ServerInterface.class.getProtectionDomain().getCodeSource().getLocation().toString());

		
	    Server obj = new Server();
	    ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);
	    
	    registry = LocateRegistry.getRegistry();
	    
	    // Free registry (incase last app was terminated prematurely).
	    
			try {
				registry.unbind("ServerInterface");
			} catch (Exception e) {
				
				// Nothing to do. An exception WILL
				// be thrown in normal circumstances
				// and is perfectly normal.
			}
			
	    registry.bind("ServerInterface", stub);
	    
		System.err.println("Server ready");

		} catch (Exception e) {
			
			JOptionPane.showMessageDialog(null,  "Problem connecting. Make sure that registry is running." + '\n' + "See Console for details." ,"Connection Error",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			
			GameState.exit(false);
			
		}
	}


	

    /**
     * Establish connection (Client to Server). Tells server to show the chess board and to store the
     * Client stub (for callback)
     */
	
	public void EstablishConnection(ClientInterface client) {
		GameState.ClientFound = true;
		stubClient = client;
	}

		
		/** Tell the client to move the unit at Location original to Location new
		 * 
		 * @param pair
		 */
	
	public void TellClient(final Location oldLocation, final Location newLocation) {
	
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {


				try {
					
					
					
					stubClient.TellClient(oldLocation, newLocation);
					
					
				} catch (RemoteException e) {
					JOptionPane.showMessageDialog(null,  e.getLocalizedMessage() + "." + '\n' + "See Console for details." ,"Connection Error",JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					GameState.restartMessage();
				}

				
				
			}
		});		
		
		thread.start();
	}



	/** 
	 * Tell the server to move the unit at Location in to the Location out.
	 * 
	 */
	
	public void TellServer(Location oldLocation, Location newLocation) {
	
		Thread newMoveThread = new moveUnit(Board.getUnitAtLocation(oldLocation), newLocation);
		newMoveThread.start();
		
	}
	
	/** Remove the server from Registry.
	 *	 (preapare fro shutdown)
	 */
	
	public void EndConnection() {

		try {
						
			registry.unbind("ServerInterface");
						
		} catch (Exception e) {
		//	e.printStackTrace();			

		}

	}


	/**
	 * Called by client when leaving (prematurely)
	 */
	public void SayGoodByeToServer() throws RemoteException {
		
		
		JOptionPane.showMessageDialog(null,  "Client (Black) has left the game." ,"GoodBye",JOptionPane.INFORMATION_MESSAGE);
		EndConnection();
		GameState.restartMessage();
		
	}
	
	public void SayGoodByeToClient(){
		
		Thread thread = new Thread (new Runnable() {
			
			@Override
			public void run() {
				
				

				try {
				
					stubClient.SayGoodByeToClient();
					
				}
				catch (RemoteException e) {
					
				//	e.printStackTrace();
					
				}
				
				
				
			}
		});
		
		thread.start();
	}
	
	
	
}