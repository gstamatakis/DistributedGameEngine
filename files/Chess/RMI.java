

public class RMI {

	private static Server serverInstance;				// Instance of a Server
	private static Client clientInstance;				// Instance of a Client
	

	/** Start Up and Initialize RMI communication. 
	 *  Provide an IP-address if starting out as a Client.
	 * 
	 * @param ipaddress
	 */
	static void turnOnNetworking(String ipaddress) {
		
		if (GameState.server == true) {
			
			serverInstance = new Server();
			serverInstance.startServer();
		}
		
		else {
			
			clientInstance = new Client();
			clientInstance.register(ipaddress);
		}
		
	}

	/** Send Unit-Movement data over network using RMI
	 * 
	 * Data is send using a Locatoion-Pair ... the original Location of a unit and the new Location location
	 * to move the unit to.
	 * 
	 * The app at the other end of the network uses Board.getUnitAtLocation to find the unit to move (at oldLocation)
	 * and then moves that unit to the new location.
	 * 
	 * The only data transferred over the nework are two Location objects (int Row  int Column | int Row  int Column)
	 * 
	 * @param oldLocation
	 * @param newLocation
	 */
	
	static void broadcastMove(Location oldLocation, Location newLocation) {
		
		
		if (GameState.server == true) {
			
			serverInstance.TellClient(oldLocation, newLocation);
			
		}
		
		else {
			
			clientInstance.TellServer(oldLocation, newLocation);
		
		}
		
	}

	static void terminateConnection() {
		
		if (GameState.server == true) {
			
			serverInstance.SayGoodByeToClient();
			
		}
		
		else {
			
			clientInstance.SayGoodByeToServer();
		
		}
	}

	
	
		

	
	
}
