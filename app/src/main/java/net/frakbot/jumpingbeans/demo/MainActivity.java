package net.frakbot.jumpingbeans.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import net.frakbot.jumpingbeans.JumpingBeans;

public class MainActivity extends Activity {

    private JumpingBeans jumpingBeans1, jumpingBeans2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Here you can see that we don't duplicate trailing dots on the text (we reuse
        // them or, if it's an ellipsis character, replace it with three dots and animate
        // those instead)
        final TextView textView1 = (TextView) findViewById(R.id.jumping_text_1);
        jumpingBeans1 = new JumpingBeans.Builder()
                .appendJumpingDots(textView1)
                .build();

        // Note that, even though we access textView2's text when starting to work on
        // the animation builder, we don't alter it in any way, so we're ok
        final TextView textView2 = (TextView) findViewById(R.id.jumping_text_2);
        jumpingBeans2 = new JumpingBeans.Builder()
                .makeTextJump(textView2, 0, textView2.getText().toString().indexOf(' '))
                .setIsWave(false)
                .setLoopDuration(1000)
                .build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        jumpingBeans1.stopJumping();
        jumpingBeans2.stopJumping();
    }
}
