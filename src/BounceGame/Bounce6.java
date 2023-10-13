package BounceGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.*;
import java.applet.*;

import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import jdk.nashorn.internal.objects.NativeFloat32Array;
import sun.audio.*;
import java.io.*;
import java.util.Timer;

public class Bounce6 extends JPanel {
    //图形边界的参数
    int width = 600;
    int height = 1000;
    //小球大小与运动参数
    int r = 15;
    int ballx0 = width/2-r;
    int bally0 = 0;
    ArrayList<Integer> ballx = new ArrayList<Integer>();  //记录每个球的位置x坐标
    ArrayList<Integer> bally = new ArrayList<Integer>();  //记录每个球的位置y坐标

    int v = 0;
    int v0 = 3;

    ArrayList<Double> cosv = new ArrayList<Double>(); //运动投影到x方向上的速度大小缩放因子
    ArrayList<Double> sinv = new ArrayList<Double>(); //运动投影到y方向上的速度大小缩放因子
    double a = 0.05;

    boolean isMoving = false;
    ArrayList<Boolean> moving = new ArrayList<Boolean>();  //判断每个小球是否在运动

    //鼠标位置
    int mousex = 0;
    int mousey = 0;
    //下面三个二维数组记录集合体信息
    int numType = 5; //几何体种数
    int numTool = 4;  //道具种数
    ArrayList<IntegerArray> type = new ArrayList<IntegerArray>();
    ArrayList<IntegerArray> value = new ArrayList<IntegerArray>();
    ArrayList<IntegerArray> rotate = new ArrayList<IntegerArray>();
    ArrayList<IntegerArray> tool = new ArrayList<IntegerArray>();
    //下面两个数组记录画面上10*6个几何体放置中心
    int x[] = new int[6];
    int y[] = new int[10];
    int picSize = 70;

    int round = 1;//回合
    boolean rover = true;//是否回合结束（即新回合是否开始），用于决定几何体上移

    boolean isBomb = false; //是否有炸弹待生效

    int timeMachine = 0;  //时光机的触发状态
    IntegerArray tm1_type = new IntegerArray();  //tm1、tm2记录时光机生效时的最后两行信息
    IntegerArray tm2_type = new IntegerArray();
    IntegerArray tm1_value= new IntegerArray();  //tm1、tm2记录时光机生效时的最后两行信息
    IntegerArray tm2_value = new IntegerArray();
    IntegerArray tm1_rotate = new IntegerArray();  //tm1、tm2记录时光机生效时的最后两行信息
    IntegerArray tm2_rotate = new IntegerArray();
    IntegerArray tm1_tool = new IntegerArray();  //tm1、tm2记录时光机生效时的最后两行信息
    IntegerArray tm2_tool = new IntegerArray();

    int numBall = 1; //球数
    int unShooted = 1; //待发射的球数
    int waitShoot = 20;  //每两个球发射之间的间隔 timeInterval*waitShoot (ms)
    int fallen = 0; //掉出画面外的球数
    boolean addBall = false;  //是否增加小球
    int power = 1; //碰撞一次的杀伤力
    int score = 0;//得分数

