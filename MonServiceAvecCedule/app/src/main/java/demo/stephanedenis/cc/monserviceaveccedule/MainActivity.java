package demo.stephanedenis.cc.monserviceaveccedule;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button button;
    Messenger monMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Affichage en job récurrente
        Intent intent = new Intent(this, MaJobService.class);
        this.startService(intent);

        // Communication
        intent = new Intent(this, MonService.class);
        this.startService(intent);

        ServiceConnection sc = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                monMessenger = new Messenger(service);
                Log.i("MainActivity","Service connecté");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i("MainActivity","Service déconnecté");
            }
        };

        bindService(intent, sc, Context.BIND_AUTO_CREATE);




        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle b = new Bundle();
                b.putString("message", "Hey! Ça marche!");

                try {
                    Message m = new Message();
                    m.setData(b);
                    m.what=1;
                    monMessenger.send(m);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        MyReceiver.scheduleJob(this);
    }
}
