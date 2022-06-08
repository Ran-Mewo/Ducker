package net.minecraftforge.ducker.digger.modifyvariable;

public class Main {

    private final String someValue;

    private Main(final String someValue) {
        this.someValue = someValue;
    }

    public void echo(String otherValue) {
        System.out.println(someValue);
    }
}