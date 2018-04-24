package autonomous_car_2;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import ail.mas.DefaultEnvironment;
import ail.mas.MAS;
import ail.syntax.Action;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.Unifier;
import ail.syntax.VarTerm;
import ail.util.AILexception;

public class AutonomousCarEnv extends DefaultEnvironment{

	/* Armazena as inforçaões do carro e a sua posição*/
	private Car car; 

	/*Quantidade de faixas  e obstáculos no ambiente (Pelo menos 2 lanes e 1 obstáculo)*/
	private int nLanes;
	private int nObstacles; 

	/*Lista que armazena as as informações de todos os obstáculos*/
	private ArrayList<Obstacle> obstacles;

	/*Controle dos obstáculos encontrados, analizados e ultrapassados*/
	private int foundObstacle;
	private int analizedObstacle;
	private int overpastObstacle;

	/*Determina se o ambiente irá enviar as mensagens para o simulador*/
	private boolean simulate; 

	/*Controle para nao enviar obstaculos mais de uma vez para o simulador*/
	private boolean notSend;

	/*Tempo de espera para enviar as mensagens*/
	private int waitTimeLocation; 
	
	/*Controla a mudança de tempo em relação a aceleração e velocidade do carro*/
	private int timeControl = 0; 

	/*Inicia informações do ambiente*/
	@Override
	public void setMAS(MAS m) {
		super.setMAS(m);

		this.simulate = true;
		this.notSend = true;
		this.waitTimeLocation = 10000; //10 seg == 10000 milisegundos 

		/*Inicia o carro na ordem: length, width, velocity, ultrasonicSensor, wideSensor, x, y*/
		this.car = new Car(5, 2, 0, 4, 30, 0, 0);

		this.nLanes = 2;

		this.nObstacles = 1;
		this.obstacles = new ArrayList<Obstacle>(nObstacles);
		this.foundObstacle = 0;
		this.analizedObstacle = 0;
		this.overpastObstacle = 0;
		
		initObstacle();
		
		Predicate carSize = new Predicate("carSize");
		carSize.addTerm(new NumberTermImpl(car.getLength()));
		carSize.addTerm(new NumberTermImpl(car.getWidth()));
		addPercept("car", carSize);

		Predicate start = new Predicate("start");
		addPercept("car", start);

	}

	public Unifier executeAction(String agName, Action act) throws AILexception {
		Unifier u = new Unifier();

		if(act.getFunctor().equals("sensor_enable")) {

			Predicate wideSensor = new Predicate("wideSensor");
			wideSensor.addTerm(new NumberTermImpl(car.getWideSensor()));
			addPercept("car", wideSensor);

			Predicate ultrasonicSensor = new Predicate("ultrasonicSensor");
			ultrasonicSensor.addTerm(new NumberTermImpl(car.getUltrasonicSensor()));
			addPercept("car", ultrasonicSensor);
			
		} else if(act.getFunctor().equals("gps_enable")) {

			Predicate at = new Predicate("at");
			at.addTerm(new NumberTermImpl(car.getX()));
			at.addTerm(new NumberTermImpl(car.getY()));
			addPercept(agName, at);
			
			Predicate velocity = new Predicate("velocity");
			velocity.addTerm(new NumberTermImpl(car.getVelocity()));
			addPercept(agName, velocity);
			
			/*30 m/s = 108 km/h --- * 10 por cada posição da matriz corresponder a 10cm*/
			car.setMaxVelocity(30*10);
			
			/*1 m/s² --- * 10 por cada posição da matriz corresponder a 10cm*/
			car.setAcceleration(1*10);
			
			Predicate maxVelocity = new Predicate("maxVelocity");
			maxVelocity.addTerm(new NumberTermImpl(car.getMaxVelocity()));
			addPercept(agName, maxVelocity);
			
			Predicate acceleration = new Predicate("acceleration");
			acceleration.addTerm(new NumberTermImpl(car.getAcceleration()));
			addPercept(agName, acceleration);
			
			switch (nLanes) {
			case 2: 
				addLane0(agName);
				addLane1(agName);
				break;
			case 3:
				addLane0(agName);
				addLane1(agName);
				addLane2(agName);
				break;
			case 4:
				addLane0(agName);
				addLane1(agName);
				addLane2(agName);
				addLane3(agName);
				break;
			default:
				break;
			}
			

		} else if(act.getFunctor().equals("run")) {	
			
			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);

			System.err.println("MOVING " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);

			sendMessageSimulator();
			
		}
		else if (act.getFunctor().equals("check_env")) {
			
			checkEnvironment(agName);
			
		} else if(act.getFunctor().equals("around")) {
			if(obstacles.get(analizedObstacle-1).back() > car.front()+1) {
				speedDown(agName);
				
				removeOldCarPosition(agName);

				car.setX(car.getX() + 1);

				System.err.println("MOVING " + car.getX() + " " + car.getY());

				addNewCarPosition(agName);

				sendMessageSimulator();
			} else {
				Predicate change_lane = new Predicate("change_lane");

				addPercept(agName, change_lane);
			}

		} else if(act.getFunctor().equals("go_right")) {

			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);
			car.setY(car.getY() + 1); 

