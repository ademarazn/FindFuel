package com.example.findfuel;

import android.os.Bundle;
import android.os.Handler;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Activity;

/**
 * @author Ademar
 * @since Classe criada em 11/10/2017
 */
public class MainActivity extends Activity {

    /*
    Método onCreate faz parte do ciclo de vida da Activity.
    Será chamado quando for criar a Activity
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            final FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            SplashFragment splashFrag = new SplashFragment();
            ft.add(R.id.layoutFrag, splashFrag, splashFrag.TAG);
            ft.commit();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FragmentTransaction ft1 = fm.beginTransaction();
                    MapsFragment mapsFrag = new MapsFragment();
                    ft1.replace(R.id.layoutFrag, mapsFrag, mapsFrag.TAG);
                    ft1.commit();
                }
            }, 2000);
        }
    }

    /*
    Método onBackPressed sobrescrito para minimizar o aplicativo
    quando for pressionado o botão voltar, ou seja, fazer o mesmo que o botão home
    */
    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}
