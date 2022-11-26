package net.minecraftforge.ducker.digger;

import java.util.List;

public class Main {

    private final String someValue;

    private Main(final String someValue) {
        this.someValue = someValue;
    }

    public void echo(String otherValue, String secondValue) {
        echoInternal(otherValue, 12345, List.of(secondValue));
        System.out.println(this.someValue);
    }

    public void echoInternal(String otherValue, int numericValue, List<String> complexValue) {
        System.out.printf(otherValue, numericValue, complexValue);
        System.out.println(this.someValue);
    }

    public void echoException() {
        throw new IllegalArgumentException("Hello World");
    }

    public static void main(String[] args) {
        Main main = new Main("Hello World!");
        main.echo("Hello from call!", "Hello the second");

        final Builder builder = new Builder();
        builder.withSomeValue("Hello World!");
        builder.build().echo("test", "test2");
    }

    public static final class Builder {
        private String someValue;

        public Builder() {
        }

        public Builder withSomeValue(final String someValue) {
            this.someValue = someValue;
            return this;
        }

        public Main build() {
            return new Main(this.someValue);
        }
    }
}