    public Bounce6(){
        this.setVisible(true);
        this.setLayout(new FlowLayout());
        this.setBorder(BorderFactory.createEtchedBorder());

        for(int i = 0; i < 6; i ++){
            x[i] = width/12 + i*width/6;
        }  //所有可能的几何体放置位置中心的x坐标
        for(int i = 0; i < 10; i ++){
            y[i] = 100 + (height-100)/18 + i*(height-100)/9;
        }  //所有可能的几何体放置位置中心的y坐标


        ballx.add(width/2-r);
        bally.add(0);
        cosv.add(0.0);
        sinv.add(0.0);
        moving.add(false);

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mousex = e.getX();
                mousey = e.getY();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousex = e.getX();
                mousey = e.getY();
            } //获取鼠标移动到的位置
        });

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!isMoving){
                    mousex = e.getX();
                    mousey = e.getY();
                    isMoving = true;
                    v = v0; //小球移动速率为v/timeInterval
                    int tempx = mousex-(ballx0+r);
                    int tempy = mousey-(bally0+r);
                    double distance = Math.sqrt(Math.pow(tempx,2)+Math.pow(tempy,2));
                    double tempcosv = v*tempx/distance;
                    double tempsinv = v*tempy/distance;
                    for(int i=0; i < numBall; i ++){
                        cosv.set(i,tempcosv);
                        sinv.set(i,tempsinv);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if(!isMoving){
                    mousex = e.getX();
                    mousey = e.getY();
                    isMoving = true;
                    v = v0; //小球移动速率为v/timeInterval
                    int tempx = mousex-(ballx0+r);
                    int tempy = mousey-(bally0+r);
                    double distance = Math.sqrt(Math.pow(tempx,2)+Math.pow(tempy,2));
                    double tempcosv = v*tempx/distance;
                    double tempsinv = v*tempy/distance;
                    for(int i=0; i < numBall; i ++){
                        cosv.set(i,tempcosv);
                        sinv.set(i,tempsinv);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mousex = e.getX();
                mousey = e.getY();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                mousex = e.getX();
                mousey = e.getY();
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

    }

    public void paintComponent(Graphics g0){
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D)g0;
        g.setColor(Color.black);
        g.drawRect(0,0,600,1000);

        if(type.size()>9){
            g.setFont(new Font("Serif", 1, 80));
            g.drawString("GAME OVER!",50,400);
        }
        else{
            g.setColor(Color.RED);
            g.setFont(new Font("Serif", 1, 30));
            g.drawString("得分："+Integer.toString(score), 430, 80);
            g.drawString("球数："+Integer.toString(numBall), 430, 130);
            g.drawString("球力："+Integer.toString(power), 430, 180);
            if(isBomb == true){
                g.drawString("获得炸弹！", 430, 230);
            }
            else{
                if(timeMachine > 0){
                    g.drawString("获得时光机", 430, 230);
                }
            }

            drawShapes(g); //画几何体目前所处的位置

            if(unShooted > 0){
                int balli = numBall-unShooted;
                if(balli == 0){
                    ballx.set(balli,(int)(ballx0+cosv.get(balli)));
                    bally.set(balli,(int)(bally0+sinv.get(balli)));
                    if((ballx.get(balli)!=ballx0)||(bally.get(balli)!=bally0)){
                        unShooted --;
                        moving.set(balli,true);
                    }
                }
                else{
                    waitShoot --;
                    if(waitShoot == 0){
                        ballx.set(balli,(int)(ballx0+cosv.get(balli)));
                        bally.set(balli,(int)(bally0+sinv.get(balli)));
                        unShooted --;
                        moving.set(balli,true);
                        waitShoot = 20;
                    }
                }
            }  //有未发射的小球，则逐一发射
            else{
                for(int i=0; i<numBall; i ++){
                    ballx.set(i,(int)(ballx.get(i)+cosv.get(i)));
                    bally.set(i,(int)(bally.get(i)+sinv.get(i)));
                }
            }  //若小球已全部发射，则根据运动情况更改每个小球的位置
            Image ball = Toolkit.getDefaultToolkit().getImage("img/ball.png");
            for(int i=0; i<numBall; i ++){
                g.drawImage(ball,ballx.get(i),bally.get(i),2*r,2*r,this);
            }  //逐个画球

            //画发射引导线
            if(isMoving == false){
                //虚线风格设置
                g.setColor(Color.RED);
                float[] dash1 = { 2f, 0f, 2f };
                BasicStroke bs1 = new BasicStroke(5,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_ROUND,
                        1.0f,
                        dash1,
                        2f);
                g.setStroke(bs1);
                g.drawLine(ballx0+r,bally0+r,mousex,mousey);
            }

            //若未落地，判断与墙壁或几何体碰撞，改变运动方向；否则落地……
            for(int k=0; k<numBall; k ++){
                if(bally.get(k)+2*r < height){
                    if(ballx.get(k) <= 0){
                        cosv.set(k,Math.abs(cosv.get(k)));
                    }
                    if(ballx.get(k)+2*r >= 0+width){
                        cosv.set(k,-Math.abs(cosv.get(k)));
                    }
                    if(bally.get(k) <= 0){
                        sinv.set(k,Math.abs(sinv.get(k)));
                    }

                    ShapeBounce(k); //和几何体碰撞反弹

                    if(moving.get(k) == true){
                        sinv.set(k,sinv.get(k)+a);
                    }
                }
                else{
                    fallen ++;
                    cosv.set(k,0.0);
                    sinv.set(k,0.0);
                    ballx.set(k,ballx0);
                    bally.set(k,bally0);
                    moving.set(k,false);
                }//判断落地
            }
            if(fallen == numBall){
                round ++;
                rover = true;
                isMoving = false;
                fallen = 0;
                unShooted = numBall;
                v = 0;
            }  //所有球都已落地，则开始下一回合
        }
    }

    public void drawShapes(Graphics2D g){
        if(rover == true){
            //生成四行随机数数组，分别代表新生成一行几何体的类型、分值、旋转角度、工具类型
            if(isBomb == true){
                int numRows = type.size();
                for(int k=0; k<numRows; k ++){
                    type.remove(0);
                    value.remove(0);
                    rotate.remove(0);
                    tool.remove(0);
                    isBomb = false;
                }

                //炸弹生效时，时光机同时就会失效
                timeMachine = 0;
                tm1_type.clear();
                tm1_value.clear();
                tm1_rotate.clear();
                tm1_tool.clear();
                tm2_type.clear();
                tm2_value.clear();
                tm2_rotate.clear();
                tm2_tool.clear();
            }  //有炸弹则将几何体清空
            if(timeMachine == 3){
                //将最后两行几何体信息记录，然后删去
                int oldsize = type.size();
                if(oldsize <= 2){
                    timeMachine = 0;
                }
                else{
                    for(int k=0; k<6; k++){  //记录要被时光机退回的两行几何体全部信息
                        tm1_type.add(type.get(oldsize-2).toIntArray()[k]);
                        tm2_type.add(type.get(oldsize-1).toIntArray()[k]);
                        tm1_value.add(value.get(oldsize-2).toIntArray()[k]);
                        tm2_value.add(value.get(oldsize-1).toIntArray()[k]);
                        tm1_rotate.add(rotate.get(oldsize-2).toIntArray()[k]);
                        tm2_rotate.add(rotate.get(oldsize-1).toIntArray()[k]);
                        tm1_tool.add(tool.get(oldsize-2).toIntArray()[k]);
                        tm2_tool.add(tool.get(oldsize-1).toIntArray()[k]);
                    }
                    type.remove(oldsize-1);
                    value.remove(oldsize-1);
                    rotate.remove(oldsize-1);
                    tool.remove(oldsize-1);
                    type.remove(oldsize-2);
                    value.remove(oldsize-2);
                    rotate.remove(oldsize-2);
                    tool.remove(oldsize-2);
                    timeMachine --;
                }
            }  //若是时光机生效后的第1轮，则将最后两行几何体信息记录后清除
            else if(timeMachine == 2){
                IntegerArray newType = new IntegerArray();
                IntegerArray newValue = new IntegerArray();
                IntegerArray newRotate = new IntegerArray();
                IntegerArray newTool = new IntegerArray();
                for(int i=0; i<6; i++){
                    newType.add(tm1_type.toIntArray()[i]);
                    newValue.add(tm1_value.toIntArray()[i]);
                    newRotate.add(tm1_rotate.toIntArray()[i]);
                    newTool.add(tm1_tool.toIntArray()[i]);
                }
                type.add(newType);
                value.add(newValue);
                rotate.add(newRotate);
                tool.add(newTool);
                timeMachine --;

                tm1_type.clear();
                tm1_value.clear();
                tm1_rotate.clear();
                tm1_tool.clear();
            }  //若是时光机生效后的第2轮，则将tm1几何体生成，并清空tm1信息
            else if(timeMachine == 1){
                IntegerArray newType = new IntegerArray();
                IntegerArray newValue = new IntegerArray();
                IntegerArray newRotate = new IntegerArray();
                IntegerArray newTool = new IntegerArray();
                for(int i=0; i<6; i++){
                    newType.add(tm2_type.toIntArray()[i]);
                    newValue.add(tm2_value.toIntArray()[i]);
                    newRotate.add(tm2_rotate.toIntArray()[i]);
                    newTool.add(tm2_tool.toIntArray()[i]);
                }
                type.add(newType);
                value.add(newValue);
                rotate.add(newRotate);
                tool.add(newTool);
                timeMachine --;

                tm2_type.clear();
                tm2_value.clear();
                tm2_rotate.clear();
                tm2_tool.clear();
            }  //若是时光机生效后的第2轮，则将tm2几何体生成，并清空tm2信息
            else{
                IntegerArray newType = new IntegerArray();
                for(int i = 0; i < 6; i ++){
                    int temp = (int)(Math.random()*numType);  //随机生成类型
                    newType.add(temp);
                }
                type.add(newType);

                IntegerArray newValue = new IntegerArray();
                for(int i = 0; i < 6; i ++){
                    int temp = (int)(2+Math.random()*round*15);  //随机生成分值
                    newValue.add(temp);
                }
                value.add(newValue);

                IntegerArray newRotate = new IntegerArray();
                for(int i = 0; i < 6; i ++){
                    int temp = (int)(Math.random()*360);  //随机生成角度
                    newRotate.add(temp);
                }
                rotate.add(newRotate);

                IntegerArray newTool = new IntegerArray();
                for(int i = 0; i < 6; i ++){
                    int temp = (int)(Math.random()*numTool);  //随机生成角度
                    newTool.add(temp);
                }
                tool.add(newTool);
            }  //没有生效的时光机，则正常产生新几何体

            if(addBall == true){
                numBall ++;
                unShooted = numBall;
                ballx.add(ballx0);
                bally.add(bally0);
                cosv.add(0.0);
                sinv.add(0.0);
                moving.add(false);
                addBall = false;
            }  //如果有加球道具生效，则增加一个球放在待发射处

            rover = false;
        }

        //根据几何体类型、分值、旋转角度进行绘制
        for(int i = type.size()-1; i >= 0; i --){
            for(int j = 0; j < 6; j ++){
                if((type.get(i)).toIntArray()[j] == 1){  //圆
                    g.setColor(Color.black);
                    BasicStroke bs2 = new BasicStroke(3);
                    g.setStroke(bs2);
                    g.drawOval(x[j]-picSize/2,y[9-type.size()+i]-picSize/2,70,70);

                    g.setColor(Color.RED); //显示的分值文字颜色
                    g.setFont(new Font("Serif", 1, 40));
                    g.drawString(Integer.toString((value.get(i)).toIntArray()[j]), x[j]-20, y[9-type.size()+i]+20);
                }
                if((type.get(i)).toIntArray()[j] == 2){  //正方
                    g.setColor(Color.black);
                    BasicStroke bs2 = new BasicStroke(3);
                    g.setStroke(bs2);
                    AffineTransform trans = new AffineTransform(); //准备旋转变换
                    int angle = (rotate.get(i)).toIntArray()[j]; //旋转角度
                    trans.rotate(angle*Math.PI/180.0,x[j],y[9-type.size()+i]); //旋转变换设置
                    g.setTransform(trans); //画笔设为旋转模式
                    g.drawRect(x[j]-picSize/2,y[9-type.size()+i]-picSize/2,70,70); //画出旋转的图形
                    trans.rotate(-angle*Math.PI/180.0,x[j],y[9-type.size()+i]); //旋转变换设置
                    g.setTransform(trans); //画笔恢复正常模式

                    g.setColor(Color.RED);
                    g.setFont(new Font("Serif", 1, 40));
                    g.drawString(Integer.toString((value.get(i)).toIntArray()[j]), x[j]-20, y[9-type.size()+i]+20);
                }
                if((type.get(i)).toIntArray()[j] == 3){  //三角
                    Image image = Toolkit.getDefaultToolkit().getImage("img/3-triangle.png");
                    AffineTransform trans = new AffineTransform();
                    int angle = (rotate.get(i)).toIntArray()[j];
                    trans.rotate(angle*Math.PI/180.0,x[j],y[9-type.size()+i]);
                    g.setTransform(trans);
                    g.drawImage(image,x[j]-picSize/2,y[9-type.size()+i]-picSize/2,70,70,this);
                    trans.rotate(-angle*Math.PI/180.0,x[j],y[9-type.size()+i]); //旋转变换设置
                    g.setTransform(trans); //画笔恢复正常模式

                    g.setColor(Color.RED);
                    g.setFont(new Font("Serif", 1, 40));
                    g.drawString(Integer.toString((value.get(i)).toIntArray()[j]), x[j]-20, y[9-type.size()+i]+20);
                }
                if((type.get(i)).toIntArray()[j] == 4){
                    if(tool.get(i).toIntArray()[j] == 0){
                        Image t0 = Toolkit.getDefaultToolkit().getImage("img/bomb.png");
                        g.drawImage(t0,x[j]-picSize/2,y[9-type.size()+i]-picSize/2,70,70,this);
                    }
                    if(tool.get(i).toIntArray()[j] == 1){
                        if(type.size()>=3){
                            Image t1 = Toolkit.getDefaultToolkit().getImage("img/timemachine.png");
                            g.drawImage(t1,x[j]-picSize/2,y[9-type.size()+i]-picSize/2,70,70,this);
                        }
                        else{
                            type.get(i).set(j,0);
                        }
                    }
                    if(tool.get(i).toIntArray()[j] == 2){
                        Image t2 = Toolkit.getDefaultToolkit().getImage("img/addball.png");
                        g.drawImage(t2,x[j]-picSize/2,y[9-type.size()+i]-picSize/2,70,70,this);
                    }
                    if(tool.get(i).toIntArray()[j] == 3){
                        Image t3 = Toolkit.getDefaultToolkit().getImage("img/inhance.png");
                        g.drawImage(t3,x[j]-picSize/2,y[9-type.size()+i]-picSize/2,70,70,this);
                    }
                }
            }
        }
    }

    public void ShapeBounce(int b){
        int pot_yFill = type.size();
        double row = ((double)(bally.get(b)+2*r))/((height-100)/9);
        double col = ((double)(ballx.get(b)+r))/(width/6);
        if(row>=10-pot_yFill){  //进入可能与下方碰撞的区域（有图形）,找左、右下方的图形
            int i,j;
            i = (int)(row-(10-pot_yFill));
            for(j = (int)col; j <= Math.min(5,(int)col+1); j ++){

                if(type.get(i).toIntArray()[j] == 1){  //进入圆的周边区域
                    double distance = Math.sqrt(Math.pow(x[j]-(ballx.get(b)+r),2)+Math.pow(y[9-type.size()+i]-(bally.get(b)+r),2));
                    double sinbo = (y[9-type.size()+i]-(bally.get(b)+r))/distance;  //圆心连线的水平夹角正弦
                    double cosbo = (x[j]-(ballx.get(b)+r))/distance;  //圆心连线的水平夹角余弦
                    if(distance <= r+35){ //圆心距小于半径之和，碰撞反弹
                        double vr = v*(sinv.get(b)*sinbo+cosv.get(b)*cosbo);
                        double vt = v*(cosv.get(b)*sinbo-sinv.get(b)*cosbo);
                        sinv.set(b,(-vt*cosbo-vr*sinbo)/v);  //改变运动轨迹方向
                        cosv.set(b,(vt*sinbo-vr*cosbo)/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                }
                if(type.get(i).toIntArray()[j] == 2){  //进入正方形的周边区域
                    double distance = Math.sqrt(Math.pow(x[j]-(ballx.get(b)+r),2)+Math.pow(y[9-type.size()+i]-(bally.get(b)+r),2));  //球心与形心连线
                    double sinbo = (y[9-type.size()+i]-(bally.get(b)+r))/distance;  //形心连线的水平夹角正弦
                    double cosbo = (x[j]-(ballx.get(b)+r))/distance;  //形心连线的水平夹角余弦
                    double alpha = (rotate.get(i).toIntArray()[j]%90)*Math.PI/180;  //正方形旋转角度，0-pi/2之间
                    double beta;
                    if(sinbo==0){
                        if(cosbo<0){
                            beta = -Math.PI/2;
                        }
                        else{
                            beta = Math.PI/2;
                        }
                    }
                    else{
                        beta = Math.atan(cosbo/sinbo);  //形心连线竖直方向夹角
                    }
                    double gamma = alpha-beta;  //形心连线与正方形参考线（从竖直顺时针旋转alpha）的夹角
                    double dist1 = Math.abs(Math.sin(gamma)*distance);  //球心到1边距离
                    double dist2 = Math.abs(Math.cos(gamma)*distance);  //球心到2边距离
                    if((dist1 <= 35)&&(dist2 <= 35+r)){ //到了1、3边的接触区，碰撞反弹
                        double vr = v*(sinv.get(b)*Math.cos(alpha)-cosv.get(b)*Math.sin(alpha));
                        double vt = v*(sinv.get(b)*Math.sin(alpha)+cosv.get(b)*Math.cos(alpha));
                        sinv.set(b,(vt*Math.sin(alpha)-vr*Math.cos(alpha))/v);  //改变运动轨迹方向
                        cosv.set(b,(vt*Math.cos(alpha)+vr*Math.sin(alpha))/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                    if((dist2 <= 35)&&(dist1 <= 35+r)){ //到了2、4边的接触区，碰撞反弹
                        double vt = v*(sinv.get(b)*Math.cos(alpha)-cosv.get(b)*Math.sin(alpha));
                        double vr = v*(sinv.get(b)*Math.sin(alpha)+cosv.get(b)*Math.cos(alpha));
                        sinv.set(b,(vt*Math.cos(alpha)-vr*Math.sin(alpha))/v);  //改变运动轨迹方向
                        cosv.set(b,(-vt*Math.sin(alpha)-vr*Math.cos(alpha))/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                }
                if(type.get(i).toIntArray()[j] == 3){  //进入三角形的周边区域
                    double distance = Math.sqrt(Math.pow(x[j]-(ballx.get(b)+r),2)+Math.pow(y[9-type.size()+i]-(bally.get(b)+r),2));  //球心与形心连线
                    double sinbo = (y[9-type.size()+i]-(bally.get(b)+r))/distance;  //形心连线的水平夹角正弦
                    double cosbo = (x[j]-(ballx.get(b)+r))/distance;  //形心连线的水平夹角余弦
                    double alpha = (rotate.get(i).toIntArray()[j]%120)*Math.PI/180;  //正方形旋转角度，0-120°之间
                    double beta;  //形心连线竖直方向夹角
                    if(sinbo==0){  //直角
                        if(cosbo<0){
                            beta = -Math.PI/2;
                        }
                        else{
                            beta = Math.PI/2;
                        }
                    }
                    else if(sinbo>0){  //-90至+90°
                        beta = Math.atan(cosbo/sinbo);
                    }
                    else{  //小于-90或大于+90
                        beta = Math.atan(cosbo/sinbo)*(Math.PI/Math.abs(Math.atan(cosbo/sinbo))-1);
                    }
                    double gamma1 = beta+alpha-Math.PI/6;  //形心连线与1边参考线（过形心与1边平行）的夹角
                    double gamma2 = beta+alpha+Math.PI/6;  //形心连线与2边参考线（过形心与2边平行）的夹角
                    double gamma3 = beta+alpha+Math.PI/2;  //形心连线与3边参考线（过形心与3边平行）的夹角
                    double dist1 = Math.abs(Math.sin(gamma1)*distance);  //球心到1边参考线距离
                    double dist2 = Math.abs(Math.sin(gamma2)*distance);  //球心到2边参考线距离
                    double dist3 = Math.abs(Math.sin(gamma3)*distance);  //球心到2边参考线距离
                    if((dist2 <= 70/3.0)&&(dist3 <= 70/3.0)){ //到了1边的接触区，碰撞反弹
                        double theta = alpha+Math.PI/6;  //xy坐标与1边rt坐标的旋转角度
                        double vt = v*(sinv.get(b)*Math.cos(theta)-cosv.get(b)*Math.sin(theta));
                        double vr = v*(sinv.get(b)*Math.sin(theta)+cosv.get(b)*Math.cos(theta));
                        sinv.set(b,(vt*Math.cos(theta)-vr*Math.sin(theta))/v);  //改变运动轨迹方向
                        cosv.set(b,(-vt*Math.sin(theta)-vr*Math.cos(theta))/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                    if((dist1 <= 70/3.0)&&(dist3 <= 70/3.0)){ //到了2边的接触区，碰撞反弹
                        double theta = alpha-Math.PI/6;  //xy坐标与2边rt坐标的旋转角度
                        double vt = v*(sinv.get(b)*Math.cos(theta)-cosv.get(b)*Math.sin(theta));
                        double vr = v*(sinv.get(b)*Math.sin(theta)+cosv.get(b)*Math.cos(theta));
                        sinv.set(b,(vt*Math.cos(theta)-vr*Math.sin(theta))/v);  //改变运动轨迹方向
                        cosv.set(b,(-vt*Math.sin(theta)-vr*Math.cos(theta))/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                    if((dist1 <= 70/3.0)&&(dist2 <= 70/3.0)){ //到了3边的接触区，碰撞反弹
                        double theta = alpha+Math.PI/2;  //xy坐标与3边rt坐标的旋转角度
                        double vt = v*(sinv.get(b)*Math.cos(theta)-cosv.get(b)*Math.sin(theta));
                        double vr = v*(sinv.get(b)*Math.sin(theta)+cosv.get(b)*Math.cos(theta));
                        sinv.set(b,(vt*Math.cos(theta)-vr*Math.sin(theta))/v);  //改变运动轨迹方向
                        cosv.set(b,(-vt*Math.sin(theta)-vr*Math.cos(theta))/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                }
                if(type.get(i).toIntArray()[j] == 4){
                    double distance = Math.sqrt(Math.pow(x[j]-(ballx.get(b)+r),2)+Math.pow(y[9-type.size()+i]-(bally.get(b)+r),2));
                    if(distance <= r+35){
                        type.get(i).set(j,0);  //碰到道具，则道具消失

                        if(tool.get(i).toIntArray()[j] == 0){
                            isBomb = true;
                        }  //若是炸弹，则标记，下回合开始时清空几何体
                        if(tool.get(i).toIntArray()[j] == 1){
                            if(isBomb == false){
                                timeMachine = 3;
                            }  //只有没炸弹即将生效时才会触发时光机
                        }  //若是时光机，则进行标记，并在下一回合开始时，将最后两行几何体记录后把它们清除，并在几何体绘制部分做修改
                        if(tool.get(i).toIntArray()[j] == 2){
                            addBall = true;
                        }  //若是小球增加，则记录后在下回合开始时加球
                        if(tool.get(i).toIntArray()[j] == 3){
                            power ++;
                        }  //若是小球增强，则power+1
                    }
                }
            }
        }
        if(row>=11-pot_yFill){  //进入可能与上方碰撞的区域（有图形）,找左、右上方的图形
            int i,j;
            i = (int)(row-(11-pot_yFill));
            for(j = (int)col; j <= Math.min(5,(int)col+1); j ++){

                if(type.get(i).toIntArray()[j] == 1){  //进入圆的周边区域
                    double distance = Math.sqrt(Math.pow(x[j]-(ballx.get(b)+r),2)+Math.pow(y[9-type.size()+i]-(bally.get(b)+r),2));
                    double sinbo = (y[9-type.size()+i]-(bally.get(b)+r))/distance;  //圆心连线的水平夹角正弦
                    double cosbo = (x[j]-(ballx.get(b)+r))/distance;  //圆心连线的水平夹角余弦
                    if(distance <= r+35){ //圆心距小于半径之和，碰撞反弹
                        double vr = v*(sinv.get(b)*sinbo+cosv.get(b)*cosbo);
                        double vt = v*(cosv.get(b)*sinbo-sinv.get(b)*cosbo);
                        sinv.set(b,(-vt*cosbo-vr*sinbo)/v);  //改变运动轨迹方向
                        cosv.set(b,(vt*sinbo-vr*cosbo)/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                }
                if(type.get(i).toIntArray()[j] == 2){  //进入正方形的周边区域
                    double distance = Math.sqrt(Math.pow(x[j]-(ballx.get(b)+r),2)+Math.pow(y[9-type.size()+i]-(bally.get(b)+r),2));  //球心与形心连线
                    double sinbo = (y[9-type.size()+i]-(bally.get(b)+r))/distance;  //形心连线的水平夹角正弦
                    double cosbo = (x[j]-(ballx.get(b)+r))/distance;  //形心连线的水平夹角余弦
                    double alpha = (rotate.get(i).toIntArray()[j]%90)*Math.PI/180;  //正方形旋转角度，0-pi/2之间
                    double beta;
                    if(sinbo==0){
                        if(cosbo<0){
                            beta = -Math.PI/2;
                        }
                        else{
                            beta = Math.PI/2;
                        }
                    }
                    else{
                        beta = Math.atan(cosbo/sinbo);  //形心连线竖直方向夹角
                    }
                    double gamma = alpha-beta;  //形心连线与正方形参考线（从竖直顺时针旋转alpha）的夹角
                    double dist1 = Math.abs(Math.sin(gamma)*distance);  //球心到1边距离
                    double dist2 = Math.abs(Math.cos(gamma)*distance);  //球心到2边距离
                    if((dist1 <= 35)&&(dist2 <= 35+r)){ //到了1、3边的接触区，碰撞反弹
                        double vr = v*(sinv.get(b)*Math.cos(alpha)-cosv.get(b)*Math.sin(alpha));
                        double vt = v*(sinv.get(b)*Math.sin(alpha)+cosv.get(b)*Math.cos(alpha));
                        sinv.set(b,(vt*Math.sin(alpha)-vr*Math.cos(alpha))/v);  //改变运动轨迹方向
                        cosv.set(b,(vt*Math.cos(alpha)+vr*Math.sin(alpha))/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                    if((dist2 <= 35)&&(dist1 <= 35+r)){ //到了2、4边的接触区，碰撞反弹
                        double vt = v*(sinv.get(b)*Math.cos(alpha)-cosv.get(b)*Math.sin(alpha));
                        double vr = v*(sinv.get(b)*Math.sin(alpha)+cosv.get(b)*Math.cos(alpha));
                        sinv.set(b,(vt*Math.cos(alpha)-vr*Math.sin(alpha))/v);  //改变运动轨迹方向
                        cosv.set(b,(-vt*Math.sin(alpha)-vr*Math.cos(alpha))/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                }
                if(type.get(i).toIntArray()[j] == 3){  //进入三角形的周边区域
                    double distance = Math.sqrt(Math.pow(x[j]-(ballx.get(b)+r),2)+Math.pow(y[9-type.size()+i]-(bally.get(b)+r),2));  //球心与形心连线
                    double sinbo = (y[9-type.size()+i]-(bally.get(b)+r))/distance;  //形心连线的水平夹角正弦
                    double cosbo = (x[j]-(ballx.get(b)+r))/distance;  //形心连线的水平夹角余弦
                    double alpha = (rotate.get(i).toIntArray()[j]%120)*Math.PI/180;  //正方形旋转角度，0-120°之间
                    double beta;  //形心连线竖直方向夹角
                    if(sinbo==0){  //直角
                        if(cosbo<0){
                            beta = -Math.PI/2;
                        }
                        else{
                            beta = Math.PI/2;
                        }
                    }
                    else if(sinbo>0){  //-90至+90°
                        beta = Math.atan(cosbo/sinbo);
                    }
                    else{  //小于-90或大于+90
                        beta = Math.atan(cosbo/sinbo)*(Math.PI/Math.abs(Math.atan(cosbo/sinbo))-1);
                    }
                    double gamma1 = beta+alpha-Math.PI/6;  //形心连线与1边参考线（过形心与1边平行）的夹角
                    double gamma2 = beta+alpha+Math.PI/6;  //形心连线与2边参考线（过形心与2边平行）的夹角
                    double gamma3 = beta+alpha+Math.PI/2;  //形心连线与3边参考线（过形心与3边平行）的夹角
                    double dist1 = Math.abs(Math.sin(gamma1)*distance);  //球心到1边参考线距离
                    double dist2 = Math.abs(Math.sin(gamma2)*distance);  //球心到2边参考线距离
                    double dist3 = Math.abs(Math.sin(gamma3)*distance);  //球心到2边参考线距离
                    if((dist2 <= 70/3.0)&&(dist3 <= 70/3.0)){ //到了1边的接触区，碰撞反弹
                        double theta = alpha+Math.PI/6;  //xy坐标与1边rt坐标的旋转角度
                        double vt = v*(sinv.get(b)*Math.cos(theta)-cosv.get(b)*Math.sin(theta));
                        double vr = v*(sinv.get(b)*Math.sin(theta)+cosv.get(b)*Math.cos(theta));
                        sinv.set(b,(vt*Math.cos(theta)-vr*Math.sin(theta))/v);  //改变运动轨迹方向
                        cosv.set(b,(-vt*Math.sin(theta)-vr*Math.cos(theta))/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                    if((dist1 <= 70/3.0)&&(dist3 <= 70/3.0)){ //到了2边的接触区，碰撞反弹
                        double theta = alpha-Math.PI/6;  //xy坐标与2边rt坐标的旋转角度
                        double vt = v*(sinv.get(b)*Math.cos(theta)-cosv.get(b)*Math.sin(theta));
                        double vr = v*(sinv.get(b)*Math.sin(theta)+cosv.get(b)*Math.cos(theta));
                        sinv.set(b,(vt*Math.cos(theta)-vr*Math.sin(theta))/v);  //改变运动轨迹方向
                        cosv.set(b,(-vt*Math.sin(theta)-vr*Math.cos(theta))/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                    if((dist1 <= 70/3.0)&&(dist2 <= 70/3.0)){ //到了3边的接触区，碰撞反弹
                        double theta = alpha+Math.PI/2;  //xy坐标与3边rt坐标的旋转角度
                        double vt = v*(sinv.get(b)*Math.cos(theta)-cosv.get(b)*Math.sin(theta));
                        double vr = v*(sinv.get(b)*Math.sin(theta)+cosv.get(b)*Math.cos(theta));
                        sinv.set(b,(vt*Math.cos(theta)-vr*Math.sin(theta))/v);  //改变运动轨迹方向
                        cosv.set(b,(-vt*Math.sin(theta)-vr*Math.cos(theta))/v);
                        int oldvalue = value.get(i).toIntArray()[j];
                        value.get(i).set(j,Math.max(0,oldvalue-power));  //图形包含分数减power
                        score += oldvalue - value.get(i).toIntArray()[j];  //得分增加
                        if(value.get(i).toIntArray()[j]==0){
                            type.get(i).set(j,0);
                        }
                    }
                }
                if(type.get(i).toIntArray()[j] == 4){
                    double distance = Math.sqrt(Math.pow(x[j]-(ballx.get(b)+r),2)+Math.pow(y[9-type.size()+i]-(bally.get(b)+r),2));
                    if(distance <= r+35){
                        type.get(i).set(j,0);  //碰到道具，则道具消失

                        if(tool.get(i).toIntArray()[j] == 0){
                            isBomb = true;
                        }  //若是炸弹，则标记，下回合开始时清空几何体
                        if(tool.get(i).toIntArray()[j] == 1){
                            if(isBomb == false){
                                timeMachine = 3;
                            }  //只有没炸弹即将生效时才会触发时光机
                        }  //若是时光机，则进行标记，并在下一回合开始时，将最后两行几何体记录后把它们清除，并在几何体绘制部分做修改
                        if(tool.get(i).toIntArray()[j] == 2){
                            addBall = true;
                        }  //若是小球增加，则记录后在下回合开始时加球
                        if(tool.get(i).toIntArray()[j] == 3){
                            power ++;
                        }  //若是小球增强，则power+1
                    }
                }
            }
        }

        if(type.size()>0){
            int pd = 0;
            for(int i=0; i < 6; i ++){
                pd += type.get(0).toIntArray()[i];
            }
            if(pd == 0){
                type.remove(0);
                value.remove(0);
                rotate.remove(0);
                tool.remove(0);
            }
        } //如果图形顶行已经清空，则从图形记录中删除
    }
}