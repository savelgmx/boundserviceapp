package fb.fandroid.adv.boundserviceapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class BoundService extends Service {


    private int count;
    private boolean canDownGrade;

    private int counterValue;

    public static int EXTRA_PROGRESS_STATUS;//="EXTRA_PROGRESS_STATUS"
    public static final String ACTION_PROGRESSCOUNT_CHANGE="ACTION_PROGRESSCOUNT_CHANGE";

    Intent progressStatusIntent=new Intent(ACTION_PROGRESSCOUNT_CHANGE);

    final String LOG_TAG = "BoundService";
    MyBinder binder = new MyBinder();

    Timer timer;
    TimerTask tTask;
    long interval = 200; //200 msec.


    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "BoundService onCreate");
        timer = new Timer();
        schedule();
    }


    void schedule() {
        if (tTask != null) tTask.cancel();
        tTask = new TimerTask() {
            public void run() {

                //здесь вызовем функцию которая прогресс статус инкрементит
                if ((count<100 ))
                {


                    if (!canDownGrade)
                    {
                        count=count+5;
                    }
                    else if (canDownGrade)
                    {
                        count=counterValue;

                    }

                }
                else if (count==100){count=0;} //progress  bar с заполнением 100% начинаем заполнять по новой1

                Log.d(LOG_TAG,"Count value="+count);
                //count value to MainActivity
                progressStatusIntent.putExtra(String.valueOf(EXTRA_PROGRESS_STATUS),count);
                sendBroadcast(progressStatusIntent); //кажды 200 м
            }
        };
        timer.schedule(tTask, 1, interval);
    }


    public int downGradeProgessBar(){

        canDownGrade=true; //выставляем флаг который er
        counterValue=count-50;
        return counterValue;
    }


    void cancelTimer(){
        if (timer != null) {
            timer.cancel();
            timer = null;
            interval=0;
            Log.d(LOG_TAG,"Cancel timer button was pressed");
        }
    }

    public IBinder onBind(Intent arg0) {
        Log.d(LOG_TAG, "BoundService onBind");
        return binder;
    }



    class MyBinder extends Binder {
        BoundService getService() {
            return BoundService.this;
        }
    }
}