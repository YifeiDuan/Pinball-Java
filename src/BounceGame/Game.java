package BounceGame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Game extends JFrame implements Runnable {
    Bounce6 bounce;
    int timeInterval = 5;//刷新界面的时间间隔
    JButton btn0;
    JButton btn1;
    JButton btn2;
    JButton btn3;
    JButton btn4;
    boolean playing = true;
    boolean start = false;

    Game(){
        this.setTitle("Bounce Game");
        this.setBounds(100,0,800,1100);

        bounce = new Bounce6();
        this.setContentPane(bounce);
        this.setVisible(true);

        Object sync = this;

        //开始
        this.btn0 = new JButton("START");
        this.getContentPane().add(btn0);
        this.btn0.setBounds(630,40,130,50);
        this.btn0.addActionListener(new ActionListener() {
            @Override
            public synchronized void actionPerformed(ActionEvent e) {
                start = true;
                synchronized (sync) {
                    if(start){
                        sync.notify();
                    }
                }
                btn0.setEnabled(false);
                btn2.setEnabled(true);
                btn3.setEnabled(true);
            }
        });


        //重新开始
        this.btn1 = new JButton("RESTART");
        this.getContentPane().add(btn1);
        this.btn1.setBounds(630,100,130,50);
        this.btn1.addActionListener(new ActionListener() {
            @Override
            public synchronized void actionPerformed(ActionEvent e) {
                bounce = new Bounce6();
                setContentPane(bounce);
                setVisible(true);
                getContentPane().add(btn0);
                getContentPane().add(btn1);
                getContentPane().add(btn2);
                getContentPane().add(btn3);
                getContentPane().add(btn4);

                start = true;
                synchronized (sync) {
                    if(start){
                        sync.notify();
                    }
                }

                btn1.setEnabled(false);
                btn2.setEnabled(true);
                btn3.setEnabled(true);
            }
        });

        //暂停/恢复
        this.btn2 = new JButton("PAUSE/RESUME");
        this.getContentPane().add(btn2);
        this.btn2.setBounds(630,160,130,50);
        this.btn2.addActionListener(new ActionListener() {
            @Override
            public synchronized void actionPerformed(ActionEvent e) {
                playing = !playing;
                synchronized (sync) {
                    if(playing){
                        sync.notify();
                    }
                }
            }
        });

        //结束
        this.btn3 = new JButton("END");
        this.getContentPane().add(btn3);
        this.btn3.setBounds(630,220,130,50);
        this.btn3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start = false;
                btn1.setEnabled(true);
                btn2.setEnabled(false);
                btn3.setEnabled(false);
            }
        });

        //退出
        this.btn4 = new JButton("EXIT");
        this.getContentPane().add(btn4);
        this.btn4.setBounds(630,280,130,50);
        this.btn4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        if(!start){
            this.btn1.setEnabled(false);
            this.btn2.setEnabled(false);
            this.btn3.setEnabled(false);
        }  //若尚未开始，则不能点击重新开始、暂停/恢复、结束按钮

    }
    public void run(){
        while(true){
            try{
                Thread.sleep(timeInterval);
                if(!start){
                    synchronized(this) {
                        while (!start)
                            this.wait();
                    }
                }
                if (!playing) {
                    synchronized(this) {
                        while (!playing)
                            this.wait();
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
            repaint();
        }
    }
    public static void main(String[] args){
        Game game = new Game();
        game.run();
    }
}
