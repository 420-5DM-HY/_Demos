package demo.stephanedenis.cc.monserviceaveccedule;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

// Utilise la nouvelle approche, exigée à partir d'Oreo
// https://developer.android.com/about/versions/oreo/background
//
// Cependant un JobService ne peux pas recevoir de messages car son cycle de vie est géré par le
// Scheduler
public class MaJobService extends JobService {

    // ici on référencerait un singleton du modèle
    static String message = "Pas de message encore";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("MaJobService", "onStartJob");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Relance la même job, donc roule en continue
        MyReceiver.scheduleJob(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i("MaJobService", "onStopJob");
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MaJobService", "onStartCommand");
        return START_NOT_STICKY;  // on ne demande pas de reprise puisqu'on relance périodiquement.
    }
}
