package com.example.atividade_2;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.atividade_2.api.AnuncioAPIService;
import com.example.atividade_2.api.CidadeAPIService;
import com.example.atividade_2.api.ModeloAPIService;
import com.example.atividade_2.model.Anuncio;
import com.example.atividade_2.model.Cidade;
import com.example.atividade_2.model.Marca;
import com.example.atividade_2.model.Modelo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnunciosActivity extends AppCompatActivity {

    AnuncioAPIService serviceAnuncio;
    ModeloAPIService serviceModelo;
    CidadeAPIService serviceCidade;
    Spinner spinnerModelo;
    Spinner spinnerCidade;
    EditText edDescricao;
    EditText edCor;
    EditText edValor;
    EditText edAno;
    EditText edKm;
    ListView lista;

    ArrayList<Anuncio> anuncios;
    ArrayAdapter<Anuncio> adapterAnuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anuncios);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerModelo = findViewById(R.id.spinnerModelo);
        spinnerCidade = findViewById(R.id.spinnerCidade);
        edDescricao = findViewById(R.id.edDescricao);
        edCor = findViewById(R.id.edCor);
        edValor = findViewById(R.id.edValor);
        edAno = findViewById(R.id.edAno);
        edKm = findViewById(R.id.edKm);

        anuncios = new ArrayList<>();
        adapterAnuncio = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                anuncios);
        lista.setAdapter(adapterAnuncio);

        registerForContextMenu(lista);

        serviceAnuncio = RetrofitClient.getClient().create(AnuncioAPIService.class);
        serviceModelo = RetrofitClient.getClient().create(ModeloAPIService.class);
        serviceCidade= RetrofitClient.getClient().create(CidadeAPIService.class);

        carregarModelosParaSpinner();
        carregarCidadesParaSpinner();

        findViewById(R.id.btSalvarAnuncio).setOnClickListener(v -> confirmar());
    }

    void carregarModelosParaSpinner(){
        Call<List<Modelo>> call = serviceModelo.listar(null);
        call.enqueue(new Callback<List<Modelo>>() {
            @Override
            public void onResponse(Call<List<Modelo>> call, Response<List<Modelo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Modelo> listaModelos = response.body();

                    ArrayAdapter<Modelo> adapterSpinnerModelo = new ArrayAdapter<>(
                            AnunciosActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            listaModelos
                    );
                    spinnerModelo.setAdapter(adapterSpinnerModelo);
                }
            }

            @Override
            public void onFailure(Call<List<Modelo>> call, Throwable throwable) {
                Toast.makeText(AnunciosActivity.this, "Erro ao carregar modelos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void carregarCidadesParaSpinner(){
        Call<List<Cidade>> call = serviceCidade.listar(null);
        call.enqueue(new Callback<List<Cidade>>() {
            @Override
            public void onResponse(Call<List<Cidade>> call, Response<List<Cidade>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Cidade> listaCidades = response.body();

                    ArrayAdapter<Cidade> adapterSpinnerCidade = new ArrayAdapter<>(
                            AnunciosActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            listaCidades
                    );
                    spinnerCidade.setAdapter(adapterSpinnerCidade);
                }
            }

            @Override
            public void onFailure(Call<List<Cidade>> call, Throwable throwable) {
                Toast.makeText(AnunciosActivity.this, "Erro ao carregar cidades", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void confirmar(){

    }

}
