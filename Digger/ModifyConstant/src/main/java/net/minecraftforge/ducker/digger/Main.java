package net.minecraftforge.ducker.digger;

public class Main {

    private final String someValue;

    private Main(final String someValue) {
        this.someValue = someValue;
    }

    public void echo(String otherValue) {
        System.out.println(otherValue);
        System.out.println(this.someValue);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 6; i++) {
            Main main = new Main("Hello World!");
            main.echo("Hello from call!");
        }
    }
}