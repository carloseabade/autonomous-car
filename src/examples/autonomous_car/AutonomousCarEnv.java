package autonomous_car;

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

	/*Instância do simulador*/
	private Simulator simulator = new Simulator();

	/*Quantidade de faixas  e obstáculos no ambiente (Pelo menos 2 lanes e 1 obstáculo)*/
	private int nLanes;
	private int nObstacles; 

	/*Lista que armazena as as informações de todos os obstáculos*/
	private ArrayList<Obstacle> obstacles;

	/*Classe que armazena as posições de cada pista*/
	private Road road;

	/*Controle dos obstáculos encontrados, analizados, sinalizados e ultrapassados*/
	private int foundObstacle;
	private int analizedObstacle;
	private int sinalized;
	private int overpastObstacle; 

	/*Tempo de espera para enviar as mensagens*/
	private int waitTimeLocation; 

	/*Controla a mudança de tempo em relação a aceleração e velocidade do carro*/
	private int timeControl; 

	/*Inicia simulador junto ao construtor do ambiente*/
	public AutonomousCarEnv() {
		new Thread(new Runnable() {
			public void run() {
				simulator.startAnimation();
			}
		}).start();
	}

	/*Inicia informações do ambiente*/
	@Override
	public void setMAS(MAS m) {
		super.setMAS(m);
		
		while(simulator.getNotStart()) {System.out.println("Stopped");}
		
		this.waitTimeLocation = 1000; //1 seg == 1000 milisegundos 
		this.timeControl = 0;

		/*Tamanho da lane e posições referentes ao meio das pistas 1, 2, 3, 4 respectivamente*/
		this.road = new Road(36, 117, 80, 43, 6);
		
		this.nLanes = 2;

		/*Inicia o carro na ordem: length, width, velocity, ultrasonicSensor, wideSensor, x, y*/
		this.car = new Car(50, 23, 0, 0, getRoad((int)(Math.random() * 2 + 1)));
		//this.car = new Car(50, 23, 0, 0, getRoad(1));

		this.nObstacles = 20;
		this.obstacles = new ArrayList<Obstacle>(nObstacles);
		
		this.foundObstacle = 0;
		this.analizedObstacle = 0;
		this.sinalized = 0;
		this.overpastObstacle = 0;

		initObstacle();

		Predicate start = new Predicate("start");
		addPercept("car", start);

	}

	public Unifier executeAction(String agName, Action act) throws AILexception {
		Unifier u = new Unifier();

		if(act.getFunctor().equals("sensor_enable")) {

			car.setUltrasonicSensor(40);
			car.setWideSensor(300);

			Predicate velocity = new Predicate("velocity");
			velocity.addTerm(new NumberTermImpl(car.getVelocity()));
			addPercept(agName, velocity);

			/*1 m/s² --- * 10 por cada posição da matriz corresponder a 10cm*/
			car.setAcceleration(1*10);

		} else if(act.getFunctor().equals("gps_enable")) {

			Predicate at = new Predicate("at");
			at.addTerm(new NumberTermImpl(car.getX()));
			at.addTerm(new NumberTermImpl(car.getY()));
			addPercept(agName, at);

			/*30 m/s = 108 km/h --- * 10 por cada posição da matriz corresponder a 10cm*/
			car.setMaxVelocity(30*10);

			Predicate maxVelocity = new Predicate("maxVelocity");
			maxVelocity.addTerm(new NumberTermImpl(car.getMaxVelocity()));
			addPercept(agName, maxVelocity);

			switch (nLanes) {
			case 2: 
				addLane1(agName);
				addLane2(agName);
				break;
			case 3:
				addLane1(agName);
				addLane2(agName);
				addLane3(agName);
				break;
			case 4:
				addLane1(agName);
				addLane2(agName);
				addLane3(agName);
				addLane4(agName);
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

		} else if (act.getFunctor().equals("check_env")) {

			checkEnvironment(agName);

		} else if(act.getFunctor().equals("around")) {

			around(agName);

		} else if(act.getFunctor().equals("go_right")) {

			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);
			car.setY(car.getY() + 1); 

			System.err.println("CHANGED LANE " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);

			if(car.getY() == road.getLane1Pos() || car.getY() == road.getLane2Pos()) {

				Predicate change_lane = new Predicate("change_lane");
				removePercept(agName, change_lane);
				System.out.println("change_lane removed");

			}

			sendMessageSimulator();

		} else if(act.getFunctor().equals("go_left")) {

			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);
			car.setY(car.getY() - 1); 

			System.err.println("CHANGED LANE " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);

			if(car.getY() == road.getLane1Pos() || car.getY() == road.getLane2Pos()) {

				Predicate change_lane = new Predicate("change_lane");
				removePercept(agName, change_lane);
				System.out.println("change_lane removed");

			}

			sendMessageSimulator();

		} else if(act.getFunctor().equals("remove_change_lane")) {

			Predicate change_lane = new Predicate("change_lane");
			removePercept(agName, change_lane);
			System.out.println("change_lane removed");

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

	/**************************************************************************** FUNÇÕES ****************************************************************************/
	private int getRoad(int roadNumber) {
		switch(roadNumber) {
			case 1: return 117;
			case 2: return 80;
			case 3: return 43;
			case 4: return 6;
		}
		return -1;
	}
	
	/*Popula o Array com os obstáculos e define as coordenadas aleatoriamente*/
	private void initObstacle() {
		/*obstacles.add(new Obstacle(50, 20, 100, road.getLane1Pos()));
		obstacles.add(new Obstacle(50, 20, 230, road.getLane2Pos()));
		obstacles.add(new Obstacle(50, 20, 750, road.getLane1Pos()));
		obstacles.add(new Obstacle(50, 20, 900, road.getLane2Pos()));
		obstacles.add(new Obstacle(50, 20, 980, road.getLane1Pos()));*/

		int i = 1;

		Obstacle aux = new Obstacle(50, 20, (100 + (int)(Math.random() * 101)), getRoad((int)(Math.random() * nLanes + 1)));

		obstacles.add(new Obstacle(aux.getLength(), aux.getWidth(), aux.getX(), aux.getY()));

		while (i < nObstacles) {
			aux.setX(obstacles.get(i-1).front() + (int)(Math.random() * 151));
			aux.setY(getRoad((int)(Math.random() * nLanes + 1)));

			obstacles.add(new Obstacle(aux.getLength(), aux.getWidth(), aux.getX(), aux.getY()));

			i++;
		}
		System.out.println("Environment obstacles:");

		i = 0;
		while (i < nObstacles) {
			System.out.println("Obstacle (" + obstacles.get(i).getX() + "," + obstacles.get(i).getY() + ")");
			i++;
		}

		simulator.setObstacles(obstacles);
	}

	/*Função que faz o carro se aproximar do obstáculo*/
	private void around(String agName) {
		if(sinalized == 0 && obstacles.get(sinalized).back() > car.front()+20) {
			speedDown(agName);

			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);

			System.err.println("MOVING " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);

			sendMessageSimulator();

			System.out.println("1");
		} else if (obstacles.size() > sinalized+1 && obstacles.get(sinalized).back() <= car.back() && obstacles.get(sinalized+1).back() > car.front()+20) {
			speedDown(agName);

			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);

			System.err.println("MOVING " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);

			sendMessageSimulator();
			System.out.println("2");
		} else if (sinalized-2 > 0 && obstacles.get(sinalized-2).back() <= car.back() && obstacles.get(sinalized-1).back() > car.front()+20) {
			speedDown(agName);

			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);

			System.err.println("MOVING " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);

			sendMessageSimulator();
			System.out.println("3");
		} else if (sinalized-1 > 0 && obstacles.get(sinalized-1).back() <= car.back() && obstacles.get(sinalized).back() > car.front()+20) {
			speedDown(agName);

			removeOldCarPosition(agName);

			car.setX(car.getX() + 1);

			System.err.println("MOVING " + car.getX() + " " + car.getY());

			addNewCarPosition(agName);

			sendMessageSimulator();
			System.out.println("4");
		} else { 
			Predicate change_lane = new Predicate("change_lane");
			addPercept(agName, change_lane);
			System.out.println("change_lane add");
		}	
	}

	/*Sensores analizam o ambiente*/
	private void checkEnvironment(String agName) {
		
		/*Informa a lane que o carro se encontra*/
		if(car.getY() < road.getLane1Bottom() && car.getY() > road.getLane1Top()) {
			Predicate in_lane2 = new Predicate("in_lane2");
			removePercept(agName, in_lane2);

			Predicate in_lane1 = new Predicate("in_lane1");
			addPercept(agName, in_lane1);

			System.out.println("In lane 1");
		} else if (car.getY() < road.getLane2Bottom() && car.getY() > road.getLane2Top()) {
			Predicate in_lane1 = new Predicate("in_lane1");
			removePercept(agName, in_lane1);

			Predicate in_lane2 = new Predicate("in_lane2");
			addPercept(agName, in_lane2);	
			System.out.println("In lane 2");
		}

		/*Encontra os obstáculos que estão dentro do alcance do WideSensor*/
		while(foundObstacle < obstacles.size() && !obstacles.get(foundObstacle).getFound() && obstacles.get(foundObstacle).back() <= car.front()+car.getWideSensor()) 
		{
			obstacles.get(foundObstacle).setFound(true);
			System.out.println("FoundObstacle: (" + obstacles.get(foundObstacle).getX() + "," + obstacles.get(foundObstacle).getY() + ")");
			foundObstacle += 1;
		}

		/*Envia para o agente os obstáculos a serem analizados*/
		if (obstacles.size() > analizedObstacle && analizedObstacle == 0 && obstacles.get(analizedObstacle).getFound() && !obstacles.get(analizedObstacle).getAnalized()) {
			addObs1(agName, analizedObstacle);
		} else if (obstacles.size() > analizedObstacle && analizedObstacle-3 >= 0 && obstacles.get(analizedObstacle).getFound() && !obstacles.get(analizedObstacle).getAnalized() && obstacles.get(analizedObstacle-3).getOverPast()) {
			addObs1(agName, analizedObstacle);
		}

		if (obstacles.size() > analizedObstacle+1 && analizedObstacle == 0 && obstacles.get(analizedObstacle+1).getFound() && !obstacles.get(analizedObstacle+1).getAnalized()) {
			addObs2(agName, analizedObstacle+1);	
		} else if (obstacles.size() > analizedObstacle+1 && analizedObstacle-2 >= 0 && obstacles.get(analizedObstacle+1).getFound() && !obstacles.get(analizedObstacle+1).getAnalized() && obstacles.get(analizedObstacle-2).getOverPast()) {
			addObs2(agName, analizedObstacle+1);
		}

		if (obstacles.size() > analizedObstacle+2 && analizedObstacle == 0 && obstacles.get(analizedObstacle+2).getFound() && !obstacles.get(analizedObstacle+2).getAnalized()) {
			addObs3(agName, analizedObstacle+2);
			analizedObstacle += 3;
		} else if (obstacles.size() > analizedObstacle+2 && analizedObstacle-1 >= 0 && obstacles.get(analizedObstacle+2).getFound() && !obstacles.get(analizedObstacle+2).getAnalized() && obstacles.get(analizedObstacle-1).getOverPast()) {
			addObs3(agName, analizedObstacle+2);
			analizedObstacle += 3;
		}


		/*Encontra os obstáculos que estão dentro do alcance do WideSensor*/
		if (obstacles.size() > sinalized && obstacles.get(sinalized).getAnalized() && !obstacles.get(sinalized).getSinalized() && obstacles.get(sinalized).back() <= car.front()+car.getUltrasonicSensor() && !obstacles.get(sinalized).getSinalized()) {
			Predicate obs1Close = new Predicate("obs1Close");
			addPercept(agName, obs1Close);
			obstacles.get(sinalized).setSinalized(true);
			System.out.println("obs1close");
		}

		if (obstacles.size() > sinalized+1 && obstacles.get(sinalized+1).getAnalized() && !obstacles.get(sinalized+1).getSinalized() && obstacles.get(sinalized+1).back() <= car.front()+car.getUltrasonicSensor() && !obstacles.get(sinalized+1).getSinalized()) {
			Predicate obs2Close = new Predicate("obs2Close");
			addPercept(agName, obs2Close);
			obstacles.get(sinalized+1).setSinalized(true);
			System.out.println("obs2close");
		}

		if (obstacles.size() > sinalized+2 && obstacles.get(sinalized+2).getAnalized() && !obstacles.get(sinalized+2).getSinalized() && obstacles.get(sinalized+2).back() <= car.front()+car.getUltrasonicSensor() && !obstacles.get(sinalized+2).getSinalized()) {
			Predicate obs3Close = new Predicate("obs3Close");
			addPercept(agName, obs3Close);
			obstacles.get(sinalized+2).setSinalized(true);
			System.out.println("obs3close");
			sinalized += 3;
		}

		/*Remove os obstaculos que já foram ultrapassados*/
		if(obstacles.size() > overpastObstacle && car.back() > obstacles.get(overpastObstacle).front() && !obstacles.get(overpastObstacle).getOverPast()) {
			removeObs1(agName, overpastObstacle);
		}

		if(obstacles.size() > overpastObstacle+1 && car.back() > obstacles.get(overpastObstacle+1).front() && !obstacles.get(overpastObstacle+1).getOverPast()) {
			removeObs2(agName, overpastObstacle+1);
		}

		if(obstacles.size() > overpastObstacle+2 && car.back() > obstacles.get(overpastObstacle+2).front() && !obstacles.get(overpastObstacle+2).getOverPast()) {
			removeObs3(agName, overpastObstacle+2);
			overpastObstacle += 3;
		}
	}
	
	private void removeOldCarPosition(String agName) {
		Predicate old_position = new Predicate("at");
		old_position.addTerm(new VarTerm("X"));
		old_position.addTerm(new VarTerm("Y"));

		removeUnifiesPercept(agName, old_position);
	}

	private void addNewCarPosition(String agName) {
		Predicate at = new Predicate("at");
		at.addTerm(new NumberTermImpl(car.getX()));
		at.addTerm(new NumberTermImpl(car.getY()));

		addPercept(agName, at);

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

		/*if (timeControl > 1) {
			timeControl -=1;
			waitTimeLocation = waitTimeLocation + (waitTimeLocation / timeControl);
		} else if (timeControl == 1) {
			waitTimeLocation = waitTimeLocation * car.getAcceleration();

		}*/

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

		obstacles.get(i).setOverPast(true);

		Predicate obs1Close = new Predicate("obs1Close");
		removePercept(agName, obs1Close);
		System.out.println("Remove obs1close");
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

		obstacles.get(i).setOverPast(true);

		Predicate obs2Close = new Predicate("obs2Close");
		removePercept(agName, obs2Close);
		System.out.println("Remove obs2close");
	}

	private void addObs3(String agName, int i) {
		Predicate obs3 = new Predicate("obs3");
		obs3.addTerm(new NumberTermImpl(obstacles.get(i).getX()));
		obs3.addTerm(new NumberTermImpl(obstacles.get(i).getY()));
		addPercept(agName, obs3);

		obstacles.get(i).setAnalized(true);

		System.out.println("Obstaculo3 (" + obstacles.get(i).getX() + "," + obstacles.get(i).getY() + ")");	
	}

	private void removeObs3(String agName, int i) {
		Predicate obs3 = new Predicate("obs3");
		obs3.addTerm(new NumberTermImpl(obstacles.get(i).getX()));
		obs3.addTerm(new NumberTermImpl(obstacles.get(i).getY()));
		removePercept(agName, obs3);

		obstacles.get(i).setOverPast(true);

		Predicate obs3Close = new Predicate("obs3Close");
		removePercept(agName, obs3Close);
		System.out.println("Remove obs3close");
	}

	/*NumberTermImpl(1) == mesma direção. NumberTermImpl(-1) == direção oposta*/
	private void addLane1(String agName) {
		Predicate lane1 = new Predicate("lane1");
		lane1.addTerm(new NumberTermImpl(1));
		addPercept(agName, lane1);
	}

	private void addLane2(String agName) {
		Predicate lane2 = new Predicate("lane2");
		lane2.addTerm(new NumberTermImpl(1));
		addPercept(agName, lane2);
	}

	private void addLane3(String agName) {
		Predicate lane3 = new Predicate("lane3");
		lane3.addTerm(new NumberTermImpl(-1));
		addPercept(agName, lane3);
	}

	private void addLane4(String agName) {
		Predicate lane4 = new Predicate("lane4");
		lane4.addTerm(new NumberTermImpl(-1));
		addPercept(agName, lane4);
	}

	private void sendMessageSimulator() {
		try {
			TimeUnit.MILLISECONDS.sleep(waitTimeLocation);
		} catch(Exception e) {
			System.err.println(e);
		}
		simulator.setCarLocation(car);
	}

}