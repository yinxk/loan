package top.yinxiaokang;

/**
 * Created by where on 2018/7/20.
 */
public class lambda {
    public static void main(String[] args) {
        int i = 0 ;
        new Thread(() -> {
            while (true){
                System.out.println("do some thing !" + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }
}
