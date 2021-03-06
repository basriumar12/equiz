package io.github.eputra.equiz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DaftarActivity extends AppCompatActivity {

    public static final String URL = "http://192.168.122.1/web/equiz/";
    private ProgressDialog progress;
    SessionManagement session;

    @BindView(R.id.etNamaPengguna) EditText etNamaPengguna;
    @BindView(R.id.etNama) EditText etNama;
    @BindView(R.id.etKataSandi) EditText etKataSandi;
    @BindView(R.id.spLevel) Spinner spLevel;

    @OnClick(R.id.btnDaftar) void daftar() {
        if (etNamaPengguna.getText().toString().isEmpty()) {
            etNamaPengguna.setError("Harus diisi");
            return;
        }

        if (etNama.getText().toString().isEmpty()) {
            etNama.setError("Harus diisi");
            return;
        }

        if (etKataSandi.getText().toString().isEmpty()) {
            etKataSandi.setError("Harus diisi");
            return;
        }

        //membuat progress dialog
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage("Loading ...");
        progress.show();

        //mengambil data dari edittext
        String nama_pengguna = etNamaPengguna.getText().toString();
        String nama = etNama.getText().toString();
        String kata_sandi = etKataSandi.getText().toString();

        //mengambil level yang dipilih dari spinner
        String level = String.valueOf(spLevel.getSelectedItem());

        //logging request and response
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        RegisterAPI api = retrofit.create(RegisterAPI.class);
        Call<Value> call = api.daftarDosen(nama_pengguna, nama, kata_sandi);
        if (level.equals("Mahasiswa")) {
            call = api.daftarMahasiswa(nama_pengguna, nama, kata_sandi);
        }
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                String value = response.body().getValue();
                String message = response.body().getMessage();
                String nama = response.body().getNama();
                String nama_pengguna = response.body().getNama_pengguna();
                String level = response.body().getLevel();
                progress.dismiss();
                if (value.equals("1")) {
                    // Session Manager
                    session.createLoginSession(nama, nama_pengguna, level);
                    if (session.isLoggedIn()) {
                        Intent i = new Intent(DaftarActivity.this, DosenActivity.class);
                        if (session.getLevel().equals("Mahasiswa")) {
                            i = new Intent(DaftarActivity.this, MahasiswaActivity.class);
                        }
                        startActivity(i);
                        Toast.makeText(DaftarActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(DaftarActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                progress.dismiss();
                Toast.makeText(DaftarActivity.this, "Jaringan Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);
        ButterKnife.bind(this);
        session = new SessionManagement(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(DaftarActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

}
