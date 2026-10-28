package com.example.atividade_2;

import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.atividade_2.api.MarcaAPIService;
import com.example.atividade_2.model.Marca;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MarcasActivity extends AppCompatActivity {

    MarcaAPIService service;
    ArrayList<Marca> marcas;
    ArrayAdapter<Marca> adapter;
    ListView lista;
    EditText edMarca;

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

        edMarca = findViewById(R.id.edMarca);
        lista = findViewById(R.id.lista_marcas);

        marcas = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice,
                marcas);
        lista.setAdapter( adapter );
        lista.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        service = RetrofitClient.getClient().create(MarcaAPIService.class);

        findViewById(R.id.btnConfirmar).setOnClickListener(
                (v) -> {confirmar();}
        );

    }

    void confirmar(){
        String novaMarca = edMarca.getText().toString();
        if (!novaMarca.isBlank()) {
            Marca m = new Marca();
            m.setNome(novaMarca);
            edMarca.setText("");
            Call<Void> req = service.inserir( m );
            req.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        adapter.add(novaMarca);
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    throwable.printStackTrace();
                    Toast.makeText(MarcasActivity.this,
                            "Erro: "+throwable.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void remover() {
        int pos = lista.getCheckedItemPosition();
        if (pos < 0) {
            Toast.makeText(this,"Selecione a marca a remover",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Marca m = marcas.get( pos );
        Call<Void> req = service.remover( m.getId() );
        req.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful())
                                adapter.remove(m);
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable throwable) { }
                    }
        );
    }

    void buscarMarcas() {
        Call<List<Marca>> busca = service.listar();
        busca.enqueue(new Callback<List<Marca>>() {
            @Override
            public void onResponse(Call<List<Marca>> call,
                                   Response<List<Marca>> response) {
                if (response.isSuccessful()) {
                    List<Marca> ret = response.body();
                    adapter.clear();
                    adapter.addAll( ret );
                }
            }

            @Override
            public void onFailure(Call<List<Marca>> call, Throwable throwable) {
                throwable.printStackTrace();
                Toast.makeText(MarcasActivity.this,
                        "Erro: "+throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
