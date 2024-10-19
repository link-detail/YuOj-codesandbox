
public class Main {
    public static void main(String[] args) throws InterruptedException{
        int a = Integer.parseInt (args[0]);
        int b = Integer.parseInt (args[1]);
        Thread.sleep(3000L);
        System.out.println (a+b);
    }
}
