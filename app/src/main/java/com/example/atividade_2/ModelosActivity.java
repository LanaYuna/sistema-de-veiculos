package com.example.atividade_2;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.atividade_2.api.MarcaAPIService;
import com.example.atividade_2.api.ModeloAPIService;
import com.example.atividade_2.model.Marca;
import com.example.atividade_2.model.Modelo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModelosActivity extends AppCompatActivity {

    ModeloAPIService serviceModelo;
    MarcaAPIService serviceMarca;

    ArrayList<Modelo> modelos;
    ArrayAdapter<Modelo> adapterModelo;
    ListView lista;

    EditText edNomeModelo;
    Spinner spinnerMarca;
    Spinner spinnerTipo;

    List<Marca> listaMarcasDoServidor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modelos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializando views do layout activity_modelos.xml
        edNomeModelo = findViewById(R.id.edNomeModelo);
        spinnerMarca = findViewById(R.id.spinnerMarca);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        lista = findViewById(R.id.lista_modelos);

        modelos = new ArrayList<>();
        adapterModelo = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                modelos);
        lista.setAdapter(adapterModelo);

        registerForContextMenu(lista);

        // Instancia os serviços do Retrofit
        serviceModelo = RetrofitClient.getClient().create(ModeloAPIService.class);
        serviceMarca = RetrofitClient.getClient().create(MarcaAPIService.class);

        // Configura o Spinner de Tipos permitidos exigidos pela API
        configurarSpinnerTipos();

        // Carrega as marcas para preencher o Spinner de marcas e depois busca os modelos
        carregarMarcasParaSpinner();

        findViewById(R.id.btSalvarModelo).setOnClickListener(v -> confirmar());
    }

    void configurarSpinnerTipos() {
        String[] tipos = {"SEDAN", "HATCH", "SUV", "CAMIONETE"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tipos);
        spinnerTipo.setAdapter(adapterTipo);
    }

    void carregarMarcasParaSpinner() {
        Call<List<Marca>> call = serviceMarca.listar();
        call.enqueue(new Callback<List<Marca>>() {
            @Override
            public void onResponse(Call<List<Marca>> call, Response<List<Marca>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Marca> listaMarcas = response.body();

                    ArrayAdapter<Marca> adapterSpinnerMarca = new ArrayAdapter<>(
                            ModelosActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            listaMarcas
                    );
                    spinnerMarca.setAdapter(adapterSpinnerMarca);

                    spinnerMarca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Marca marcaSelecionada = (Marca) parent.getItemAtPosition(position);
                            buscarModelos(marcaSelecionada.getId());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            buscarModelos(null);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Marca>> call, Throwable t) {
                Toast.makeText(ModelosActivity.this, "Erro ao carregar marcas", Toast.LENGTH_SHORT).show();
            }
        });
    }


    void confirmar() {
        String nomeModelo = edNomeModelo.getText().toString().trim();

        if (nomeModelo.isEmpty()) {
            Toast.makeText(this, "Digite o nome do modelo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerMarca.getSelectedItem() == null) {
            Toast.makeText(this, "Selecione uma marca válida", Toast.LENGTH_SHORT).show();
            return;
        }

        Marca marcaSelecionada = (Marca) spinnerMarca.getSelectedItem();
        String tipoSelecionado = spinnerTipo.getSelectedItem().toString();

        Modelo m = new Modelo();
        m.setNome(nomeModelo);
        m.setTipo(tipoSelecionado);
        m.setIdMarca(marcaSelecionada.getId()); // Envia o ID da marca relacionada conforme enunciado!

        edNomeModelo.setText("");

        Call<Modelo> req = serviceModelo.inserir(m);
        req.enqueue(new Callback<Modelo>() {
            @Override
            public void onResponse(Call<Modelo> call, Response<Modelo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Modelo modeloCriado = response.body();
                    adapterModelo.add(modeloCriado);
                    adapterModelo.notifyDataSetChanged();
                    Toast.makeText(ModelosActivity.this, "Modelo salvo com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ModelosActivity.this, "Erro ao salvar modelo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Modelo> call, Throwable throwable) {
                Toast.makeText(ModelosActivity.this, "Erro de rede: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void buscarModelos(Long idMarcaFiltro) {
        Call<List<Modelo>> busca = serviceModelo.listar(idMarcaFiltro);
        busca.enqueue(new Callback<List<Modelo>>() {
            @Override
            public void onResponse(Call<List<Modelo>> call, Response<List<Modelo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    modelos.clear();
                    modelos.addAll(response.body());
                    adapterModelo.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Modelo>> call, Throwable throwable) {
                Toast.makeText(ModelosActivity.this, "Erro ao buscar modelos: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void alterar(Modelo modelo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar nome do Modelo");

        final EditText input = new EditText(this);
        input.setText(modelo.getNome());
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String novoNome = input.getText().toString().trim();
            if (!novoNome.isEmpty()) {
                modelo.setNome(novoNome);

                Call<Modelo> req = serviceModelo.alterar(modelo.getId(), modelo);
                req.enqueue(new Callback<Modelo>() {
                    @Override
                    public void onResponse(Call<Modelo> call, Response<Modelo> response) {
                        if (response.isSuccessful()) {
                            adapterModelo.notifyDataSetChanged();
                            Toast.makeText(ModelosActivity.this, "Editado com sucesso", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ModelosActivity.this, "Erro ao atualizar no servidor", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Modelo> call, Throwable t) {
                        Toast.makeText(ModelosActivity.this, "Erro de rede", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    void remover(Modelo modelo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tem certeza que deseja remover?");

        builder.setPositiveButton("Sim", (dialog, which) -> {
            Call<Void> req = serviceModelo.remover(modelo.getId());
            req.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        adapterModelo.remove(modelo);
                        adapterModelo.notifyDataSetChanged();
                        Toast.makeText(ModelosActivity.this, "Removido com sucesso", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ModelosActivity.this, "Erro: Não é possível remover itens com dependência", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ModelosActivity.this, "Erro de rede", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Não", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Opções de Modelo");
        menu.add(0, 1, 0, "Editar modelo");
        menu.add(0, 2, 1, "Remover modelo");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);
        Modelo modelo = modelos.get(info.position);

        switch (item.getItemId()) {
            case 1:
                alterar(modelo);
                return true;
            case 2:
                remover(modelo);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}