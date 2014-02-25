package parser;


public class Pojo1 {


    String name;

    String surname;

    int a,b=3;

    public static void main(String args[]) {
        Pojo1 p = new Pojo1();
        int i = 0;


    }

    @Override
    public String toString() {
        return "Pojo1{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", a=" + a +
                ", b=" + b +
                '}';
    }
}
