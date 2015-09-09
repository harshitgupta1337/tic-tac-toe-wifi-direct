package org.wifi.p2p.tictactoe.tictactoe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class GameActivity extends Activity {

    private static int EMPTY = 0;
    private static int OWNER = 1;
    private static int MEMBER = 2;
    private ChatConnection gameConnection;
    private Handler mUpdateHandler;
    boolean gameOn = true;
    int winStates[][] = {{0,1,2}, {3,4,5}, {6,7,8}, {0,3,6}, {1,4,7}, {2,5,8}, {0,4,8}, {2,4,6}};
    int arr[][] = {{0,0,0},{0,0,0},{0,0,0}};
    Map<Integer, Button> buttonMap = new HashMap<Integer, Button>();
    boolean isOwner;
    String mark;
    boolean myTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String msgStr = msg.getData().getString("msg");
                modifyGame(msgStr);
                Toast.makeText(GameActivity.this, "MSG : "+msgStr,
                        Toast.LENGTH_LONG).show();

            }
        };
        gameConnection = new ChatConnection(mUpdateHandler, this);


        createOnClickListeners();
        Intent intent = getIntent();
        isOwner = intent.getBooleanExtra(getResources().getString(R.string.is_owner), false);

        initializeGame();

        mark = (isOwner)?"X":"O";
        if(!isOwner){
            String ownerAddress = intent.getStringExtra(getResources().getString(R.string.owner_ip));
            try {
                Toast.makeText(GameActivity.this, InetAddress.getByName(ownerAddress).toString(), Toast.LENGTH_SHORT).show();
                gameConnection.connectToServer(InetAddress.getByName(ownerAddress), GameConnection.PORT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void initializeGame() {
        if(isOwner)
            myTurn = true;
        else
            myTurn = false;

        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                arr[i][j] = 0;
            }
        }
        for(Integer i : buttonMap.keySet()){
            buttonMap.get(i).setText("");
        }

        gameOn = true;
    }

    private void modifyGame(final String gameState){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject reader = new JSONObject(gameState);
                    for(int k=0;k<9;k++){
                        String id = Integer.toString(k);
                        int val = reader.getInt(id);
                        int i = k/3;
                        int j = k%3;
                        if(val == OWNER){
                            buttonMap.get(k).setText("X");
                            arr[i][j] = (isOwner)?1:-1;
                        }else if(val == MEMBER){
                            buttonMap.get(k).setText("O");
                            arr[i][j] = (isOwner)?-1:1;
                        }
                        Log.d("GameActivity", "Set text for button "+k);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(isGameCompleted())
                    gameOn = false;

                boolean youWin = checkForWinning(true);

                if (youWin) {
                    Toast.makeText(getApplicationContext(), "YOU WIN", Toast.LENGTH_LONG).show();
                    gameOn = false;
                }

                boolean youLose = checkForWinning(false);

                if (youLose) {
                    Toast.makeText(getApplicationContext(), "YOU LOSE", Toast.LENGTH_LONG).show();
                    gameOn = false;
                }

                myTurn = true;
            }
        });
    }

    public void handleMessage(String msg){
        try {
            JSONObject reader = new JSONObject(msg);
            String messageType = reader.getString("TYPE");
            if(messageType.equals("game_state")){
                modifyGame(msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isGameCompleted() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(arr[i][j] == 0)
                    return  false;
            }
        }
        return true;
    }

    private boolean checkForWinning(boolean player){

        int op = -1;
        if(player)
            op = 1;

        boolean interState = false;
        for(int i=0;i<8;i++){

            boolean intraState = true;
            for(int j=0;j<3;j++){

                int id = winStates[i][j];

                int x = id/3;
                int y = id%3;

                if(arr[x][y] != op)
                    intraState = false;
            }
            if(intraState) {
                interState = true;
                break;
            }
        }

        if(interState){
            return true;
        }
        return false;
    }

    private void sendGameState(){
        JSONObject gameState = new JSONObject();
        try {
            gameState.put("TYPE", "game_state");
            for(int k=0;k<9;k++){
                String id = Integer.toString(k);
                int i = k/3;
                int j = k%3;
                if(arr[i][j]==0)
                    gameState.put(id, EMPTY);
                else if(arr[i][j]==1)
                    gameState.put(id, (isOwner)?OWNER:MEMBER);
                else if(arr[i][j]==-1)
                    gameState.put(id, (isOwner)?MEMBER:OWNER);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("GameActivity", "JSON");
        }
        gameConnection.sendMessage(gameState.toString());
    }

    private void setOnClickListenerForButton(final Button button, final int id){
        buttonMap.put(id, button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (gameOn && myTurn) {
                    if (!button.getText().equals(""))
                        return;
                    button.setText(mark);

                    int j = id % 3;
                    int i = id / 3;
                    arr[i][j] = 1;

                    if(isGameCompleted())
                        gameOn = false;

                    boolean youWin = checkForWinning(true);

                    if (youWin) {
                        Toast.makeText(getApplicationContext(), "YOU WIN", Toast.LENGTH_LONG).show();
                        gameOn = false;
                    }

                    boolean youLose = checkForWinning(false);

                    if (youLose) {
                        Toast.makeText(getApplicationContext(), "YOU LOSE", Toast.LENGTH_LONG).show();
                        gameOn = false;
                    }
                }

                sendGameState();
                myTurn = false;

            }
        });

    }


    private void createOnClickListeners() {
        Button restartButton = (Button) findViewById(R.id.restartGameButton);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!gameOn)
                    initializeGame();
            }
        });
        Button button0 = (Button) findViewById(R.id.button0);
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button3 = (Button) findViewById(R.id.button3);
        Button button4 = (Button) findViewById(R.id.button4);
        Button button5 = (Button) findViewById(R.id.button5);
        Button button6 = (Button) findViewById(R.id.button6);
        Button button7 = (Button) findViewById(R.id.button7);
        Button button8 = (Button) findViewById(R.id.button8);
        setOnClickListenerForButton(button0, 0);
        setOnClickListenerForButton(button1, 1);
        setOnClickListenerForButton(button2, 2);
        setOnClickListenerForButton(button3, 3);
        setOnClickListenerForButton(button4, 4);
        setOnClickListenerForButton(button5, 5);
        setOnClickListenerForButton(button6, 6);
        setOnClickListenerForButton(button7, 7);
        setOnClickListenerForButton(button8, 8);

    }
}