			System.err.println("CHANGED LANE " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);	
			
			Predicate change_lane = new Predicate("change_lane");
			removePercept(agName, change_lane);

			sendMessageSimulator();

		} 
		else if(act.getFunctor().equals("go_left")) {

			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);
			car.setY(car.getY() - 1); 

			System.err.println("CHANGED LANE " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);
			
			Predicate change_lane = new Predicate("change_lane");
			removePercept(agName, change_lane);

			sendMessageSimulator();

		} else if(act.getFunctor().equals("removeObs1")) {
			
			obstacles.get(overpastObstacle).setOverPast(true);
			
			removeObs1(agName, overpastObstacle);
			
			overpastObstacle += 1;

		} else if(act.getFunctor().equals("removeObs2")) {
			
			obstacles.get(overpastObstacle).setOverPast(true);
			
			removeObs2(agName, overpastObstacle);
			
			overpastObstacle += 1;

		} else if(act.getFunctor().equals("accelerate")) {
			
			speedUp(agName);
			
			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);

			System.err.println("MOVING " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);

			sendMessageSimulator();
			
		} else if(act.getFunctor().equals("decelerate")) {
			
			speedDown(agName);
			
			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);

			System.err.println("MOVING " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);

			sendMessageSimulator();
			
		} else if(act.getFunctor().equals("stop")) {
			
			Predicate stopped = new Predicate("stopped");
			addPercept(agName, stopped);

			Predicate start = new Predicate("start");
			removePercept(agName, start);

			sendMessageSimulator(); 
		}

		super.executeAction(agName, act);

		return u;

	}
	
	/*Popula o Array com os obstáculos e define as coordenadas aleatoriamente*/
	private void initObstacle() {
		obstacles.add(new Obstacle(5, 2, 35, 0));
		
		Client.sendMessage( Client.convertArray2String( new String[] 
				{"obsLocation", String.valueOf(obstacles.get(0).getX()), String.valueOf(obstacles.get(0).getY())} ) );
		System.out.println("Obstacle (" + obstacles.get(0).getX() + "," + obstacles.get(0).getY() + ")");
		
		if (false) {
			int i = 1;

			Obstacle aux = new Obstacle(5, 2, 9 + (int)(Math.random() * 31), (int) (Math.random() * 2));

			obstacles.add(new Obstacle(aux.getLength(), aux.getWidth(), aux.getX(), aux.getY()));

			while (i < nObstacles) {
				do {
					aux.setX(obstacles.get(i-1).front() + (int)(Math.random() * 31));
					aux.setY((int) (Math.random() * 2));
				} while (aux.getX() == obstacles.get(i-1).front() && obstacles.get(i-1).getY() == aux.getY());

				obstacles.add(new Obstacle(aux.getLength(), aux.getWidth(), aux.getX(), aux.getY()));
				
				i++;
			}
			System.out.println("Environment obstacles:");
			

			i = 0;

			if(simulate) {
				while (i < nObstacles) {
					Client.sendMessage( Client.convertArray2String( new String[] 
							{"obsLocation", String.valueOf(obstacles.get(i).getX()), String.valueOf(obstacles.get(i).getY())} ) );
					System.out.println("Obstacle (" + obstacles.get(i).getX() + "," + obstacles.get(i).getY() + ")");
					/*try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch(Exception e) {
						System.err.println(e);
					}*/

					i++;
				}
			}
			notSend = false;
		}
	}

	/*Remove a crença da posição atual do carro*/
	private void removeOldCarPosition(String agName) {
		Predicate old_position = new Predicate("at");
		old_position.addTerm(new VarTerm("X"));
		old_position.addTerm(new VarTerm("Y"));

		removeUnifiesPercept(agName, old_position); //remove old position
	}
	
	/*Adiciona a crença da nova posição do carro*/
	private void addNewCarPosition(String agName) {
		Predicate at = new Predicate("at");
		at.addTerm(new NumberTermImpl(car.getX()));
		at.addTerm(new NumberTermImpl(car.getY()));

		addPercept(agName, at); //inform new position to the agent

		Predicate going_forward = new Predicate("going_forward");

		addPercept(agName, going_forward);
	}
	
	private void speedUp(String agName) {
		removeOldVelocity(agName);
		
		car.setVelocity(car.getVelocity() + car.getAcceleration());
		
		if (timeControl == 0) {
			timeControl +=1;
			waitTimeLocation = waitTimeLocation/car.getAcceleration();
		} else {
			timeControl +=1;
			waitTimeLocation = waitTimeLocation - (waitTimeLocation / timeControl);
		}
		
		addNewVelocity(agName);
	}
	
	private void speedDown(String agName) {	
		removeOldVelocity(agName);
		
		car.setVelocity(car.getVelocity() - car.getAcceleration());
		
		if (timeControl > 1) {
			timeControl -=1;
			waitTimeLocation = waitTimeLocation + (waitTimeLocation / timeControl);
		} else if (timeControl == 1) {
			waitTimeLocation = waitTimeLocation * car.getAcceleration();
			
		}
		
		addNewVelocity(agName);
	}
	
	private void removeOldVelocity(String agName) {
		Predicate old_Velocity = new Predicate("velocity");
		old_Velocity.addTerm(new VarTerm("X"));

		removeUnifiesPercept(agName, old_Velocity);
	}
	
	private void addNewVelocity(String agName) {
		Predicate velocity = new Predicate("velocity");
		velocity.addTerm(new NumberTermImpl(car.getVelocity()));

		addPercept(agName, velocity); //inform new position to the agent
	}

	/*Sensores analizam o ambiente*/
	private void checkEnvironment(String agName) {
		
		if(car.getY() == 0) {
			Predicate in_lane1 = new Predicate("in_lane1");
			removePercept(agName, in_lane1);
			
			Predicate in_lane0 = new Predicate("in_lane0");
			addPercept(agName, in_lane0);
		} else if (car.getY() == 1) {
			Predicate in_lane0 = new Predicate("in_lane0");
			removePercept(agName, in_lane0);
			
			Predicate in_lane1 = new Predicate("in_lane1");
			addPercept(agName, in_lane1);			
		}
		
		/*Encontra os obstáculos que estão dentro do alcance do WideSensor*/
		while(foundObstacle < obstacles.size() && !obstacles.get(foundObstacle).getFound() && obstacles.get(foundObstacle).front() <= car.front()+car.getWideSensor()) 
		{
			obstacles.get(foundObstacle).setFound(true);
			System.out.println("FoundObstacle: (" + obstacles.get(foundObstacle).getX() + "," + obstacles.get(foundObstacle).getY() + ")");
			foundObstacle += 1;
		}

		/*Envia para o agente os obstáculos a serem analizados*/
		if (analizedObstacle == 0 && obstacles.get(analizedObstacle).getFound()) {
			addObs1(agName, analizedObstacle);
			
			if(obstacles.size() > analizedObstacle+1 && obstacles.get(analizedObstacle+1).getFound()) {
				addObs2(agName, analizedObstacle+1);
			}
			analizedObstacle += 1;			
		} else if(obstacles.size() > analizedObstacle && obstacles.get(analizedObstacle).getFound() && !obstacles.get(analizedObstacle).getAnalized()) {
			addObs2(agName, analizedObstacle);
			
		} else if (obstacles.size() > analizedObstacle && analizedObstacle > 0 && obstacles.get(analizedObstacle-1).getOverPast()) {
			addObs1(agName, analizedObstacle);
			
			removeObs2(agName, analizedObstacle-1);
			
			if(obstacles.size() > analizedObstacle+1 && obstacles.get(analizedObstacle+1).getFound()) {
				addObs2(agName, analizedObstacle+1);
			}
			analizedObstacle += 1;
		}
		
		if (obstacles.size() > overpastObstacle && obstacles.get(overpastObstacle).getAnalized() && obstacles.get(overpastObstacle).back() == car.front()+car.getUltrasonicSensor()) {
			Predicate obs1Close = new Predicate("obs1Close");
			addPercept(agName, obs1Close);
		}
		
		if (obstacles.size() > overpastObstacle+1 && obstacles.get(overpastObstacle+1).getAnalized() && obstacles.get(overpastObstacle+1).back() == car.front()+car.getUltrasonicSensor()) {
			Predicate obs2Close = new Predicate("obs2Close");
			addPercept(agName, obs2Close);
		}
		
	}
	
	private void addObs1(String agName, int i) {
		Predicate obs1 = new Predicate("obs1");
		obs1.addTerm(new NumberTermImpl(obstacles.get(i).getX()));
		obs1.addTerm(new NumberTermImpl(obstacles.get(i).getY()));
		addPercept(agName, obs1);
		
		obstacles.get(i).setAnalized(true);
		
		System.out.println("Obstaculo1 (" + obstacles.get(i).getX() + "," + obstacles.get(i).getY() + ")");
	}
	
	private void removeObs1(String agName, int i) {
		Predicate obs1 = new Predicate("obs1");
		obs1.addTerm(new NumberTermImpl(obstacles.get(i).getX()));
		obs1.addTerm(new NumberTermImpl(obstacles.get(i).getY()));
		removePercept(agName, obs1);	
		
		Predicate obs1Close = new Predicate("obs1Close");
		removePercept(agName, obs1Close);
	}
	
	private void addObs2(String agName, int i) {
		Predicate obs2 = new Predicate("obs2");
		obs2.addTerm(new NumberTermImpl(obstacles.get(i).getX()));
		obs2.addTerm(new NumberTermImpl(obstacles.get(i).getY()));
		addPercept(agName, obs2);
		
		obstacles.get(i).setAnalized(true);
		
		System.out.println("Obstaculo2 (" + obstacles.get(i).getX() + "," + obstacles.get(i).getY() + ")");	
	}
	
	private void removeObs2(String agName, int i) {
		Predicate obs2 = new Predicate("obs2");
		obs2.addTerm(new NumberTermImpl(obstacles.get(i).getX()));
		obs2.addTerm(new NumberTermImpl(obstacles.get(i).getY()));
		removePercept(agName, obs2);
		
		Predicate obs2Close = new Predicate("obs2Close");
		removePercept(agName, obs2Close);
	}

	/*NumberTermImpl(1) == mesma direção. NumberTermImpl(-1) == direção oposta*/
	private void addLane0(String agName) {
		Predicate lane0 = new Predicate("lane0");
		lane0.addTerm(new NumberTermImpl(1));
		addPercept(agName, lane0);
	}

	private void addLane1(String agName) {
		Predicate lane1 = new Predicate("lane1");
		lane1.addTerm(new NumberTermImpl(1));
		addPercept(agName, lane1);
	}

	private void addLane2(String agName) {
		Predicate lane2 = new Predicate("lane2");
		lane2.addTerm(new NumberTermImpl(-1));
		addPercept(agName, lane2);
	}

	private void addLane3(String agName) {
		Predicate lane3 = new Predicate("lane3");
		lane3.addTerm(new NumberTermImpl(-1));
		addPercept(agName, lane3);
	}

	/*Atualiza as informações do carro no simulador*/
	private void sendMessageSimulator() {
		if(simulate) {
			try {
				TimeUnit.MILLISECONDS.sleep(waitTimeLocation);
			} catch(Exception e) {
				System.err.println(e);
			}
			Client.sendMessage( Client.convertArray2String( new String[] 
					{"carLocation", String.valueOf(car.getX()), String.valueOf(car.getY())} ) );
		}
	}

}