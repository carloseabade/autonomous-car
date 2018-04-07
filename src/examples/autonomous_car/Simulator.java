package autonomous_car;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
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

   private static DatagramSocket server;

   private int fps = 1000 / 48;
   private boolean animate = true;
   
   private byte lanesQuantity = 2;
   private byte obstaclesQuantity = 4;
   private byte carVelocity = 1;
   
//   5.03682 m = 155 px
//   	   1 m = 30.77338479437423 px ~ 31 px
   private double pixelsPerMeter = 30.77338479437423;
   
   private Simulator() {
       window = new JPanel() {
           @Override
           public void paintComponent(Graphics g) {
        	   g.setColor(Color.decode("#fdfdfd"));
               g.fillRect(0, 0, width, height);

               g.setColor(Color.decode("#dcdcdc"));
               g.setColor(Color.decode("#dcdcdc"));

               // Draw middle line
               for(int i = 0; i < width+car.getX()*pixelsPerMeter*carVelocity; i += 110) {
                   g.fillRect((int) (i-car.getX()*pixelsPerMeter*carVelocity), height/2, 70, 2);
               }
               
               if(lanesQuantity > 2) {
            	   //faz a terceira pista
                   g.fillRect(0, (int) (height/2-(3.6*pixelsPerMeter)), width, 2);
                   g.fillRect(0, (int) (height/2-(3.6*pixelsPerMeter)), width, 2);
                   g.fillRect(0, (int) (height/2-(3.8*pixelsPerMeter)), width, 2);
                   g.fillRect(0, (int) (height/2-(3.8*pixelsPerMeter)), width, 2);
               }
               if(lanesQuantity > 3) {
            	   //faz a quarta pista
                   for(int i = 0; i < width+car.getX()*pixelsPerMeter*carVelocity; i += 110) {
                       g.fillRect((int) (i-car.getX()*pixelsPerMeter*carVelocity), (int) (height/2-(3.8*2*pixelsPerMeter)), 70, 2);
                   }
               }

               switch(lanesQuantity) {
               case 2:
                   // Draw left line
                   g.fillRect(0, (int) (height/2-(3.6*pixelsPerMeter)), width, 2);
                   g.fillRect(0, (int) (height/2-(3.6*pixelsPerMeter+0.5*pixelsPerMeter)), width, 2);
                   break;
               case 3:
                   // Draw left line
                   g.fillRect(0, (int) (height/2-(3.8*2*pixelsPerMeter)), width, 2);
                   g.fillRect(0, (int) (height/2-(3.8*2*pixelsPerMeter+0.5*pixelsPerMeter)), width, 2);
                   break;
               case 4:
                   // Draw left line
                   g.fillRect(0, (int) (height/2-(3.8*3*pixelsPerMeter)), width, 2);
                   g.fillRect(0, (int) (height/2-(3.8*3*pixelsPerMeter+0.5*pixelsPerMeter)), width, 2);
                   break;
               }

               // Draw right line
               g.fillRect(0, (int) (height/2+(3.6*pixelsPerMeter)), width, 2);
               g.fillRect(0, (int) (height/2+(3.6*pixelsPerMeter+0.5*pixelsPerMeter)), width, 2);

               // Draw car
               BufferedImage bi_car = null;
               try {
            	   bi_car = ImageIO.read(new File("./res/img/tesla.png"));
               } catch (IOException e) {
                   e.printStackTrace();
               }
               g.drawImage(bi_car, 1, (int) (height/2-((0.7+2.2-((1.4+2.2)*car.getY()))*pixelsPerMeter)), 155, 70, null);

               // Draw sensor
               BufferedImage bi_sensor = null;
               try {
                   bi_sensor = ImageIO.read(new File("./res/img/sensor-all-sides.png"));
               } catch (IOException e) {
                   e.printStackTrace();
               }
               g.drawImage(bi_sensor, (int) (1-(4.0*pixelsPerMeter)), (int) (height/2-((0.7+2.2-((1.4+2.2)*car.getY()))*pixelsPerMeter)-(4.0*pixelsPerMeter)), 403, 318, null);

               // Draw obstacles
               BufferedImage bi_stone = null;
               try {
                   bi_stone = ImageIO.read(new File("./res/img/passenger.png"));
               } catch (IOException e) {
                   e.printStackTrace();
               }
               for(Coordinate c : obstacles) {
                   g.drawImage(bi_stone, (int) ((c.getX()-car.getX())*pixelsPerMeter), (int) (height/2-((0.7+2.2-((1.4+2.2)*c.getY()))*pixelsPerMeter)), 155, 70, null);
               }
           }
       };
       simulatorSettings = new JPanel();
       simulatorSettings.setBorder(BorderFactory.createTitledBorder("Simulator Configuration:"));
       simulatorSettings.setLayout(new GridLayout(2,0));
       
       jL_lanesQuantity = new JLabel("Lanes quantity:");
       jS_lanesQuantity = new JSlider();
       jTF_lanesQuantity = new JTextField();
       jS_lanesQuantity.setMaximum(4);
       jS_lanesQuantity.setValue(lanesQuantity);
       jS_lanesQuantity.setMinimum(2);
       jS_lanesQuantity.setMajorTickSpacing(1);
       jS_lanesQuantity.setPaintTicks(true);
       jS_lanesQuantity.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
	    	       jTF_lanesQuantity.setText(String.valueOf(jS_lanesQuantity.getValue())+" lanes");
			}
		});
       jTF_lanesQuantity.setEditable(false);
       jTF_lanesQuantity.setText(String.valueOf(jS_lanesQuantity.getValue())+" lanes");
       simulatorSettings.add(jL_lanesQuantity);
       simulatorSettings.add(jS_lanesQuantity);
       simulatorSettings.add(jTF_lanesQuantity);
       
       jL_obstaclesQuantity = new JLabel("Obstacles quantity:");
       jS_obstaclesQuantity = new JSlider();
       jTF_obstaclesQuantity = new JTextField();
       jS_obstaclesQuantity.setMaximum(50);
       jS_obstaclesQuantity.setValue(obstaclesQuantity);
       jS_obstaclesQuantity.setMinimum(0);
       jS_obstaclesQuantity.setMajorTickSpacing(1);
       jS_obstaclesQuantity.setPaintTicks(true);
       jS_obstaclesQuantity.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
	    	       jTF_obstaclesQuantity.setText(String.valueOf(jS_obstaclesQuantity.getValue())+" obstacles");
			}
		});
       jTF_obstaclesQuantity.setEditable(false);
       jTF_obstaclesQuantity.setText(String.valueOf(jS_obstaclesQuantity.getValue())+" obstacles");
       simulatorSettings.add(jL_obstaclesQuantity);
       simulatorSettings.add(jS_obstaclesQuantity);
       simulatorSettings.add(jTF_obstaclesQuantity);
              
       jL_autoSteerTechnique = new JLabel("Auto steer technique:");
       jCB_autoSteerTechnique = new JComboBox<String>(new DefaultComboBoxModel<>(new String[] {"Tecnique 1","Tecnique 2","Tecnique 3"}));
       simulatorSettings.add(jL_autoSteerTechnique);
       simulatorSettings.add(jCB_autoSteerTechnique);
       
       jB_apply = new JButton("Apply");
       jB_apply.addActionListener(new ActionListener() {
    	   @Override
    	   public void actionPerformed(ActionEvent actionEvent) {
    		   lanesQuantity = (byte) jS_lanesQuantity.getValue();
    		   obstaclesQuantity = (byte) jS_obstaclesQuantity.getValue();
    		   window.repaint();
    	   }
       });
       simulatorSettings.add(jB_apply);

       window.setLayout(new BorderLayout());
       window.add(simulatorSettings, BorderLayout.SOUTH);

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
           case    "carLocation":
               car.setX(Integer.parseInt(messageArray[1]));
               car.setY(Integer.parseInt(messageArray[2]));
               break;
           case    "obsLocation":
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

           while(animate) {
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
       
   private JPanel simulatorSettings;
   private JLabel jL_lanesQuantity;
   private JSlider jS_lanesQuantity;
   private JTextField jTF_lanesQuantity;
   private JLabel jL_obstaclesQuantity;
   private JSlider jS_obstaclesQuantity;
   private JTextField jTF_obstaclesQuantity;
   private JLabel jL_autoSteerTechnique;
   private JComboBox<String> jCB_autoSteerTechnique;
   private JButton jB_apply;
   
   private JMenu jM_File;
   private JMenu jM_Edit;
   private JMenuBar jMB_menuBar;
}