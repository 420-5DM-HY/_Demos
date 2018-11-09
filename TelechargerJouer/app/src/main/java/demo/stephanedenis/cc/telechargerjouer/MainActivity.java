package demo.stephanedenis.cc.telechargerjouer;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import static android.os.Environment.getExternalStoragePublicDirectory;

/**
 * @author sdenis@cegepsth.qc.ca
 *
 * License : (CC by-nc-sa) http://creativecommons.org/licenses/by-nc-sa/4.0/
 */
@EActivity
public class MainActivity extends AppCompatActivity {

    String urlPathStr = "https://420-gep-hy.github.io/_Demos/TestData/"; //
    String fileName = "SillyIntro.mp3";
    String path = "MesPodcasts/";

    Long downloadReference;
    Uri uri;
    String mimeType;
    BroadcastReceiver receiver;

    @ViewById
    TextView texte;

    @ViewById
    Button telecharger;

    @ViewById
    Button jouer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // todo: vérifier si le fichier est disponible avant de désactiver le bouton, car l'application était peut-être en arrière-plan
        jouer.setEnabled(false);
    }

    @Click
    void telecharger() {
        // Endroit où enregistrer
        uri = Uri.fromFile(new File(getExternalStoragePublicDirectory (path).toPath()+"/"+ fileName));

        if (!checkPermissions(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE

        )) {
            message("Permissions insuffisantes pour procéder");
        } else {

            // Abonnement aux messages pour le suivi du téléchargement à venir
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id == downloadReference) {
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(downloadReference);
                        Cursor cursor = downloadManager.query(query);

                        if (cursor.moveToFirst()) {

                            switch (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                                case DownloadManager.STATUS_PAUSED: {
                                    message(getString(R.string.DownloadPaused));
                                    break;
                                }
                                case DownloadManager.STATUS_PENDING: {
                                    message(getString(R.string.DownloadPending));
                                    break;
                                }
                                case DownloadManager.STATUS_RUNNING: {
                                    message(getString(R.string.DownloadRunning));
                                    break;
                                }
                                case DownloadManager.STATUS_FAILED: {
                                    int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));

                                    // voir https://developer.android.com/reference/android/app/DownloadManager#COLUMN_REASON
                                    message("Erreur : " + reason); // 400 = https obligatoire, 404 = not found

                                    if (reason == DownloadManager.ERROR_CANNOT_RESUME || reason == DownloadManager.ERROR_UNKNOWN) {
                                        // Rerun download
                                    }
                                    break;
                                }
                                case DownloadManager.STATUS_SUCCESSFUL: {
                                    mimeType = downloadManager.getMimeTypeForDownloadedFile(downloadReference);
                                    message("Destination : " + uri + "\nType : " + mimeType);
                                    jouer.setEnabled(true);
                                    break;
                                }
                                default:
                                    Log.e("MainActivity","Statut inconnu pour l'état de téléchargement");
                                    break;
                            }
                        }

                    }
                }
            };

            // Prépare le téléchargement
            message(getString(R.string.LoadRequested));

            // Utilise le receiver qu'on vient de définir pour assurer un suivi du téléchargement
            registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlPathStr+fileName));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);

            // Affiche la petite icone de téléchargement dans la barre du haut
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Emplacement où mettre le fichier
            request.setDestinationUri(uri);

            Log.i(getClass().getName(),"Téléchargement de uri="+uri);

            // Lance la requête de téléchargement
            try {
                downloadReference = downloadManager.enqueue(request);
            } catch (SecurityException e) {
                message(getString(R.string.CheckSecSettings));
            } catch (IllegalStateException e) {
                message(getString(R.string.CheckSecSettings)+" : " + e.getMessage());
            }
        }
    }

    /**
     *  Ma méthode pour vérifier les permissions en lot (notez le "s" distinctif de checkPermission)
     *  permet de demander à l'utilisateur d'autoriser sur le champ les permissions manquantes
     *  en lot et valider ce lot.
     *
     * @param requestPerms permissions à valider/recouvrer
     * @return true, si tout est conforme, false si une permission a été refusée par utilisateur
     */
    boolean checkPermissions(String... requestPerms) {
        requestPermissions(requestPerms, 1);
        for (String perm : requestPerms) {
            if (checkSelfPermission(perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @UiThread
    void message(String msg) {
        texte.setText(msg);
    }

    @Click
    void jouer() {

        // On utilise un content provider afin d'être compatible aux nouvelles versions d'Android
        // Une configuration est nécessaire pour faire ça "simple"
        // Voir fichier provider_path.xml et la section provider du manifeste

        // "authority" doit correspondre à ce qui est dans le manifest
        String authority =BuildConfig.APPLICATION_ID + ".provider";
        try {
            if("file".equals(uri.getScheme()))
            // utilise content: au lieu de file: comme URI, nouvelle approche. file: est interdit maintenant!
            uri = FileProvider.getUriForFile(MainActivity.this, authority, new File(uri.getPath()));
        }
        catch(IllegalArgumentException e){
            Log.e(getClass().getName(),"Zut! "+e.getMessage());
        }

        Log.i(getClass().getName(),"Lancement de uri="+uri);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
