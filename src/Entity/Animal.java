package Entity;

public abstract class Animal {
	private String Name;
	private String Movement;
	private String Strength;
	private String Weakness;
	
	public String getMovement() {
		return Movement;
	}

	public void setMovement(String movement) {
		Movement = movement;
	}

	public String getStrength() {
		return Strength;
	}

	public void setStrength(String strength) {
		Strength = strength;
	}

	public String getWeakness() {
		return Weakness;
	}

	public void setWeakness(String weakness) {
		Weakness = weakness;
	}

	public Animal(String name) {
		Name = name;
	}
	
	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}
}
