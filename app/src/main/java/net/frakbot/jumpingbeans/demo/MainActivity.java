package net.frakbot.jumpingbeans.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.TextView;
import net.frakbot.jumpingbeans.JumpingBeansSpan;


public class MainActivity extends Activity {

    private JumpingBeansSpan[] jumpingBeans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.jumping_text);
        final String message = getString(R.string.message);
        SpannableStringBuilder sbb = new SpannableStringBuilder(message);
        jumpingBeans = new JumpingBeansSpan[] {new JumpingBeansSpan(textView, 0),
                                               new JumpingBeansSpan(textView, 1),
                                               new JumpingBeansSpan(textView, 2)};
        sbb.setSpan(jumpingBeans[0], message.length() - 3, message.length() - 2,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sbb.setSpan(jumpingBeans[1], message.length() - 2, message.length() - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        sbb.setSpan(jumpingBeans[2], message.length() - 1, message.length(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(sbb);
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (JumpingBeansSpan bean : jumpingBeans) {
            bean.teardown();
        }
    }
}
