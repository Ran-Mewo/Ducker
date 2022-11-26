package net.minecraftforge.ducker.digger;

public class Main
{

    public static void main(String[] args)
    {
        new Main().run();
    }

    public void run()
    {
        final Echo echo = new Echo(getRandom());
        echo.echo();
    }

    public String getRandom()
    {
        return "Hello World!";
    }

    private static class Echo
    {
        private final String someValue;

        private Echo(final String someValue) {this.someValue = someValue;}

        public void echo()
        {
            System.out.println(someValue);
        }
    }
}
