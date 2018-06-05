package autonomous_car_simulacao;

import java.net.*;

public class Client {

	/* Recebe um array String e retorna-o em formato String */
	public static String convertArray2String(String[] stringArray) {
		
		String stringConverted = "";
		
		for( String string : stringArray ) {
			stringConverted += string + ";";
		}
		
		return stringConverted;
	}
	
	/* Realiza a comunica��o com o servidor utilizando o protocolo UDP.
	   Recebe String message como par�metro, transforma seu valor em 
	   array de bytes e envia para o simulador.*/
	public static void sendMessage (String message) {
		try {
    		
		    	DatagramSocket client = new DatagramSocket();
		    	InetAddress IPAddress = InetAddress.getByName("localhost");
			byte[] sendData = new byte[1024];
			
			sendData = message.getBytes();
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9999);
			client.send(sendPacket);
			
			client.close();
        	}
        	catch (Exception e) {
    			System.out.println(e);
        	}
	}
	
	public static void sendMessage(String[] stringArray) {
	    sendMessage (convertArray2String (stringArray) );
	}
}
