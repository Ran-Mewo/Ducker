package net.minecraftforge.ducker.digger;

public class Main {

    private final String someValue;

    private Main(final String someValue) {
        this.someValue = someValue;
    }

    public void echo(String otherValue) {
        if (shouldEcho()) {
            System.out.println(someValue + otherValue);
        }
    }

    public boolean shouldEcho() {
        return true;
    }

    public static void main(String[] args) {
        Main main = new Main("Hello World!");
        main.echo("Hello from call!");
    }
}