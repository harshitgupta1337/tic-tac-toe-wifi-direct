package org.wifi.p2p.tictactoe.tictactoe2;

/**
 * Created by hp on 10-09-2015.
 */
public class Singleton {

    private boolean locked = false;

    private static Singleton instance;
    public static Singleton getInstance(){
        if(instance == null){
            instance = new Singleton();
        }
        return instance;
    }

    private Singleton(){

    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
