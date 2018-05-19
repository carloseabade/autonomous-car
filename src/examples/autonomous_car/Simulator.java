package autonomous_car;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

   private Car car = new Car(50, 23, 1, 0, 0); // Coordenada onde o agente está localizado.
   private ArrayList<Obstacle> obstacles = new ArrayList<>();
   private ArrayList<Crosswalk> crosswalks = new ArrayList<>();
   TrafficLight trafficLight = new TrafficLight(17, 67, 1000, 86);
   
   private static DatagramSocket server;

   private int fps = 1000 / 24;
   private boolean animate = true;
   private boolean notStart = true;
   private byte zoom = 1;
   
   private byte lanesQuantity = 2;
   private byte obstaclesQuantity = 4;
   
   Simulator() {
	   window = new JPanel();
       window.setLayout(new BorderLayout());
       
       JPanel drawing = new JPanel() {
           @Override
           public void paintComponent(Graphics g) {
        	   g.setColor(Color.decode("#fdfdfd"));
               g.fillRect(0, 0, width, height);

               g.setColor(Color.decode("#dcdcdc"));

//               //Draw lane lines
//               g.fillRect(0, 0, width, 1*zoom);
//
//               for(int i = 0; i < width+car.getX()*zoom; i += 36*zoom) {
////                   g.fillRect(i-car.getX()*zoom, 36*zoom, 23*zoom, 1*zoom);
//            	   g.fillRect(i*zoom, 36*zoom, 23*zoom, 1*zoom);
//               }
               
               g.fillRect(0, 74*zoom, width, 1*zoom);
               
               for(int i = 0; i < width+car.getX()*zoom; i += 36*zoom) {
//                   g.fillRect(i-car.getX()*zoom, 110*zoom, 23*zoom, 1*zoom);
            	   g.fillRect(i*zoom, 110*zoom, 23*zoom, 1*zoom);
               }
               
               g.fillRect(0, 146*zoom, width, 1*zoom);
               
               
               // Draw crosswalk
        	   BufferedImage bi_crosswalk = null;
        	   try {
        		   bi_crosswalk = ImageIO.read(new File("./res/img/crosswalk.png"));
        	   } catch (IOException e) {
        		   e.printStackTrace();
        	   }
        	   // Draw pedestrian
        	   BufferedImage bi_pedestrian = null;
        	   try {
        		   bi_pedestrian = ImageIO.read(new File("./res/img/pedestrian.png"));
        	   } catch (IOException e) {
        		   e.printStackTrace();
        	   }
               for(Crosswalk c : crosswalks) {
//            	   g.drawImage(bi_crosswalk, (c.getX()-car.getX())*zoom, c.getY()*zoom, 36*zoom, 72*zoom, null);
            	   g.drawImage(bi_crosswalk, (c.getX())*zoom, c.getY()*zoom, 36*zoom, 72*zoom, null);
            	   if(c.hasPedestrian()) {
//                	   g.drawImage(bi_pedestrian, (c.getPedestrian().getX()-car.getX())*zoom, c.getPedestrian().getY()*zoom, 9*zoom, 5*zoom, null);
            		   g.drawImage(bi_pedestrian, (c.getPedestrian().getX())*zoom, c.getPedestrian().getY()*zoom, 9*zoom, 5*zoom, null);
            	   }
               }

               // Draw car
               if(car.getY() != 0) {
            	   BufferedImage bi_car = null;
            	   try {
            		   bi_car = ImageIO.read(new File("./res/img/tesla.png"));
            	   } catch (IOException e) {
            		   e.printStackTrace();
            	   }
            	   g.drawImage(bi_car, 0, (car.getY())*zoom, 50*zoom, 23*zoom, null);

            	   g.setColor(Color.decode("#ff0000"));
            	   g.drawString(String.valueOf(car.getX()), 20, 20);

            	   // Draw sensor
            	   BufferedImage bi_sensor = null;
            	   try {
            		   bi_sensor = ImageIO.read(new File("./res/img/sensor-all-sides.png"));
            	   } catch (IOException e) {
            		   e.printStackTrace();
            	   }
            	   g.drawImage(bi_sensor, -40*zoom, (car.getY()-40)*zoom, 130*zoom, 103*zoom, null);

            	   // Draw sensor wide
            	   BufferedImage bi_sensor_wide = null;
            	   try {
            		   bi_sensor_wide = ImageIO.read(new File("./res/img/sensor-wide.png"));
            	   } catch (IOException e) {
            		   e.printStackTrace();
            	   }
            	   g.drawImage(bi_sensor_wide, 30*zoom, (car.getY()-519)*zoom, 600*zoom, 1060*zoom, null);
            	   
                   // Draw traffic light
                   BufferedImage bi_traffic_light = null;
                   try {
                	   if(trafficLight.isGreen()) {
                    	   bi_traffic_light = ImageIO.read(new File("./res/img/traffic-light-green.png"));
                	   } else if(trafficLight.isYellow()) {
                    	   bi_traffic_light = ImageIO.read(new File("./res/img/traffic-light-yellow.png"));
                	   } else if(trafficLight.isRed()) {
                    	   bi_traffic_light = ImageIO.read(new File("./res/img/traffic-light-red.png"));
                	   }
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
//                   g.drawImage(bi_traffic_light, (trafficLight.getX()-car.getX())*zoom, (trafficLight.getY())*zoom, 17*zoom, 67*zoom, null);
                   g.drawImage(bi_traffic_light, (trafficLight.getX())*zoom, (trafficLight.getY())*zoom, 17*zoom, 67*zoom, null);
               }
               
               // Draw obstacles
               BufferedImage bi_stone = null;
               try {
                   bi_stone = ImageIO.read(new File("./res/img/passenger.png"));
               } catch (IOException e) {
                   e.printStackTrace();
               }
               for(Obstacle c : obstacles) {
//                   g.drawImage(bi_stone, (c.getX()-car.getX())*zoom, (c.getY())*zoom, 50*zoom, 23*zoom, null);
            	   g.drawImage(bi_stone, (c.getX())*zoom, (c.getY())*zoom, 50*zoom, 23*zoom, null);
               }
           }
       };
       simulatorSettings = new JPanel();
       simulatorSettings.setBorder(BorderFactory.createTitledBorder("Simulator Configuration:"));
       simulatorSettings.setLayout(new BoxLayout(simulatorSettings, BoxLayout.PAGE_AXIS));
       
       jL_lanesQuantity = new JLabel("Lanes quantity:");
       jS_lanesQuantity = new JSlider();
       jS_lanesQuantity.setPaintLabels(true);
       jS_lanesQuantity.setMaximum(4);
       jS_lanesQuantity.setValue(getLanesQuantity());
       jS_lanesQuantity.setMinimum(2);
       jS_lanesQuantity.setMajorTickSpacing(1);
       jS_lanesQuantity.setPaintTicks(true);
       simulatorSettings.add(jL_lanesQuantity);
       simulatorSettings.add(jS_lanesQuantity);

       jL_obstaclesQuantity = new JLabel("Obstacles quantity:");
       jS_obstaclesQuantity = new JSlider();
//       jS_obstaclesQuantity.setPaintLabels(true);
       jS_obstaclesQuantity.setMaximum(50);
       jS_obstaclesQuantity.setValue(getObstaclesQuantity());
       jS_obstaclesQuantity.setMinimum(0);
       jS_obstaclesQuantity.setMajorTickSpacing(1);
       jS_obstaclesQuantity.setPaintTicks(true);
       simulatorSettings.add(jL_obstaclesQuantity);
       simulatorSettings.add(jS_obstaclesQuantity);
              
       jL_zoom = new JLabel("Zoom:");
       jS_zoom = new JSlider();
       jS_zoom.setPaintLabels(true);
       jS_zoom.setMaximum(5);
       jS_zoom.setValue(zoom);
       jS_zoom.setMinimum(1);
       jS_zoom.setMajorTickSpacing(1);
       jS_zoom.setPaintTicks(true);
       jS_zoom.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
	    		   zoom = (byte) jS_zoom.getValue();
	    		   window.repaint();
			}
		});
       simulatorSettings.add(jL_zoom);
       simulatorSettings.add(jS_zoom);

       jB_apply = new JButton("Apply");
       jB_apply.addActionListener(new ActionListener() {
    	   @Override
    	   public void actionPerformed(ActionEvent actionEvent) {
    		   setLanesQuantity((byte) jS_lanesQuantity.getValue());
    		   setObstaclesQuantity((byte) jS_obstaclesQuantity.getValue());
    		   window.repaint();
    	   }
       });
       simulatorSettings.add(jB_apply);

       jB_start = new JButton("Start");
       jB_start.addActionListener(new ActionListener() {
    	   @Override
    	   public void actionPerformed(ActionEvent actionEvent) {
    		   notStart = false;
    	   }
       });
       simulatorSettings.add(jB_start);
       
       add(window);
       
       window.add(drawing, BorderLayout.CENTER);
       window.add(simulatorSettings, BorderLayout.EAST);

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
   
   private void readReceivedMessage(String message) {

	   String[] messageArray = message.split(";");
	   String switchMessage = messageArray[0];
	
	   switch(switchMessage) {
	       case    "carLocation":
	           car.setX(Integer.parseInt(messageArray[1]));
	           car.setY(Integer.parseInt(messageArray[2]));
	           break;
//	       case    "obsLocation":
//	           obstacles.add(new Obstacle(5, 2, (Integer.parseInt(messageArray[1])), (Integer.parseInt(messageArray[2]))));
//	           break;
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
//            	   this.crosswalks.get(0).getPedestrian().check_pedestrian();
            	   if(this.crosswalks.get(0).hasPedestrian()) {
            		   if(this.crosswalks.get(0).getPedestrian().isGoingDown()) this.crosswalks.get(0).getPedestrian().setY(this.crosswalks.get(0).getPedestrian().getY()+1);
            		   else if(this.crosswalks.get(0).getPedestrian().isGoingUp()) this.crosswalks.get(0).getPedestrian().setY(this.crosswalks.get(0).getPedestrian().getY()-1);
            	   }
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
   
   public boolean getNotStart() {
	   return notStart;
   }
   
   public void setNoStart(boolean noStart) {
	   this.notStart = noStart;
   }

   public void setTrafficLight(TrafficLight trafficLight) {
	   this.trafficLight = trafficLight;
   }
   
   public TrafficLight getTrafficLight() {
	   return this.trafficLight;
   }
   
   public void setObstacles(ArrayList<Obstacle> obstacles) {
	   this.obstacles = obstacles;
   }
   
   public void setCrosswalk(ArrayList<Crosswalk> crosswalks) {
	   this.crosswalks = crosswalks;
   }
   
   public void setCarLocation(Car car2) {
	   this.car = car2;
   }
       
   public byte getLanesQuantity() {
	return lanesQuantity;
}

public void setLanesQuantity(byte lanesQuantity) {
	this.lanesQuantity = lanesQuantity;
}

public void setObstaclesQuantity(byte obstaclesQuantity) {
	this.obstaclesQuantity = obstaclesQuantity;
}

private JPanel simulatorSettings;
   private JLabel jL_lanesQuantity;
   private JSlider jS_lanesQuantity;
   private JLabel jL_obstaclesQuantity;
   private JSlider jS_obstaclesQuantity;
   private JLabel jL_zoom;
   private JSlider jS_zoom;
   private JButton jB_apply;
   private JButton jB_start;
   
   private JMenu jM_File;
   private JMenu jM_Edit;
   private JMenuBar jMB_menuBar;
}