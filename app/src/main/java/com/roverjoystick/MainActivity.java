package com.roverjoystick;
import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;

import io.github.controlwear.virtual.joystick.android.JoystickView;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    Thread Thread1 = null;
    EditText etIP, etPort;
    TextView status_field;
    TextView etMessage;
    EditText gear_txt;
    String SERVER_IP;
    JoystickView joy;
    int SERVER_PORT;
    int joy_x, joy_y;
    int ogx, ogy;
    int gear = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        status_field = findViewById(R.id.status);
        joy = findViewById(R.id.joystick);
        etMessage = findViewById(R.id.etMessage);
        gear_txt = findViewById(R.id.gear);
        Button btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status_field.setText("");
                SERVER_IP = etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                Thread1 = new Thread(new Thread1());
                Thread1.start();
            }
        });

        joy.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                    gear = Integer.parseInt(gear_txt.getText().toString().trim());
                    joy_x = 80* (int) (strength * Math.cos(angle * (Math.PI / 180)));
                    joy_y = 80* (int) (strength * Math.sin(angle * (Math.PI / 180)));
                    joy_x = (8000 + joy_x);
                    joy_y = (joy_y + 8000);
                    ogx = joy_x;
                    ogy = joy_y;
                    new Thread(new Thread3(joy_x, joy_y)).start();
            }
        });
    }
    private PrintWriter output;
    private BufferedReader input;
    class Thread1 implements Runnable {
        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status_field.setText("Connected\n");
                    }
                });
                new Thread(new Thread2()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //tvMessages.append("server: " + message + "\n");
                                System.out.println("Message from server: " + message);
                            }
                        });
                    } else {
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Thread3 implements Runnable {
        private String message;
        Thread3(int x, int y) {
            //make the message equal to the masking

//            int gear_pack = (0b00001111 & gear);
//            gear_pack |= 0b00010000;
//
//            int x1 = 0b00001111 & (x >> 10);
//            x1 |= 0b00100000;
//
//            int x2 = 0b000011111 & (x >> 5);
//            x2 |= 0b01000000;
//
//            int x3 = 0b00000000011111 & (x >> 0);
//            x3 |= 0b01100000;
//
//            int y1 = 0b00001111 & (y >> 10);
//            y1 |= 0b10000000;
//
//            int y2 = 0b000011111 & (y >> 5);
//            y2 |= 0b10100000;
//
//            int y3 = 0b00000000011111 & (y >> 0);
//            y3 |= 0b11000000;

            this.message = "m" + gear + "s" + String.format(Locale.US, "%05d",x)  + "f" + String.format(Locale.US, "%05d",y) + "n" ;
        }

        @Override
        public void run() {
            output.write(message);
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    etMessage.setText("Sent X: " + joy_x + "\tY: " + joy_y + " Gear: " + gear + "\n" + message.getBytes());
                }
            });
        }
    }
}