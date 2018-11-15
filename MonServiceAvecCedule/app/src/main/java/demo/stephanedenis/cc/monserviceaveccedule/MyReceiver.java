package demo.stephanedenis.cc.monserviceaveccedule;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("MyReceiver", "Démarrage du service par événement du système au Boot");
        context.startService(new Intent(context, MaJobService.class));
        scheduleJob(context);
    }

    public static void scheduleJob(Context c) {
        Log.i("MyReceiver", "scheduleJob");
        ComponentName serviceComponent = new ComponentName(c, MaJobService.class);

        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);

        builder.setMinimumLatency(5000);
        builder.setOverrideDeadline(10000);
        builder.setRequiresBatteryNotLow(true);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);

        JobScheduler jobScheduler = c.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }
}
