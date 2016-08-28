package bluejay.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent go = new Intent(MainActivity.this, DemoActivity.class);

        findViewById(R.id.shoes_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(go.putExtra("DEMO", "shoes"));
            }
        });

        findViewById(R.id.music_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(go.putExtra("DEMO", "music"));
            }
        });
    }

}
