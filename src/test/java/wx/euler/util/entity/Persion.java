package wx.euler.util.entity;

public class Persion extends Animal<String>{
	private String name;
	private int age;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public void singASong() {
		System.out.println("π");
	}
	public int calculate(Integer num1,Integer num2) {
		return Math.addExact(num1,num2);
	}
	@Override
	public String toString() {
		return "Persion [name=" + name + ", age=" + age + "]";
	}
	
}
