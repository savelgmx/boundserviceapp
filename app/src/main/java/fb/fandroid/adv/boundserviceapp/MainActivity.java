package fb.fandroid.adv.boundserviceapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
     boolean bound = false;
    ServiceConnection sConn;
    Intent intent;
    BoundService boundService;

    private int progressStatus=0;
    ProgressBar progressBar;

    public IntentFilter mIntentFilter;

    private void showMessage(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    /*
     Реализация ресивера прямо в активити. Поэтому можно спокойно обращаться к полям активити
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(BoundService.ACTION_PROGRESSCOUNT_CHANGE)){

                progressStatus=intent.getExtras().getInt(String.valueOf(BoundService.EXTRA_PROGRESS_STATUS));
                 Log.d(LOG_TAG,"progress Status received by Main="+progressStatus);
                progressBar.setProgress(progressStatus);//--меняем значение прогресс бара
                if (progressStatus==100){showMessage("Загрузка завершена");}
            }
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button stopServiceButton = (Button) findViewById(R.id.stop_service);
        Button progressBarDownButton =(Button)findViewById(R.id.progressbar_down);

        progressBar =(ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);// visible the progress bar
        //-----инициализируем связь с сервисом и передваемые в него интенты
        // ----регистрируем бродкастресивер без регистрации он не будет работать-------------

        intent = new Intent(this, BoundService.class);
        sConn = new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                boundService = ((BoundService.MyBinder) binder).getService();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
            }
        };

        mIntentFilter = new IntentFilter(BoundService.ACTION_PROGRESSCOUNT_CHANGE);
        registerReceiver(mReceiver, mIntentFilter);
        //------------------------------------------------
        Log.d(LOG_TAG,"MainActivity ..is creating");


        progressBarDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(LOG_TAG,"progressswbar Button was pressed");

               boundService.downGradeProgessBar(); //вызываем метод

            }
        });


        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bound) {
                    unbindService(sConn);
                    bound = false;
                }
                Intent intent = new Intent(MainActivity.this,
                        BoundService.class);
               // stopService(intent);
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
         bindService(intent,sConn, Context.BIND_AUTO_CREATE);
     }

    @Override
    protected void onStop() {
        super.onStop();
        if (!bound) return;
        unbindService(sConn);
        bound = false;
    }


    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    protected void onDestroy(){
        super.onDestroy();

        if (!bound) return;
        unbindService(sConn);
        bound = false;

        stopService(intent);//не забываем прибить ненужный сервис при завершении программы
        Log.d(LOG_TAG,"Service stopped");
    }

}
