package fb.fandroid.adv.boundserviceapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class BoundService extends Service{

    public static final int START_SCHEDULE=1;
    public static final int STOP_SCHEDULE=2;
    public static final int SET_COUNT=3;

    private ScheduledExecutorService mScheduledExecutorService;

    int count = 0;

    IncomingHandler inHandler;
    Messenger messenger;
    Messenger toActivityMessenger;



    private static String LOG_TAG = "BoundService";


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "in onCreate");

        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        inHandler = new IncomingHandler(thread.getLooper());
        messenger = new Messenger(inHandler);

        //---инициализируем ScheduledExecutorService
        //  она должна по расписанию кажые 200 мс.  увеличивать значение переданнной переменной
        mScheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    @Override

    public IBinder onBind(Intent arg0) {
        return messenger.getBinder();
    }

    @Override
    public void onRebind(Intent arg0) {
        Log.d(LOG_TAG, "in onRebind");
        super.onRebind(arg0);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");
        mScheduledExecutorService.shutdownNow();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    //обработчик сообщений активити
    private class IncomingHandler extends Handler{

        public IncomingHandler(Looper looper){
            super(looper);
        }
        @Override
        //
        public void handleMessage(Message msg) {
            toActivityMessenger = msg.replyTo;

            switch (msg.what) {

                case START_SCHEDULE:

                    Log.d(LOG_TAG,"(service schedule) ....started");
                    //----здесь мы запускам расписание по которому увеличивается счетчик
                   /* Если брать максимум ProgressBar - 100%,
                        то значение прогресса должно меняться на 5% каждые 200 миллисекунд.
                        По достижению 100% ProgressBar перестает заполняться.*/
                    mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {

                            if ((count<100 ))
                            {
                                count=count+5;
        }
                            else if (count==100){count=0;} //progress  bar с заполнением 100% начинаем заполнять по новой1

                            Log.d(LOG_TAG,"Count value="+count);

                            //------т.к.все внутри кгт то нужно отпрвлять значение в активити здесь
                            //отправляем значение счетчика в активити
                            Message outMsg = Message.obtain(inHandler, START_SCHEDULE);
                            outMsg.arg1 = count;
                            outMsg.replyTo = messenger;

                            try {
                                if( toActivityMessenger != null )
                                    toActivityMessenger.send(outMsg);
                                }
                            catch (RemoteException e)
                                {
                                e.printStackTrace();
                                }
                            //--------отправляем значение счетчика в активити

                         }
                    },1, 200, TimeUnit.MILLISECONDS);

                break;

                case STOP_SCHEDULE:
                    Log.d(LOG_TAG,"schedule...stop");
                     mScheduledExecutorService.shutdownNow();
                    break;


                case SET_COUNT:

                    count = msg.arg1;
                    Log.d(LOG_TAG,"initial count value ..set to 0");

                    break;
            }

        }
    }
}