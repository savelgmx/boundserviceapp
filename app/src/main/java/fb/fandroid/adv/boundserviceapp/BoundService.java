package fb.fandroid.adv.boundserviceapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

/**
 * Created by andrew on 23.06.2018.
 * andrew s, [11.07.18 12:55]
 И что с ним надо сделать чтобы он запускал инкремент переменной счетчика каждые 2 секунды например?
 Надо ли писать что-то вроде этого ниже
 //========================================
 class UpdateProgressBarTask extends TimerTask {


 public void run() {
 // инкремент переменной счетчика
 counter=counter++;
 }
 }

 TimerTask updateProgressBar = new UpdateProgressBarTask();
 timer.scheduleAtFixedRate(updateProgressBar, 0, 2000); //2000millesec = 2sec
 //======================================================

 Marat Tanchuev, [11.07.18 13:06]
 да

 Marat Tanchuev, [11.07.18 13:06]
 вот так и делай

 Marat Tanchuev, [11.07.18 13:06]
 в run() пишешь что надо сделать и всё

 Marat Tanchuev, [11.07.18 13:06]
 и запускаешь как тут написано
 *
 *
 *
 */

public class BoundService extends Service{
    private static String LOG_TAG = "BoundService";
    private IBinder mBinder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "in onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");

    }


    public class MyBinder extends Binder {
        BoundService getService() {
            return BoundService.this;
        }
    }}
