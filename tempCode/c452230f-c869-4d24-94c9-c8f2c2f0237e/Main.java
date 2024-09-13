public class Main {
    public static void main(String[] args) throws InterruptedException{
        int a = Integer.parseInt (args[0]);
        int b = Integer.parseInt (args[1]);
        Thread.sleep(6000L);
        System.out.println ("结果是:"+(a+b));
    }
}
