package bluejay.sdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.android.gms.ads.AdView;

public class DemoActivity extends AppCompatActivity {

    Bluejay bluejay = new Bluejay(this);

    void prepare() {
        setContentView(R.layout.activity_demo);
        String demo = getIntent().getStringExtra("DEMO");
        if ("music".equals(demo)) {
            ((ImageView) findViewById(R.id.bg)).setImageResource(R.mipmap.music);
        } else {
            ((ImageView) findViewById(R.id.bg)).setImageResource(R.mipmap.shoes);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepare();

        // Intializes the Bluejay Ads SDK
        bluejay.initialize();

        // Loads Google ads into your existing AdView
        AdView adView = (AdView) findViewById(R.id.adView);
        bluejay.loadAd(adView);
    }
}
