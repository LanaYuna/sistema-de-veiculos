package com.example.atividade_2;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Objects;

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

        serviceModelo = RetrofitClient.getClient().create(ModeloAPIService.class);
        serviceMarca = RetrofitClient.getClient().create(MarcaAPIService.class);

        configurarSpinnerTipos();
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
                Toast.makeText(ModelosActivity.this, R.string.erroCarregarMarcas, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void confirmar() {
        String nomeModelo = edNomeModelo.getText().toString().trim();

        if (nomeModelo.isEmpty()) {
            Toast.makeText(this, getString(R.string.digiteNomeModelo), Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerMarca.getSelectedItem() == null) {
            Toast.makeText(this, getString(R.string.selecioneMarcaValida), Toast.LENGTH_SHORT).show();
            return;
        }

        Marca marcaSelecionada = (Marca) spinnerMarca.getSelectedItem();
        String tipoSelecionado = spinnerTipo.getSelectedItem().toString();

        Modelo m = new Modelo();
        m.setNome(nomeModelo);
        m.setTipo(tipoSelecionado);
        m.setIdMarca(marcaSelecionada.getId());
        edNomeModelo.setText("");

        Call<Modelo> req = serviceModelo.inserir(m);
        req.enqueue(new Callback<Modelo>() {
            @Override
            public void onResponse(Call<Modelo> call, Response<Modelo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Modelo modeloCriado = response.body();
                    adapterModelo.add(modeloCriado);
                    adapterModelo.notifyDataSetChanged();
                    Toast.makeText(ModelosActivity.this, getString(R.string.modeloSalvoSucesso), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ModelosActivity.this, getString(R.string.erroSalvarModelo), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Modelo> call, Throwable throwable) {
                Toast.makeText(ModelosActivity.this, getString(R.string.erroRede) + ": " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ModelosActivity.this, getString(R.string.erroBuscarModelos) + ": " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void alterar(Modelo modelo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.editarModelo);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);

        final EditText inputNome = new EditText(this);
        inputNome.setHint(R.string.nomeModelo);
        inputNome.setText(modelo.getNome());
        layout.addView(inputNome);

        TextView lblTipo = new TextView(this);
        lblTipo.setText(R.string.tipoVeiculoLabel);
        layout.addView(lblTipo);

        final Spinner spinnerEditTipo = new Spinner(this);
        String[] tipos = {"SEDAN", "HATCH", "SUV", "CAMIONETE"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tipos);
        spinnerEditTipo.setAdapter(adapterTipo);

        if (modelo.getTipo() != null) {
            for (int i = 0; i < spinnerEditTipo.getCount(); i++) {
                String tipoItem = (String) spinnerEditTipo.getItemAtPosition(i);
                if (tipoItem.equalsIgnoreCase(modelo.getTipo())) {
                    spinnerEditTipo.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(spinnerEditTipo);

        TextView lblMarca = new TextView(this);
        lblMarca.setText(R.string.marcaLabel);
        layout.addView(lblMarca);

        final Spinner spinnerEditMarca = new Spinner(this);
        spinnerEditMarca.setAdapter(spinnerMarca.getAdapter());

        if (modelo.getIdMarca() != null) {
            for (int i = 0; i < spinnerEditMarca.getCount(); i++) {
                Marca m = (Marca) spinnerEditMarca.getItemAtPosition(i);
                if (Objects.equals(m.getId(), modelo.getIdMarca())) {
                    spinnerEditMarca.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(spinnerEditMarca);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);
        builder.setView(scroll);

        builder.setPositiveButton(R.string.salvar, (dialog, which) -> {
            String novoNome = inputNome.getText().toString().trim();
            String novoTipo = spinnerEditTipo.getSelectedItem() != null ? spinnerEditTipo.getSelectedItem().toString() : modelo.getTipo();
            Marca marcaSelecionada = (Marca) spinnerEditMarca.getSelectedItem();

            if (novoNome.isEmpty()) {
                Toast.makeText(this, getString(R.string.preenchaNomeModelo), Toast.LENGTH_SHORT).show();
                return;
            }

            if (marcaSelecionada == null) {
                Toast.makeText(this, getString(R.string.selecioneMarca), Toast.LENGTH_SHORT).show();
                return;
            }

            Modelo modeloAtualizado = new Modelo();
            modeloAtualizado.setId(modelo.getId());
            modeloAtualizado.setNome(novoNome);
            modeloAtualizado.setTipo(novoTipo);
            modeloAtualizado.setIdMarca(marcaSelecionada.getId());

            Call<Void> req = serviceModelo.alterar(modelo.getId(), modeloAtualizado);
            req.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        modelo.setNome(novoNome);
                        modelo.setTipo(novoTipo);
                        modelo.setIdMarca(marcaSelecionada.getId());
                        adapterModelo.notifyDataSetChanged();
                        Toast.makeText(ModelosActivity.this, getString(R.string.editadoSucesso), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ModelosActivity.this, getString(R.string.erroAtualizarServidor), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ModelosActivity.this, R.string.erroRede, Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    void remover(Modelo modelo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.certezaRemocao);

        builder.setPositiveButton(R.string.sim, (dialog, which) -> {
            Call<Void> req = serviceModelo.remover(modelo.getId());
            req.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        adapterModelo.remove(modelo);
                        adapterModelo.notifyDataSetChanged();
                        Toast.makeText(ModelosActivity.this, getString(R.string.remocaoSucesso), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ModelosActivity.this, getString(R.string.dependencia), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ModelosActivity.this, R.string.erroRede, Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton(R.string.nao, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.opcoesModelo);
        menu.add(0, 1, 0, R.string.editarModeloMenu);
        menu.add(0, 2, 1, R.string.removerModeloMenu);
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