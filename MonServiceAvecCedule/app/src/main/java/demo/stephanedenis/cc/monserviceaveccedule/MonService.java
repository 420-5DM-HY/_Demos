package demo.stephanedenis.cc.monserviceaveccedule;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

// Pour offrir le service de communication IPC
// Le service est déclenché par les messages
public class MonService extends Service {

    MonServiceHandler monHandler;
    Messenger monMessenger;

    public MonService() {
        monHandler = new MonServiceHandler();
        monMessenger = new Messenger(monHandler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return monMessenger.getBinder();
    }

    class MonServiceHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    // Faire la vrai job sur le modèle ici, actuellement altère juste un champ statique
                    MaJobService.message =  msg.getData().getString("message");
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }
}
