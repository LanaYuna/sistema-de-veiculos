package com.example.atividade_2;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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

        if (!isNetworkAvailable()) {
            exibirAlertaSemInternet();
            return;
        }

        Button btnMarcas = findViewById(R.id.btnMarcas);
        Button btnModelos = findViewById(R.id.btnModelos);
        Button btnCidades = findViewById(R.id.btnCidades);
        Button btnAnuncios = findViewById(R.id.btnAnuncios);

        btnMarcas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MarcasActivity.class);
            startActivity(intent);
        });

        btnModelos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ModelosActivity.class);
            startActivity(intent);
        });

        btnCidades.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CidadesActivity.class);
            startActivity(intent);
        });

        btnAnuncios.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AnunciosActivity.class);
            startActivity(intent);
        });
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
                    finish();
                })
                .show();
    }
    
}