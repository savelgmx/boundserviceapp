package fb.fandroid.adv.boundserviceapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/*
    При запуске приложения создать Bound Service
 в потоке которого постепенно будет меняться значение прогресса и,
    соответственно, обновляться ProgressBar. Если брать максимум ProgressBar - 100%,
    то значение прогресса должно меняться на 5% каждые 200 миллисекунд.
    По достижению 100% ProgressBar перестает заполняться.
    В любой момент по нажатию на кнопку шкала уменьшается на 50%, но не меньше 0%.
    (75% -> 25%; 35% -> 0%)
Дополнительно: Каждый раз по достижении 100% появляется тост о завершении загрузки.
 */

public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG =  "BoundService";
    boolean mServiceBound = false;
    private int progressStatus=0;

    BoundServiceConnection boundServConn;
    ProgressBar progressBar;


    final Messenger messenger = new Messenger(new IncomingHandler());
    Messenger toServiceMessenger;


    private void showMessage(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startScheduleBth =(Button)findViewById(R.id.startScheduleBtn);
        Button stopServiceButton = (Button) findViewById(R.id.stop_service);
        Button progressBarDownButton =(Button)findViewById(R.id.progressbar_down);

        progressBar =(ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);// visible the progress bar

        Log.d(LOG_TAG,"MainActivity ..is creating");


        progressBarDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (progressStatus>0) {
                   progressStatus = progressStatus - 50;
                    progressBar.setProgress(progressStatus);
                }
            }
        });


        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServiceBound) {
                    unbindService(boundServConn);
                    mServiceBound = false;
                }
                Intent intent = new Intent(MainActivity.this,
                        BoundService.class);
                stopService(intent);
            }
        });

        startScheduleBth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, BoundService.START_SCHEDULE);
                msg.replyTo = messenger;

                try {
                    toServiceMessenger.send(msg);
                }
                catch (RemoteException e)
                {
                    e.printStackTrace();
                }
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BoundService.class);
        startService(intent);
       // bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, BoundService.class),
                (boundServConn = new BoundServiceConnection()),
                Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

        unbindService(boundServConn);
        stopService(new Intent(this,BoundService.class));//не забываем прибить ненужный сервис при завершении программы
        Log.d(LOG_TAG,"Service stopped");
    }



    private class IncomingHandler extends Handler  {

        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                 case BoundService.START_SCHEDULE:
                    Log.d(LOG_TAG,"(schedule)...start schedule command");

                     //-------progress bar------------------
                    progressStatus=msg.arg1;

                    Log.d(LOG_TAG,"progress status="+progressStatus);

                    progressBar.setProgress(progressStatus);
                    //-------------------------------

                    break;
            }
        }

    }

    private class BoundServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            toServiceMessenger = new Messenger(service);
            //отправляем начальное значение счетчика
            Message msg = Message.obtain(null, BoundService.SET_COUNT);
            msg.replyTo = messenger;
            msg.arg1 = 0; //наш счетчик
            try {
                toServiceMessenger.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {	}
    }

}
