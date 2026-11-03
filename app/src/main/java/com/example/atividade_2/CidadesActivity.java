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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.atividade_2.api.CidadeAPIService;
import com.example.atividade_2.model.Cidade;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CidadesActivity extends AppCompatActivity {

    CidadeAPIService serviceCidade;
    ArrayList<Cidade> cidades;
    ArrayAdapter<Cidade> adapterCidade;
    ListView lista;
    EditText edNomeCidade;
    EditText edDddCidade;
    Spinner spinnerCidade;
    ArrayList<Cidade> cidadesSpinner;
    ArrayAdapter<Cidade> adapterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cidades);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        edNomeCidade = findViewById(R.id.edNomeCidade);
        edDddCidade = findViewById(R.id.edDddCidade);
        spinnerCidade = findViewById(R.id.spinnerCidade);
        lista = findViewById(R.id.lista_cidades);

        cidades = new ArrayList<>();
        adapterCidade = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                cidades);
        lista.setAdapter(adapterCidade);

        registerForContextMenu(lista);

        serviceCidade = RetrofitClient.getClient().create(CidadeAPIService.class);

        cidadesSpinner = new ArrayList<>();
        adapterSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cidadesSpinner);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCidade.setAdapter(adapterSpinner);

        spinnerCidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                buscarCidades(filtroAtual());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                buscarCidades(null);
            }
        });

        carregarCidadesParaSpinner();

        findViewById(R.id.btnSalvarCidade).setOnClickListener(
                (v) -> { salvar(); }
        );
    }

    void salvar() {
        String nomeCidade = edNomeCidade.getText().toString();
        String ddd = edDddCidade.getText().toString();

        if (!nomeCidade.isBlank() && !ddd.isBlank()) {
            Cidade c = new Cidade();
            c.setNome(nomeCidade);
            c.setDdd(ddd);
            edNomeCidade.setText("");
            edDddCidade.setText("");
            Call<Cidade> req = serviceCidade.inserir(c);
            req.enqueue(new Callback<Cidade>() {
                @Override
                public void onResponse(Call<Cidade> call, Response<Cidade> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Cidade cidadeCriadaServidor = response.body();
                        adapterCidade.add(cidadeCriadaServidor);
                        adapterCidade.notifyDataSetChanged();
                        carregarCidadesParaSpinner();
                        buscarCidades(filtroAtual());
                        Toast.makeText(CidadesActivity.this, getString(R.string.cidadeSalvaSucesso), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Cidade> call, Throwable throwable) {
                    throwable.printStackTrace();
                    Toast.makeText(CidadesActivity.this,
                            getString(R.string.erro) + " " + throwable.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.digiteNomeDddCidade), Toast.LENGTH_SHORT).show();
        }
    }

    void buscarCidades(String nomeCidade) {
        Call<List<Cidade>> busca = serviceCidade.listar(nomeCidade);
        busca.enqueue(new Callback<List<Cidade>>() {
            @Override
            public void onResponse(Call<List<Cidade>> call,
                                   Response<List<Cidade>> response) {
                if (response.isSuccessful()) {
                    List<Cidade> ret = response.body();
                    adapterCidade.clear();
                    adapterCidade.addAll(ret);
                    adapterCidade.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Cidade>> call, Throwable throwable) {
                throwable.printStackTrace();
                Toast.makeText(CidadesActivity.this,
                        getString(R.string.erro) + " " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    void alterar(Cidade cidade) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.editarCidade);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);

        final EditText inputNome = new EditText(this);
        inputNome.setHint(R.string.nomeCidadeHint);
        inputNome.setText(cidade.getNome());
        layout.addView(inputNome);

        final EditText inputDdd = new EditText(this);
        inputDdd.setHint(R.string.dddCidadeHint);
        inputDdd.setText(cidade.getDdd());
        layout.addView(inputDdd);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);
        builder.setView(scroll);

        builder.setPositiveButton(R.string.salvar, (dialog, which) -> {
            String novoNome = inputNome.getText().toString().trim();
            String novoDdd = inputDdd.getText().toString().trim();
            if (!novoNome.isEmpty()) {
                Cidade cidadeAtualizada = new Cidade();
                cidadeAtualizada.setId(cidade.getId());
                cidadeAtualizada.setNome(novoNome);
                cidadeAtualizada.setDdd(novoDdd);

                Call<Void> req = serviceCidade.alterar(cidade.getId(), cidadeAtualizada);
                req.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            cidade.setNome(novoNome);
                            adapterCidade.notifyDataSetChanged();
                            carregarCidadesParaSpinner();
                            buscarCidades(filtroAtual());
                            Toast.makeText(CidadesActivity.this, getString(R.string.editadoSucesso), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CidadesActivity.this, getString(R.string.erroAtualizarServidor), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        Toast.makeText(CidadesActivity.this, getString(R.string.erroRede), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    void remover(Cidade cidade) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.certezaRemocao);

        builder.setPositiveButton(R.string.sim, (dialog, which) -> {
            Call<Void> req = serviceCidade.remover(cidade.getId());
            req.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        adapterCidade.remove(cidade);
                        adapterCidade.notifyDataSetChanged();
                        carregarCidadesParaSpinner();
                        buscarCidades(filtroAtual());
                        Toast.makeText(CidadesActivity.this, getString(R.string.remocaoSucesso), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CidadesActivity.this, getString(R.string.dependencia), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(CidadesActivity.this, R.string.erroRede, Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton(R.string.nao, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    String filtroAtual() {
        int pos = spinnerCidade.getSelectedItemPosition();
        if (pos <= 0 || pos >= cidadesSpinner.size()) return null;
        return cidadesSpinner.get(pos).getNome();
    }

    void carregarCidadesParaSpinner() {
        serviceCidade.listar(null).enqueue(new Callback<List<Cidade>>() {
            @Override
            public void onResponse(Call<List<Cidade>> call, Response<List<Cidade>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String nomeSelecionado = filtroAtual();

                    Cidade todas = new Cidade();
                    todas.setNome(getString(R.string.todasCidades));

                    cidadesSpinner.clear();
                    cidadesSpinner.add(todas);
                    cidadesSpinner.addAll(response.body());
                    adapterSpinner.notifyDataSetChanged();

                    int novaPos = 0;
                    for (int i = 1; i < cidadesSpinner.size(); i++) {
                        if (cidadesSpinner.get(i).getNome().equals(nomeSelecionado)) {
                            novaPos = i;
                            break;
                        }
                    }
                    spinnerCidade.setSelection(novaPos);
                }
            }

            @Override
            public void onFailure(Call<List<Cidade>> call, Throwable t) {
                Toast.makeText(CidadesActivity.this, getString(R.string.erroCarregarCidades),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.opcoesCidade);
        menu.add(0, 1, 0, R.string.editarCidadeMenu);
        menu.add(0, 2, 1, R.string.removerCidadeMenu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);
        Cidade cidade = cidades.get(info.position);

        switch (item.getItemId()) {
            case 1:
                alterar(cidade);
                return true;

            case 2:
                remover(cidade);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }
}