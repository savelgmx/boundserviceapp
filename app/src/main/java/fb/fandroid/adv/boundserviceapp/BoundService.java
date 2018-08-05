package fb.fandroid.adv.boundserviceapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
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


/*public void run() {
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

    public static final int START_SCHEDULE=1;
    public static final int STOP_SCHEDULE=2;

    private ScheduledExecutorService mScheduledExecutorService;

    int count = 0;

    IncomingHandler inHandler;

    Messenger messenger;
    Messenger toActivityMessenger;



    private static String LOG_TAG = "BoundService";
    private IBinder mBinder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "in onCreate");

        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        inHandler = new IncomingHandler(thread.getLooper());
        messenger = new Messenger(inHandler);

        //---инициализируем ScheduledExecutorService
        //  она должна по расписанию кажые 200 мс.  увеличивать значение переданнной переменной
        mScheduledExecutorService = Executors.newScheduledThreadPool(1);
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
                            catch (RemoteException e) {
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
            }

            //отправляем значение счетчика в активити
            /*


            Message outMsg = Message.obtain(inHandler, START_SCHEDULE);
            outMsg.arg1 = count;
            outMsg.replyTo = messenger;

            try {
                if( toActivityMessenger != null )
                    toActivityMessenger.send(outMsg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }

                */
        }
    }
}