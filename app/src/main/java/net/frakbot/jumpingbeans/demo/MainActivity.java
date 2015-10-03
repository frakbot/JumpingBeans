/*
 * Copyright 2015 Frakbot (Sebastiano Poggi and Francesco Pontillo)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.frakbot.jumpingbeans.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import net.frakbot.jumpingbeans.JumpingBeans;

public class MainActivity extends AppCompatActivity {

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
        jumpingBeans1 = JumpingBeans.with(textView1)
                .appendJumpingDots()
                .build();

        // Note that, even though we access textView2's text when starting to work on
        // the animation builder, we don't alter it in any way, so we're ok
        final TextView textView2 = (TextView) findViewById(R.id.jumping_text_2);
        jumpingBeans2 = JumpingBeans.with(textView2)
                .makeTextJump(0, textView2.getText().toString().indexOf(' '))
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
