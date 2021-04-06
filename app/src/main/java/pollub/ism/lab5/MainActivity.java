/*
Dokonane zmiany:
    - usunięcie znaku nowej linii z pierwszej odczytywanej linii
    + czyszczenie pola z nazwą zapisywanego pliku po udanym zapisie
 */

package pollub.ism.lab5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button btnZapis = null, btnOdczyt = null;
    private EditText editTxtNazwaZapis = null, editTxtNotatka = null;
    private Spinner spinnerNazwaCzytaj = null;

    private ArrayList <String> nazwyPlikow = null;
    private ArrayAdapter <String> adapterSpinera = null;

    private final String NAZWA_PREFERENCES = "Aplikacja do notatek";
    private final String KLUCZ_DO_PREFERENCES = "Zapisywanie nazwy plików";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //przyciski i pola
        btnZapis = (Button) findViewById(R.id.przyciskZapisz);
        btnOdczyt = (Button) findViewById(R.id.przyciskCzytaj);
        editTxtNazwaZapis = (EditText) findViewById(R.id.editTextNazwaZapisz);
        editTxtNotatka = (EditText) findViewById(R.id.editTextNotatka);
        spinnerNazwaCzytaj = (Spinner) findViewById(R.id.spinnerNazwaCzytaj);

        //podłączenie funkcji do kliknięć przycisków - LISTENERY
        btnZapis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zapisanieNotatki();
            }
        });

        btnOdczyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                odczytanieNotatki();
            }
        });
    }

    @Override
    protected void onPause() {

        zapiszSharePreferences();
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();

        nazwyPlikow = new ArrayList<>();
        adapterSpinera = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nazwyPlikow);
        spinnerNazwaCzytaj.setAdapter(adapterSpinera);

        odczytajSharePreferences();
    }

    private void zapisanieNotatki(){

        String nazwaPliku = editTxtNazwaZapis.getText().toString();
        String informacja = "Udało się zapisać";

        if (!zapiszDoPliku(nazwaPliku, editTxtNotatka)) {
            informacja = "Nie udało się zapisać";
        }
        else{
            editTxtNazwaZapis.setText("");
        }

        Toast.makeText(this, informacja, Toast.LENGTH_LONG).show();
    }

    private void odczytanieNotatki(){

        String nazwaPliku = spinnerNazwaCzytaj.getSelectedItem().toString();
        
        String informacja = "Udało się przeczytać";

        editTxtNotatka.getText().clear();

        if (!odczytajZPliku(nazwaPliku, editTxtNotatka)){
            informacja = "Nie udało się przeczytać";
        }

        Toast.makeText(this, informacja, Toast.LENGTH_SHORT).show();
    }

    private boolean zapiszDoPliku(String nazwaPliku, EditText poleEdycyjne){

        boolean sukces = true;

        File katalog = getApplicationContext().getExternalFilesDir(null);
        File plik = new File (katalog + File.separator  + nazwaPliku);
        BufferedWriter zapisywacz = null;

        try {
            zapisywacz = new BufferedWriter(new FileWriter(plik));
            zapisywacz.write(poleEdycyjne.getText().toString());
        } catch (Exception e){
            sukces = false;
        } finally {
            try {
                zapisywacz.close();
            } catch (Exception e) {
                sukces = false;
            }
        }

        if (sukces && !nazwyPlikow.contains(nazwaPliku)){
            nazwyPlikow.add(nazwaPliku);
            adapterSpinera.notifyDataSetChanged();
        }

        return sukces;
    }

    private boolean odczytajZPliku(String nazwaPliku, EditText poleEdycyjne){

        boolean sukces = true;

        File katalog = getApplicationContext().getExternalFilesDir(null);
        File plik = new File (katalog + File.separator  + nazwaPliku);
        BufferedReader odczytywacz = null;

        if(plik.exists()){
            try{
                odczytywacz = new BufferedReader(new FileReader(plik));

                String linia = odczytywacz.readLine();
                while (linia != null){
                    poleEdycyjne.getText().append(linia + "\n");
                    linia = odczytywacz.readLine();
                }

            } catch (Exception e){
                sukces = false;
            } finally {
                if (odczytywacz != null){
                    try {
                        odczytywacz.close();
                    } catch (Exception e) {
                        sukces = false;
                    }
                }
            }
        }

        return sukces;
    }

    private void zapiszSharePreferences(){

        SharedPreferences preferences = getSharedPreferences(NAZWA_PREFERENCES, MODE_PRIVATE);

        SharedPreferences.Editor edytor = preferences.edit();

        edytor.putStringSet(KLUCZ_DO_PREFERENCES, new HashSet<String>(nazwyPlikow));

        edytor.apply();
    }

    private void odczytajSharePreferences() {

        SharedPreferences sh = getSharedPreferences(NAZWA_PREFERENCES, MODE_PRIVATE);
        Set <String> zapisaneNazwy = sh.getStringSet(KLUCZ_DO_PREFERENCES, null);

        if (zapisaneNazwy != null){
            nazwyPlikow.clear();
            for (String nazwa : zapisaneNazwy){
                nazwyPlikow.add(nazwa);
            }
            adapterSpinera.notifyDataSetChanged();
        }
    }
}