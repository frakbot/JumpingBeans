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

package net.frakbot.jumpingbeans;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Provides "jumping beans" functionality for a TextView.
 * <p/>
 * Remember to call the {@link #stopJumping()} method once you're done
 * using the JumpingBeans (that is, when you detach the TextView from
 * the view tree, you hide it, or the parent Activity/Fragment goes in
 * the paused status). This will allow to release the animations and
 * free up memory and CPU that would be otherwise wasted.
 * <p/>
 * Please note that you:
 * <ul>
 * <li><b>Must not</b> try to change a jumping beans text in a textview before calling
 * {@link #stopJumping()} as to avoid unnecessary invalidation calls;
 * the JumpingBeans class cannot know when this happens and will keep
 * animating the textview (well, try to, anyway), wasting resources</li>
 * <li><b>Must not</b> try to use a jumping beans text in another view; it will not
 * animate. Just create another jumping beans animation for each new
 * view</li>
 * <li><b>Must not</b> use more than one JumpingBeans instance on a single TextView, as
 * the first cleanup operation called on any of these JumpingBeans will also cleanup
 * all other JumpingBeans' stuff. This is most likely not what you want to happen in
 * most cases.</li>
 * <li><b>Should not</b> use JumpingBeans on large chunks of text. Ideally this should
 * be done on small views with just a few words. We've strived to make it as inexpensive
 * as possible to use JumpingBeans but invalidating and possibly relayouting a large
 * TextView can be pretty expensive.</li>
 * </ul>
 */
public final class JumpingBeans {

    private static final String ELLIPSIS_GLYPH = "…";
    private static final String THREE_DOTS_ELLIPSIS = "...";
    private static final int THREE_DOTS_ELLIPSIS_LENGTH = 3;

    private final JumpingBeansSpan[] jumpingBeans;
    private final WeakReference<TextView> textView;

    private JumpingBeans(JumpingBeansSpan[] beans, TextView textView) {
        this.jumpingBeans = beans;
        this.textView = new WeakReference<>(textView);
    }

    /**
     * Create an instance of the {@link net.frakbot.jumpingbeans.JumpingBeans.Builder}
     * applied to the provided {@code TextView}.
     *
     * @param textView The TextView to apply the JumpingBeans to
     * @return the {@link net.frakbot.jumpingbeans.JumpingBeans.Builder}
     */
    public static Builder with(@NonNull TextView textView) {
        return new Builder(textView);
    }

    /**
     * Stops the jumping animation and frees up the animations.
     */
    public void stopJumping() {
        for (JumpingBeansSpan bean : jumpingBeans) {
            if (bean != null) {
                bean.teardown();
            }
        }

        cleanupSpansFrom(textView.get());
    }

    private static void cleanupSpansFrom(TextView textView) {
        if (textView == null) {
            return;
        }

        CharSequence text = textView.getText();
        if (text instanceof Spanned) {
            CharSequence cleanText = removeJumpingBeansSpansFrom((Spanned) text);
            textView.setText(cleanText);
        }
    }

    private static CharSequence removeJumpingBeansSpansFrom(Spanned text) {
        SpannableStringBuilder sbb = new SpannableStringBuilder(text.toString());
        Object[] spans = text.getSpans(0, text.length(), Object.class);
        for (Object span : spans) {
            if (!(span instanceof JumpingBeansSpan)) {
                sbb.setSpan(span, text.getSpanStart(span), text.getSpanEnd(span), text.getSpanFlags(span));
            }
        }
        return sbb;
    }

    /**
     * Builder class for {@link net.frakbot.jumpingbeans.JumpingBeans} objects.
     * <p/>
     * Provides a way to set the fields of a {@link JumpingBeans} and generate
     * the desired jumping beans effect. With this builder you can easily append
     * a Hangouts-style trio of jumping suspension points to any TextView, or
     * apply the effect to any other subset of a TextView's text.
     * <p/>
     * <p>Example:
     * <p/>
     * <pre class="prettyprint">
     * JumpingBeans jumpingBeans = JumpingBeans.with(myTextView)
     *   .appendJumpingDots()
     *   .setLoopDuration(1500)
     *   .build();
     * </pre>
     *
     * @see JumpingBeans#with(TextView)
     */
    public static class Builder {

        private static final float DEFAULT_ANIMATION_DUTY_CYCLE = 0.65f;
        private static final int DEFAULT_LOOP_DURATION = 1300;   // ms
        private static final int DEFAULT_WAVE_CHAR_DELAY = -1;

        private final TextView textView;

        private int startPos;
        private int endPos;

        private float animRange = DEFAULT_ANIMATION_DUTY_CYCLE;
        private int loopDuration = DEFAULT_LOOP_DURATION;
        private int waveCharDelay = DEFAULT_WAVE_CHAR_DELAY;
        private CharSequence text;
        private boolean wave;

        Builder(TextView textView) {
            this.textView = textView;
        }

        /**
         * Appends three jumping dots to the end of a TextView text.
         * <p/>
         * This implies that the animation will by default be a wave.
         * <p/>
         * If the TextView has no text, the resulting TextView text will
         * consist of the three dots only.
         * <p/>
         * The TextView text is cached to the current value at
         * this time and set again in the {@link #build()} method, so any
         * change to the TextView text done in the meantime will be lost.
         * This means that <b>you should do all changes to the TextView text
         * <i>before</i> you begin using this builder.</b>
         * <p/>
         * Call the {@link #build()} method once you're done to get the
         * resulting {@link net.frakbot.jumpingbeans.JumpingBeans}.
         *
         * @see #setIsWave(boolean)
         */
        @NonNull
        public Builder appendJumpingDots() {
            CharSequence text = appendThreeDotsEllipsisTo(textView);

            this.text = text;
            this.wave = true;
            this.startPos = text.length() - THREE_DOTS_ELLIPSIS_LENGTH;
            this.endPos = text.length();

            return this;
        }

        private static CharSequence appendThreeDotsEllipsisTo(TextView textView) {
            CharSequence text = getTextSafe(textView);
            if (text.length() > 0 && endsWithEllipsisGlyph(text)) {
                text = text.subSequence(0, text.length() - 1);
            }

            if (!endsWithThreeEllipsisDots(text)) {
                text = new SpannableStringBuilder(text).append(THREE_DOTS_ELLIPSIS);  // Preserve spans in original text
            }
            return text;
        }

        private static CharSequence getTextSafe(TextView textView) {
            return !TextUtils.isEmpty(textView.getText()) ? textView.getText() : "";
        }

        private static boolean endsWithEllipsisGlyph(CharSequence text) {
            CharSequence lastChar = text.subSequence(text.length() - 1, text.length());
            return ELLIPSIS_GLYPH.equals(lastChar);
        }

        @SuppressWarnings("SimplifiableIfStatement")                 // For readability
        private static boolean endsWithThreeEllipsisDots(CharSequence text) {
            if (text.length() < THREE_DOTS_ELLIPSIS_LENGTH) {
                // TODO we should try to normalize "invalid" ellipsis (e.g., ".." or "....")
                return false;
            }
            CharSequence lastThreeChars = text.subSequence(text.length() - THREE_DOTS_ELLIPSIS_LENGTH, text.length());
            return THREE_DOTS_ELLIPSIS.equals(lastThreeChars);
        }

        /**
         * Appends three jumping dots to the end of a TextView text.
         * <p/>
         * This implies that the animation will by default be a wave.
         * <p/>
         * If the TextView has no text, the resulting TextView text will
         * consist of the three dots only.
         * <p/>
         * The TextView text is cached to the current value at
         * this time and set again in the {@link #build()} method, so any
         * change to the TextView text done in the meantime will be lost.
         * This means that <b>you should do all changes to the TextView text
         * <i>before</i> you begin using this builder.</b>
         * <p/>
         * Call the {@link #build()} method once you're done to get the
         * resulting {@link net.frakbot.jumpingbeans.JumpingBeans}.
         *
         * @param startPos The position of the first character to animate
         * @param endPos   The position after the one the animated range ends at
         *                 (just like in {@link String#substring(int)})
         * @see #setIsWave(boolean)
         */
        @NonNull
        public Builder makeTextJump(@IntRange(from = 0) int startPos, @IntRange(from = 0) int endPos) {
            CharSequence text = textView.getText();
            ensureTextCanJump(startPos, endPos, text);

            this.text = text;
            this.wave = true;
            this.startPos = startPos;
            this.endPos = endPos;

            return this;
        }

        private static CharSequence ensureTextCanJump(int startPos, int endPos, CharSequence text) {
            if (text == null) {
                throw new NullPointerException("The textView text must not be null");
            }

            if (endPos < startPos) {
                throw new IllegalArgumentException("The start position must be smaller than the end position");
            }

            if (startPos < 0) {
                throw new IndexOutOfBoundsException("The start position must be non-negative");
            }

            if (endPos > text.length()) {
                throw new IndexOutOfBoundsException("The end position must be smaller than the text length");
            }
            return text;
        }

        /**
         * Sets the fraction of the animation loop time spent actually animating.
         * The rest of the time will be spent "resting".
         *
         * @param animatedRange The fraction of the animation loop time spent
         *                      actually animating the characters
         */
        @NonNull
        public Builder setAnimatedDutyCycle(@FloatRange(from = 0f, to = 1f, fromInclusive = false) float animatedRange) {
            if (animatedRange <= 0f || animatedRange > 1f) {
                throw new IllegalArgumentException("The animated range must be in the (0, 1] range");
            }
            this.animRange = animatedRange;
            return this;
        }

        /**
         * Sets the jumping loop duration.
         *
         * @param loopDuration The jumping animation loop duration, in milliseconds
         */
        @NonNull
        public Builder setLoopDuration(@IntRange(from = 1) int loopDuration) {
            if (loopDuration < 1) {
                throw new IllegalArgumentException("The loop duration must be bigger than zero");
            }
            this.loopDuration = loopDuration;
            return this;
        }

        /**
         * Sets the delay for starting the animation of every single dot over the
         * start of the previous one, in milliseconds. The default value is
         * the loop length divided by three times the number of character animated
         * by this instance of JumpingBeans.
         * <p/>
         * Only has a meaning when the animation is a wave.
         *
         * @param waveCharOffset The start delay for the animation of every single
         *                       character over the previous one, in milliseconds
         * @see #setIsWave(boolean)
         */
        @NonNull
        public Builder setWavePerCharDelay(@IntRange(from = 0) int waveCharOffset) {
            if (waveCharOffset < 0) {
                throw new IllegalArgumentException("The wave char offset must be non-negative");
            }
            this.waveCharDelay = waveCharOffset;
            return this;
        }

        /**
         * Sets a flag that determines if the characters will jump in a wave
         * (i.e., with a delay between each other) or all at the same
         * time.
         *
         * @param wave If true, the animation is going to be a wave; if
         *             false, all characters will jump ay the same time
         * @see #setWavePerCharDelay(int)
         */
        @NonNull
        public Builder setIsWave(boolean wave) {
            this.wave = wave;
            return this;
        }

        /**
         * Combine all of the options that have been set and return a new
         * {@link net.frakbot.jumpingbeans.JumpingBeans} instance.
         * <p/>
         * Remember to call the {@link #stopJumping()} method once you're done
         * using the JumpingBeans (that is, when you detach the TextView from
         * the view tree, you hide it, or the parent Activity/Fragment goes in
         * the paused status). This will allow to release the animations and
         * free up memory and CPU that would be otherwise wasted.
         */
        @NonNull
        public JumpingBeans build() {
            SpannableStringBuilder sbb = new SpannableStringBuilder(text);
            JumpingBeansSpan[] spans;
            if (wave) {
                spans = buildWavingSpans(sbb);
            } else {
                spans = buildSingleSpan(sbb);
            }

            textView.setText(sbb);
            return new JumpingBeans(spans, textView);
        }

        @SuppressWarnings("Range")          // Lint bug: the if makes sure waveCharDelay >= 0
        private JumpingBeansSpan[] buildWavingSpans(SpannableStringBuilder sbb) {
            JumpingBeansSpan[] spans;
            if (waveCharDelay == DEFAULT_WAVE_CHAR_DELAY) {
                waveCharDelay = loopDuration / (3 * (endPos - startPos));
            }

            spans = new JumpingBeansSpan[endPos - startPos];
            for (int pos = startPos; pos < endPos; pos++) {
                JumpingBeansSpan jumpingBean =
                        new JumpingBeansSpan(textView, loopDuration, pos - startPos, waveCharDelay, animRange);
                sbb.setSpan(jumpingBean, pos, pos + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spans[pos - startPos] = jumpingBean;
            }
            return spans;
        }

        private JumpingBeansSpan[] buildSingleSpan(SpannableStringBuilder sbb) {
            JumpingBeansSpan[] spans;
            spans = new JumpingBeansSpan[]{new JumpingBeansSpan(textView, loopDuration, 0, 0, animRange)};
            sbb.setSpan(spans[0], startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spans;
        }

    }

}
