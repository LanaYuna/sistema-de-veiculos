package com.example.atividade_2;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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

    MarcaAPIService serviceMarca;
    ArrayList<Marca> marcas;
    ArrayAdapter<Marca> adapterMarca;
    ListView lista;
    EditText edMarca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_marcas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edMarca = findViewById(R.id.edMarca);
        lista = findViewById(R.id.lista_marcas);

        marcas = new ArrayList<>();
        adapterMarca = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                marcas);
        lista.setAdapter( adapterMarca );

        registerForContextMenu(lista);

        serviceMarca = RetrofitClient.getClient().create(MarcaAPIService.class);

        buscarMarcas();

        findViewById(R.id.btnConfirmar).setOnClickListener(
                (v) -> {confirmar();}
        );

    }

    void confirmar(){
        String nomeMarca = edMarca.getText().toString();
        if (!nomeMarca.isBlank()) {
            Marca m = new Marca();
            m.setNome(nomeMarca);
            edMarca.setText("");
            Call<Marca> req = serviceMarca.inserir( m );
            req.enqueue(new Callback<Marca>() {
                @Override
                public void onResponse(Call<Marca> call, Response<Marca> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Marca marcaCriadaServidor = response.body();
                        adapterMarca.add(marcaCriadaServidor);
                        adapterMarca.notifyDataSetChanged();
                        Toast.makeText(MarcasActivity.this, R.string.marcaSalva, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Marca> call, Throwable throwable) {
                    throwable.printStackTrace();
                    Toast.makeText(MarcasActivity.this,
                            R.string.erro +throwable.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, R.string.digiteNomeMarca, Toast.LENGTH_SHORT).show();
        }
    }
    void buscarMarcas() {
        Call<List<Marca>> busca = serviceMarca.listar();
        busca.enqueue(new Callback<List<Marca>>() {
            @Override
            public void onResponse(Call<List<Marca>> call,
                                   Response<List<Marca>> response) {
                if (response.isSuccessful()) {
                    List<Marca> ret = response.body();
                    adapterMarca.clear();
                    adapterMarca.addAll( ret );
                    adapterMarca.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Marca>> call, Throwable throwable) {
                throwable.printStackTrace();
                Toast.makeText(MarcasActivity.this,
                        R.string.erro +throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    void alterar(Marca marca){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.editarMarca);

        final EditText input = new EditText(this);
        input.setText(marca.getNome());
        builder.setView(input);

        builder.setPositiveButton(R.string.salvar, (dialog, which) -> {
            String novoNome = input.getText().toString().trim();
            if (!novoNome.isEmpty()) {
                Marca marcaAtualizada = new Marca();
                marcaAtualizada.setId(marca.getId());
                marcaAtualizada.setNome(novoNome);

                Call<Void> req = serviceMarca.alterar(marca.getId(), marcaAtualizada);
                req.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            marca.setNome(novoNome);
                            adapterMarca.notifyDataSetChanged();
                            Toast.makeText(MarcasActivity.this, R.string.editadoSucesso, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MarcasActivity.this, R.string.erroServidor, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        Toast.makeText(MarcasActivity.this, R.string.erroRede, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    void remover(Marca marca) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.certezaRemocao);

        builder.setPositiveButton(R.string.sim, (dialog, which) -> {
            Call<Void> req = serviceMarca.remover(marca.getId());
            req.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        adapterMarca.remove(marca);
                        adapterMarca.notifyDataSetChanged();
                        Toast.makeText(MarcasActivity.this, R.string.remocaoSucesso, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MarcasActivity.this, R.string.dependencia, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    Toast.makeText(MarcasActivity.this, R.string.erroRede, Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton(R.string.nao, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.opcoes);
        menu.add(0, 1, 0, R.string.editarMarca);
        menu.add(0, 2, 1, R.string.removerMarca);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(info == null) return super.onContextItemSelected(item);
        Marca marca = marcas.get(info.position);

        switch (item.getItemId()) {
            case 1:
                alterar(marca);
                return true;

            case 2:
                remover(marca);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

}
