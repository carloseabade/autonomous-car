package autonomous_car_2;

import java.awt.Graphics;
import java.awt.Toolkit;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import autonomous_car.Coordinate;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Simulator extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private JPanel window;
	
	private Toolkit tk = Toolkit.getDefaultToolkit();
	private Dimension d = tk.getScreenSize();
	
	private int width = d.width;
	private int height = d.height;

	private Coordinate car = new Coordinate(0, 0); // Coordenada onde o agente está localizado.
	private ArrayList<Coordinate> obstacles = new ArrayList<Coordinate>();
		
	private int velocity = 50;
	
	private static DatagramSocket server;
	
	private int fps = 1000 / 48;
	private int proportion = 1;
	
	private boolean animate = true;
	
	private Simulator() {
		
		window = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				g.setColor(Color.decode("#fdfdfd"));
				g.fillRect(0, 0, width, height);

				g.setColor(Color.decode("#dcdcdc"));
				
				// Draw middle line
				for(int i = 0; i < width+car.getX()*velocity; i += 110*proportion) {
					g.fillRect(i-car.getX()*velocity, height/2*proportion, 70*proportion, 2*proportion);
				}
				
				// Draw left line
				g.fillRect(0, height/2-105*proportion, width, 2*proportion);
				g.fillRect(0, height/2-120*proportion, width, 2*proportion);
				
				// Draw right line
				g.fillRect(0, height/2+105*proportion, width, 2*proportion);
				g.fillRect(0, height/2+120*proportion, width, 2*proportion);
				
			    // Draw sensor
				BufferedImage bi_sensor = null;
		        try {
		        	bi_sensor = ImageIO.read(new File("./res/img/sensor-all-sides.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			    g.drawImage(bi_sensor, 50-123*proportion, height/2-87-122*proportion + 110*car.getY(), 400*proportion, 312*proportion, null);

			    // Draw car
				BufferedImage bi_car = null;
		        try {
		        	bi_car = ImageIO.read(new File("./res/img/tesla.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			    g.drawImage(bi_car, 50*proportion, height/2-87*proportion + 110*car.getY(), 155*proportion, 70*proportion, null);
			    
			    // Draw obstacles
			    BufferedImage bi_stone = null;
		        try {
		        	bi_stone = ImageIO.read(new File("./res/img/passenger.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
		        for(Coordinate c : obstacles) {
		        	g.drawImage(bi_stone, (c.getX()*velocity)-(car.getX()*velocity), height/2-87*proportion + 110*c.getY(), 155*proportion, 70*proportion, null);
		        }
			}
		};
		
		getContentPane().add(window);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(width, height);
		setVisible(true);
		setTitle("Autonomous Car Simulator");
		setLocationRelativeTo(null);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		width = getWidth();
		height = getHeight();
		window.repaint();
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
		
		switch(switchMessage) {
			case	"carLocation":
				car.setX(Integer.parseInt(messageArray[1]));
				car.setY(Integer.parseInt(messageArray[2]));
				break;
			case	"obsLocation":
				obstacles.add(new Coordinate((Integer.parseInt(messageArray[1])), (Integer.parseInt(messageArray[2]))));
				break;
			default:
				System.out.println("Erro");
				System.out.println(messageArray[0]);
		}
	}
	
	public void startAnimation() {
		long nextUpdate = 0;
		
		try {

			server = new DatagramSocket(9999);
			byte[] receive = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receive, receive.length); // Pacote Recebido
			
			while(animate)
			{
				server.receive(receivePacket);
				
				String sentence = new String(receivePacket.getData());
				this.readReceivedMessage(sentence);
				
				if(System.currentTimeMillis() >= fps) {
					window.repaint();
					
					nextUpdate = System.currentTimeMillis() + fps;
				}
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void main(String args[]){
		Simulator sim = new Simulator();
		sim.startAnimation();
	 }
			
}


