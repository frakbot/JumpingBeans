package net.frakbot.jumpingbeans.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import net.frakbot.jumpingbeans.JumpingBeans;


public class MainActivity extends Activity {

    private JumpingBeans jumpingBeans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final TextView textView = (TextView) findViewById(R.id.jumping_text);
        jumpingBeans = new JumpingBeans.Builder()
            .appendJumpingDots(textView)
            .build();
    }

    @Override
    protected void onStop() {
        super.onStop();
        jumpingBeans.stopJumping();
    }
}
