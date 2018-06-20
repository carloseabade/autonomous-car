package autonomous_car_simulacao; 

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

	private Car car = new Car(50, 23, 1, 0, 0); // Coordenada onde o agente est� localizado.
	private ArrayList<Obstacle> obstacles = new ArrayList<>();
	private Crosswalk crosswalk;
	private TrafficLight trafficLight;
	private Pedestrian pedestrian;
	private String trafficLightState = "";
	private int pedestrianX;
	private int pedestrianY;

	private int fps = 1000 / 24;
	private boolean animate = true;
	private boolean notStart = true;
	private byte zoom = 2;

	private static DatagramSocket server;

	private byte lanesQuantity = 2;
	private byte obstaclesQuantity = 7;
	
	private boolean showWideSensor = true;
	private boolean showWideSensorOutline = false;
	private boolean showUltrasonicSensor = true;
	private boolean showUltrasonicSensorOutline = false;
	
	private int mouseY = 0;
	private int mouseDraggedY = 0;

	Simulator() {
		window = new JPanel();
		window.setLayout(new BorderLayout());

		JPanel drawing = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				g.setColor(Color.decode("#518535"));
				g.fillRect(0, 0, width, height);

				//Draw road
				g.setColor(Color.decode("#423d42"));
				g.fillRect(0, (74-mouseDraggedY)*zoom, width, 72*zoom);

				//Draw lane lines
				g.setColor(Color.decode("#daa226"));
				g.fillRect(0, (71-mouseDraggedY)*zoom, width, 4*zoom);

				g.setColor(Color.decode("#fbfbfb"));
				for(int i = 0; i < width+car.getX()*zoom; i += 36*zoom) {
					g.fillRect(i-car.getX()*zoom, (110-mouseDraggedY)*zoom, 23*zoom, 1*zoom);
				}

				g.setColor(Color.decode("#daa226"));
				g.fillRect(0, (146-mouseDraggedY)*zoom, width, 4*zoom);

				// Draw car, sensor, sensor wide, traffic light, crosswalk, pedestrian
				if(car.getY() != 0) {
					g.setColor(Color.decode("#fbfbfb"));
					
					if(crosswalk != null) { 
						// Draw crosswalk
						BufferedImage bi_crosswalk = null;
						try {
							bi_crosswalk = ImageIO.read(new File("./res/img/crosswalk.png"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						g.drawImage(bi_crosswalk, (crosswalk.getX()-car.getX())*zoom, crosswalk.getY()*zoom, 36*zoom, 72*zoom, null);
					}
					
					if(pedestrian != null) {
						// Draw pedestrian
						BufferedImage bi_pedestrian = null;
						try {
							bi_pedestrian = ImageIO.read(new File("./res/img/pedestrian.png"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						g.drawImage(bi_pedestrian, (pedestrianX-car.getX())*zoom, pedestrianY*zoom, 9*zoom, 5*zoom, null);
					}

					//Draw car
					BufferedImage bi_car = null;
					try {
						bi_car = ImageIO.read(new File("./res/img/tesla.png"));
					} catch (IOException e) {
						e.printStackTrace();
					}
					g.drawImage(bi_car, 0, (car.getY())*zoom, 50*zoom, 23*zoom, null);

					//Draw position
					g.setColor(Color.decode("#ff0000"));
					
					if(showUltrasonicSensor) {
						// Draw ultrasonic sensor
						BufferedImage bi_sensor = null;
						try {
							bi_sensor = ImageIO.read(new File("./res/img/sensor-all-sides.png"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						g.drawImage(bi_sensor, -40*zoom, (car.getY()-40)*zoom, 130*zoom, 103*zoom, null);
					}

					if(showUltrasonicSensorOutline) {
						// Draw ultrasonic sensor
						BufferedImage bi_sensor_outline = null;
						try {
							bi_sensor_outline = ImageIO.read(new File("./res/img/sensor-all-sides-outline.png"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						g.drawImage(bi_sensor_outline, -40*zoom, (car.getY()-40)*zoom, 130*zoom, 103*zoom, null);
					}
					
					if(showWideSensor) {
						// Draw sensor wide
						BufferedImage bi_sensor_wide = null;
						try {
							bi_sensor_wide = ImageIO.read(new File("./res/img/sensor-wide.png"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						g.drawImage(bi_sensor_wide, 30*zoom, (car.getY()-518)*zoom, 600*zoom, 1060*zoom, null);
					}
					
					if(showWideSensorOutline) {
						// Draw sensor wide
						BufferedImage bi_sensor_wide_outline = null;
						try {
							bi_sensor_wide_outline = ImageIO.read(new File("./res/img/sensor-wide-outline.png"));
						} catch (IOException e) {
							e.printStackTrace();
						}
						g.drawImage(bi_sensor_wide_outline, 30*zoom, (car.getY()-518)*zoom, 600*zoom, 1060*zoom, null);
					}

					// Draw obstacles
					BufferedImage bi_obstacle = null;
					try {
						bi_obstacle = ImageIO.read(new File("./res/img/obstacle.png"));
					} catch (IOException e) {
						e.printStackTrace();
					}
					for(Obstacle c : obstacles) {
						g.drawImage(bi_obstacle, (c.getX()-car.getX())*zoom, (c.getY())*zoom, 50*zoom, 23*zoom, null);
					}
					
					if(trafficLight != null) {
						// Draw traffic light
						BufferedImage bi_traffic_light = null;
						try {
							if(trafficLightState.equals("green")) {
								bi_traffic_light = ImageIO.read(new File("./res/img/traffic-light-green.png"));
							} else if(trafficLightState.equals("yellow")) {
								bi_traffic_light = ImageIO.read(new File("./res/img/traffic-light-yellow.png"));
							} else if(trafficLightState.equals("red")) {
								bi_traffic_light = ImageIO.read(new File("./res/img/traffic-light-red.png"));
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						g.drawImage(bi_traffic_light, (trafficLight.getX()-car.getX())*zoom, (trafficLight.getY())*zoom, 17*zoom, 67*zoom, null);
					}
				}
			}
		};
		
		drawing.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				System.out.println(mouseY);
				e.consume();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				mouseY = e.getY();
				e.consume();
			}			
		});

		simulatorSettings = new JPanel();
		simulatorSettings.setBorder(BorderFactory.createTitledBorder("Simulator Configuration:"));
		simulatorSettings.setLayout(new BoxLayout(simulatorSettings, BoxLayout.PAGE_AXIS));

		jL_obstaclesQuantity = new JLabel("Obstacles quantity: "+ obstaclesQuantity);
		jS_obstaclesQuantity = new JSlider();
		jS_obstaclesQuantity.setMaximum(50);
		jS_obstaclesQuantity.setValue(getObstaclesQuantity());
		jS_obstaclesQuantity.setMinimum(1);
		jS_obstaclesQuantity.setMajorTickSpacing(1);
		jS_obstaclesQuantity.setPaintTicks(true);
		jS_obstaclesQuantity.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				obstaclesQuantity = (byte) jS_obstaclesQuantity.getValue();
				jL_obstaclesQuantity.setText("Obstacles quantity: "+ obstaclesQuantity);
			}
		});		simulatorSettings.add(jL_obstaclesQuantity);
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
		
		jCB_wideSensor = new JCheckBox("Show wide sensor");
		jCB_wideSensor.setSelected(showWideSensor);
		jCB_wideSensor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(showWideSensor) showWideSensor = false;
				else showWideSensor = true;
			}
		});
		simulatorSettings.add(jCB_wideSensor);
		
		jCB_wideSensorOutline = new JCheckBox("Show wide sensor outline");
		jCB_wideSensorOutline.setSelected(showWideSensorOutline);
		jCB_wideSensorOutline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(showWideSensorOutline) showWideSensorOutline = false;
				else showWideSensorOutline = true;
			}
		});
		simulatorSettings.add(jCB_wideSensorOutline);

		jCB_ultrasonicSensor = new JCheckBox("Show ultrasonic sensor");
		jCB_ultrasonicSensor.setSelected(true);
		jCB_ultrasonicSensor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(showUltrasonicSensor) showUltrasonicSensor = false;
				else showUltrasonicSensor = true; 
			}
		});
		simulatorSettings.add(jCB_ultrasonicSensor);
		
		jCB_ultrasonicSensorOutline = new JCheckBox("Show ultrasonic sensor outline");
		jCB_ultrasonicSensorOutline.setSelected(showUltrasonicSensorOutline);
		jCB_ultrasonicSensorOutline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(showUltrasonicSensorOutline) showUltrasonicSensorOutline = false;
				else showUltrasonicSensorOutline = true;
			}
		});
		simulatorSettings.add(jCB_ultrasonicSensorOutline);

		jB_start = new JButton("Start");
		jB_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				notStart = false;
				jS_obstaclesQuantity.setEnabled(false);
				jL_obstaclesQuantity.setEnabled(false);
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
		case    "trafficLightState":
			trafficLightState = messageArray[1];
			break;
		case    "pedestrianLocation":
			pedestrianX = Integer.parseInt(messageArray[1]);
			pedestrianY = Integer.parseInt(messageArray[2]);
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

	public boolean getNotStart() {
		return notStart;
	}

	public void setNoStart(boolean noStart) {
		this.notStart = noStart;
	}

	public void setTrafficLight(TrafficLight trafficLight) {
		this.trafficLight = trafficLight;
	}
	
	public void setPedestrian(Pedestrian pedestrian) {
		this.pedestrian = pedestrian;
	}

	public void setObstacles(ArrayList<Obstacle> obstacles2) {
		this.obstacles = obstacles2;
	}

	public void setCrosswalk(Crosswalk crosswalk) {
		this.crosswalk = crosswalk;
	}

	public void setCarLocation(Car car) {
		this.car = car;
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
	private JLabel jL_obstaclesQuantity;
	private JSlider jS_obstaclesQuantity;
	private JLabel jL_zoom;
	private JSlider jS_zoom;
	private JButton jB_start;
	private JCheckBox jCB_wideSensor;
	private JCheckBox jCB_wideSensorOutline;
	private JCheckBox jCB_ultrasonicSensor;
	private JCheckBox jCB_ultrasonicSensorOutline;
}