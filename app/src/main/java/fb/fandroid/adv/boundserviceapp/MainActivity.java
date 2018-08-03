package fb.fandroid.adv.boundserviceapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import fb.fandroid.adv.boundserviceapp.BoundService.MyBinder;

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
    BoundService mBoundService;
    boolean mServiceBound = false;
    private int progressStatus=0;

    private Handler handler=new Handler();

    private void showMessage(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button stopServiceButon = (Button) findViewById(R.id.stop_service);
        Button progressBarDownButton =(Button)findViewById(R.id.progressbar_down);
        final ProgressBar progressBar =(ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);// visible the progress bar



        progressBarDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (progressStatus>0) {
                    progressStatus = progressStatus - 50;
                    progressBar.setProgress(progressStatus);
                }
            }
        });


        stopServiceButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServiceBound) {
                    unbindService(mServiceConnection);
                    mServiceBound = false;
                }
                Intent intent = new Intent(MainActivity.this,
                        BoundService.class);
                stopService(intent);
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BoundService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BoundService.MyBinder myBinder = (BoundService.MyBinder) service;
            mBoundService = myBinder.getService();
            mServiceBound = true;
        }
    };
}
