package com.androidmorefast.moden.appspinnermysql;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private Button btnAgregar;
    private TextView txtAgregar;
    private Spinner spinnerfruta;
    // array para listar las frutas
    private ArrayList<Frutas> frutasList;
    ProgressDialog pDialog;

    // API urls
    // Url creacion de nuevas frutas
    private String URL_NEW_FRUTA = "http://192.168.1.33:8080/blog/spinner/agregar.php";
    // Url listar las frutas
    private String URL_LISTA_FRUTA = "http://192.168.1.33:8080/blog/spinner/listar.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAgregar = (Button) findViewById(R.id.btnNuevaFruta);
        spinnerfruta = (Spinner) findViewById(R.id.spinfruta);
        txtAgregar = (TextView) findViewById(R.id.txtFruta);

        frutasList = new ArrayList<Frutas>();

        // seleccionar las frutas del spinner
        spinnerfruta.setOnItemSelectedListener(this);

        btnAgregar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (txtAgregar.getText().toString().trim().length() > 0) {


                    String nuevaFruta = txtAgregar.getText().toString();


                    new AddNuevafruta().execute(nuevaFruta);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "por favor ingrese nombre de la fruta", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        new Getfrutas().execute();



    }


    private void populateSpinner() {
        List<String> lables = new ArrayList<String>();

        txtAgregar.setText("");

        for (int i = 0; i < frutasList.size(); i++) {
            lables.add(frutasList.get(i).getName());
        }


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);


        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


       spinnerfruta.setAdapter(spinnerAdapter);



    }


    private class Getfrutas extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Obtencion de las frutas..");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ServiceHandler jsonParser = new ServiceHandler();
            String json = jsonParser.makeServiceCall(URL_LISTA_FRUTA, ServiceHandler.GET);

            Log.e("Response: ", "> " + json);

            if (json != null) {
                try {
                    JSONObject jsonObj = new JSONObject(json);
                    if (jsonObj != null) {
                        JSONArray frutas = jsonObj
                                .getJSONArray("frutas");

                        for (int i = 0; i < frutas.length(); i++) {
                            JSONObject catObj = (JSONObject) frutas.get(i);
                            Frutas cat = new Frutas(catObj.getInt("id"),
                                    catObj.getString("nombre"));
                            frutasList.add(cat);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.e("JSON Data", "¿No ha recibido ningún dato desde el servidor!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
           populateSpinner();
        }

    }



    private class AddNuevafruta extends AsyncTask<String, Void, Void> {

        boolean nuevaFrutaCreada = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("creación de la nueva fruta..");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... arg) {

            String newFruta = arg[0];


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("nombre", newFruta));

            ServiceHandler serviceClient = new ServiceHandler();

            String json = serviceClient.makeServiceCall(URL_NEW_FRUTA,
                    ServiceHandler.POST, params);

            Log.d("Create Response: ", "> " + json);

            if (json != null) {
                try {
                    JSONObject jsonObj = new JSONObject(json);
                    boolean error = jsonObj.getBoolean("error");

                    if (!error) {

                        nuevaFrutaCreada = true;
                    } else {
                        Log.e("Error en la creacion: ", "> " + jsonObj.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.e("JSON Data", "No ha recibido ningún dato desde el servidor!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            if (nuevaFrutaCreada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        new Getfrutas().execute();
                        frutasList.clear();
                    }
                });
            }
        }
    }


    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        Toast.makeText(
                getApplicationContext(),
                parent.getItemAtPosition(position).toString() + " Seleccionado" ,
                Toast.LENGTH_LONG).show();

    }


    public void onNothingSelected(AdapterView<?> arg0) {
    }
}

