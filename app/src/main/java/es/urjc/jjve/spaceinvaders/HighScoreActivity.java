package es.urjc.jjve.spaceinvaders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import es.urjc.jjve.spaceinvaders.controllers.Score;
import es.urjc.jjve.spaceinvaders.controllers.ScoreManager;

public class HighScoreActivity extends AppCompatActivity implements OnClickListener  {

    private int score;
    private String fileScores;
    private String nombre;
    private Uri playerImageUri;

    private static final Logger LOGGER = Logger.getLogger("es.urjc.jjve.spaceinvaders.HighScoreActivity");

    @Override   //Este método se carga el primero en cuanto se llama a la activity
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        //Se inicializan los elementos de la pantalla
        View exit = findViewById(R.id.quitButton);
        View reiniciar = findViewById(R.id.reiniciar);
        reiniciar.setVisibility(View.INVISIBLE);

        TextView highScoreField = findViewById(R.id.highScore);
        TextView yourScore = findViewById(R.id.yourScore);

        //Se obtienen las puntuaciones
        score = getIntent().getExtras().getInt("score");
        nombre = getIntent().getExtras().getString("nombre");
        playerImageUri = (Uri) getIntent().getExtras().get("uri");

        ImageView foto = findViewById(R.id.fotoLastGame);
        try {
            foto.setImageBitmap(getBitmapFromUri(playerImageUri));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error al establecer la foto");
        }

        if (score>=500){
            reiniciar.setVisibility(View.VISIBLE);
        }
        fileScores = cargarScores();

        //Se pintan las listas de puntuaciones
        yourScore.setText("Tu puntuación: " + score);
        highScoreField.setText(fileScores);


        //Se agregan el OnClickListener para que el botón funcione al pulsarlo
        exit.setOnClickListener(this);
        reiniciar.setOnClickListener(this);
    }

    @Override   //Permite agregar funcionalidad de click a los objeto que tenga agregados
    public void onClick(View v) {

        //Botón de salir de la aplicación (quizás haya que cambiar de salir de la aplicación a volver a la pantalla de título)
        if(v.getId()== findViewById(R.id.quitButton).getId()) {
            Intent inicio = new Intent(getApplicationContext(),Inicio.class);
            startActivity(inicio);
            this.finishActivity(0);
        }
        //Boton para reiniciar el juego
        if(v.getId()== findViewById(R.id.reiniciar).getId()){
            Intent juegoNuevo = new Intent(getApplicationContext(),SpaceInvadersActivity.class);
            juegoNuevo.putExtra("underage",false); //TODO investigar esto
            startActivity(juegoNuevo);
            this.finishActivity(0);
        }
    }

    //Carga las puntuaciones almacenadas en el fichero
    public String cargarScores() {
        ScoreManager sm = new ScoreManager(this.getApplicationContext());
        TreeSet<Score> puntuaciones = sm.getScores();
        String scores = "";

        //Se genera las puntuaciones a mostrar "Nombre:Puntuación:URI"
        for(Score punts: puntuaciones){
            scores += punts.getName() + "-" + punts.getScore() + "\n";
        }

        return scores;
    }

    //Genera un URI como el método anterior pero maneja datos e imágenes mucho mayores
    public Bitmap getBitmapFromUri(Uri uri) throws IOException{
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > 100) ? (originalSize / 100) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }
    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

}
