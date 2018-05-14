package autonomous_car_3;

public class Coordinate {
	
	/*Posi��o no eixo x*/
	private int x;
	
	/*Posi��o no eixo y*/
	private int y;
	
	/*Comprimento do objeto*/
	private int length; 
	
	/*Largura do objeto*/
	private int width;
	
	public Coordinate() {
		
	}
	
	/*Retorna a posi��o da parte de tr�s do objeto*/
	public int back() {
		return x;
	}
	
	/*Retorna a posi��o da parte da frente do objeto*/
	public int front() {
		return x + length;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

}
