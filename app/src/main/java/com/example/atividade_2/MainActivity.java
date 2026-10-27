package com.example.atividade_2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        retrofit = new Retrofit.Builder()
                .baseUrl("http://argo.td.utfpr.edu.br/locadora-war/ws/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        if (!isNetworkAvailable()) {
            exibirAlertaSemInternet();
            return; // Interrompe o fluxo para não tentar buscar marcas offline
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
        }
        return false;
    }

    private void exibirAlertaSemInternet() {
        new AlertDialog.Builder(this)
                .setTitle("Sem Conexão com a Internet")
                .setMessage("Este aplicativo requer conexão com a internet para funcionar e não pode ser usado offline.")
                .setCancelable(false) // Impede que o usuário feche o alerta clicando fora
                .setPositiveButton("Sair", (dialog, which) -> {
                    finish(); // Fecha o aplicativo
                })
                .show();
    }
}