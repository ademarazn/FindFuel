package com.example.findfuel;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 *
 * @author Ademar
 * @since Classe criada em 11/10/2017
 */
public class SplashFragment extends Fragment {
    public final String TAG = "SplashFragment";

    /* Construtor vazio Default */
    public SplashFragment() {
    }

    /*
    Método que faz parte do ciclo de vida de um Fragment e, que será chamado
    quando for criar a View no fragmento
    */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }
}
