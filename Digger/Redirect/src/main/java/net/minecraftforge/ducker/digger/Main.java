package net.minecraftforge.ducker.digger;

public class Main {

    private final String someValue;

    private Main(final String someValue) {
        this.someValue = someValue;
    }

    public void echo(String otherValue) {
        System.out.println(otherValue);
        internalEcho();
    }

    public void internalEcho() {
        System.out.println(this.someValue);
    }

    public static void main(String[] args) {
        Main main = new Main("Hello World!");
        main.echo("Hello from call!");
    }
}