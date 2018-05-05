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

import autonomous_car_2.Car;
import autonomous_car_2.Coordinate;
import autonomous_car_2.Obstacle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class Simulator extends JFrame{

   private static final long serialVersionUID = 1L;

   private JPanel window;

   private Toolkit tk = Toolkit.getDefaultToolkit();
   private Dimension d = tk.getScreenSize();

   private int width = d.width;
   private int height = d.height;

   private Coordinate car = new Car(5, 2, 1, 0, 0); // Coordenada onde o agente está localizado.
   private ArrayList<Coordinate> obstacles = new ArrayList<Coordinate>();

   private static DatagramSocket server;
   
   private int fps = 1000 / 24;
   private boolean animate = false;
   private byte zoom = 2;
   private boolean run = true;
   
   private byte lanesQuantity = 2;
   private byte obstaclesQuantity = 4;
   private byte carVelocity = 1		;
   
//   5.03682 m = 155 px
//   	   1 m = 30.77338479437423 px ~ 31 px
   private double pixelsPerMeter = 30.77338479437423;
   
   Simulator() {
       window = new JPanel() {
           @Override
           public void paintComponent(Graphics g) {
        	   g.setColor(Color.decode("#fdfdfd"));
               g.fillRect(0, 0, width, height);

               g.setColor(Color.decode("#dcdcdc"));

               //Draw lane lines
               g.fillRect(0, 0, width, 1*zoom);

               for(int i = 0; i < width+car.getX()*zoom; i += 36*zoom) {
                   g.fillRect(i-car.getX()*zoom, 36*zoom, 23*zoom, 1*zoom);
               }
               
               g.fillRect(0, 74*zoom, width, 1*zoom);
               
               for(int i = 0; i < width+car.getX()*zoom; i += 36*zoom) {
                   g.fillRect(i-car.getX()*zoom, 110*zoom, 23*zoom, 1*zoom);
               }
               
               g.fillRect(0, 146*zoom, width, 1*zoom);

               // Draw car
               BufferedImage bi_car = null;
               try {
            	   bi_car = ImageIO.read(new File("./res/img/tesla.png"));
               } catch (IOException e) {
                   e.printStackTrace();
               }
               g.drawImage(bi_car, 0, (car.getY())*zoom, 50*zoom, 23*zoom, null);
               
               g.setColor(Color.decode("#000"));
               g.drawString(String.valueOf(car.getY()), 20, 20);
               
               // Draw sensor
               BufferedImage bi_sensor = null;
               try {
                   bi_sensor = ImageIO.read(new File("./res/img/sensor-all-sides.png"));
               } catch (IOException e) {
                   e.printStackTrace();
               }
               g.drawImage(bi_sensor, -40*zoom, (car.getY()-40)*zoom, 130*zoom, 103*zoom, null);

               // Draw obstacles
               BufferedImage bi_stone = null;
               try {
                   bi_stone = ImageIO.read(new File("./res/img/passenger.png"));
               } catch (IOException e) {
                   e.printStackTrace();
               }
               for(Coordinate c : obstacles) {
                   g.drawImage(bi_car, (c.getX()-car.getX())*zoom, (c.getY())*zoom, 50*zoom, 23*zoom, null);
                   g.drawString(String.valueOf(c.getX()), 20+obstacles.indexOf(c)+10, 20);
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
       jS_lanesQuantity.setMinimum(1);
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
              
       jL_zoom = new JLabel("Zoom:");
       jS_zoom = new JSlider();
       jTF_zoom = new JTextField();
       jS_zoom.setMaximum(10);
       jS_zoom.setValue(zoom);
       jS_zoom.setMinimum(1);
       jS_zoom.setMajorTickSpacing(1);
       jS_zoom.setPaintTicks(true);
       jS_zoom.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
	    	       jTF_zoom.setText(String.valueOf(jS_zoom.getValue())+" proportion");
	    		   zoom = (byte) jS_zoom.getValue();
	    		   window.repaint();
			}
		});
       jTF_zoom.setEditable(false);
       jTF_zoom.setText(String.valueOf(jS_zoom.getValue())+" proportion");
       simulatorSettings.add(jL_zoom);
       simulatorSettings.add(jS_zoom);
       simulatorSettings.add(jTF_zoom);

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

       jB_start = new JButton("Start");
       jB_start.addActionListener(new ActionListener() {
    	   @Override
    	   public void actionPerformed(ActionEvent actionEvent) {
    		   animate = true;
    	   }
       });
       simulatorSettings.add(jB_start);

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
          obstacles.add(new Obstacle(5, 2, (Integer.parseInt(messageArray[1])), (Integer.parseInt(messageArray[2]))));
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

              if(System.currentTimeMillis() >= nextUpdate) {
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
   
   public byte getObstaclesQuantity() {
	   return obstaclesQuantity;
   }
   
   public boolean getAnimate() {
	   return animate;
   }
   
   public void setObstacles(ArrayList<Coordinate> obstacles2) {
	   this.obstacles = obstacles2;
   }
   
   public void setCarLocation(Coordinate car) {
	   this.car = car;
   }
       
   private JPanel simulatorSettings;
   private JLabel jL_lanesQuantity;
   private JSlider jS_lanesQuantity;
   private JTextField jTF_lanesQuantity;
   private JLabel jL_obstaclesQuantity;
   private JSlider jS_obstaclesQuantity;
   private JTextField jTF_obstaclesQuantity;
   private JLabel jL_zoom;
   private JSlider jS_zoom;
   private JTextField jTF_zoom;
   private JLabel jL_autoSteerTechnique;
   private JComboBox<String> jCB_autoSteerTechnique;
   private JButton jB_apply;
   private JButton jB_start;
   
   private JMenu jM_File;
   private JMenu jM_Edit;
   private JMenuBar jMB_menuBar;
}