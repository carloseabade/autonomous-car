package autonomous_car;

import java.awt.Graphics;
import java.awt.HeadlessException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Observer;

public class Simulator extends JComponent{

	private static final long serialVersionUID = 1L;
	
	private JFrame window = new JFrame();
	
	Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension d = tk.getScreenSize();
	
	private int width = d.width;
	private int height = d.height;

	private Coordinate car = new Coordinate(0, 0); // Coordenada onde o agente está localizado.
	
	private static DatagramSocket server;
	
	private Simulator() throws HeadlessException {
		this.window.setTitle("Autonomous Car Simulator");
		this.window.setSize(width, height);
		this.window.setLocationRelativeTo(null);
		this.window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		this.window.add(this);
		this.window.setVisible(true);
	}
	
	
	protected void drawMotorway(Graphics g) {
		
		// Draw middle line
		g.drawLine(0, height/2, width, height/2);
		// Draw left line
		g.drawLine(0, height/2-60, width, height/2-60);
		// Draw right line
		g.drawLine(0, height/2+60, width, height/2+60);

		// Draw car
		BufferedImage img = null;
        try {
			img = ImageIO.read(new File("./res/img/autonomous-car.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    g.drawImage(img, 0, height/2, 132, 60+height/2, 0, 0, 1461, 666, null);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		
		
		super.paintComponent(g);
		drawMotorway(g);
		
	}

	
	/*
	 * O método readReceivedMessage realiza o processamente da mensagem.
	 * Por meio da interpretação da mensagem, é atualizado as variáveis do simulador para sua exibição gráfica.
	 * 
	 * A mensagem recebida é do tipo String, onde cada argumento da mensagem é separado por ponto-e-vírgula (;).
	 * O processamento da mensagem é feito da seguinte forma: 
	 * A mensagem é separada e tranformada em um array de String, messageArray, 
	 * onde a primeira posição identifica o tipo da mensagem, messageArray[0]. 
	 * Para cada tipo de mensagem, uma atualização diferente é realizada. A identificação de cada mensagem é feito por meio de um switch().
	 */
	private void readReceivedMessage(String message) {
		
		String[] messageArray = message.split(";");
		String switchMessage = messageArray[0];
		
		int x = 0,  y = 0;
		String d;
	
		
		if( !(switchMessage.equals("clear") || switchMessage.equals("removeObstacleDamage")) ) {
			x = Integer.parseInt( messageArray[1] );
			y = Integer.parseInt( messageArray[2] );
		}
	}
	
	
	public static void main(String args[]){
		
		Simulator sim = new Simulator();
		
		try {

			server = new DatagramSocket(9999);
			byte[] receive = new byte[1024];
			
			while(true)
			{
				DatagramPacket receivePacket = new DatagramPacket(receive, receive.length); // Pacote Recebido
				server.receive(receivePacket);
				
				String sentence = new String( receivePacket.getData() );
				sim.readReceivedMessage(sentence);
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
	 }
		
}


