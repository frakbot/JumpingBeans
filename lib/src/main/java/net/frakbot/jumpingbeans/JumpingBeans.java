package net.frakbot.jumpingbeans;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.TextView;

/**
 * Provides "jumping beans" functionality for a TextView.
 * <p/>
 * Remember to call the {@link #stopJumping()} method once you're done
 * using the JumpingBeans (that is, when you detach the TextView from
 * the view tree, you hide it, or the parent Activity/Fragment goes in
 * the paused status). This will allow to release the animations and
 * free up memory and CPU that would be otherwise wasted.
 */
public final class JumpingBeans {

    public static final int DEFAULT_LOOP_DURATION = 1000;
    public static final int DEFAULT_WAVE_CHAR_DELAY = 100;

    private JumpingBeansSpan[] jumpingBeans;

    private JumpingBeans(JumpingBeansSpan[] beans) {
        // Clients will have to use the builder
        jumpingBeans = beans;
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
     * JumpingBeans jumpingBeans = new JumpingBeans.Builder()
     * .appendJumpingDots(myTextView)
     * .setLoopDuration(1500)
     * .build();
     * </pre>
     */
    public static class Builder {

        private int startPos, endPos;
        private int loopDuration = DEFAULT_LOOP_DURATION;
        private int waveCharOffset = DEFAULT_WAVE_CHAR_DELAY;
        private CharSequence text;
        private TextView textView;
        private boolean wave;

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
         * @param textView The TextView to append the dots to
         *
         * @see #setIsWave(boolean)
         */
        public Builder appendJumpingDots(TextView textView) {
            if (textView == null) {
                throw new NullPointerException("The textView must not be null");
            }

            CharSequence text = !TextUtils.isEmpty(textView.getText()) ? textView.getText() : "";
            if (text.length() > 0 && text.subSequence(text.length() - 1, text.length()).equals("…")) {
                text = text.subSequence(0, text.length() - 1);
            }

            if (text.length() < 3 || !TextUtils.equals(text.subSequence(text.length() - 3, text.length()), "...")) {
                text = new SpannableStringBuilder(text).append("...");  // Preserve spans in original text
            }

            this.text = text;
            this.wave = true;
            this.textView = textView;
            this.startPos = this.text.length() - 3;
            this.endPos = this.text.length();
            return this;
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
         * @param textView The TextView whose text is to be animated
         * @param startPos The position of the first character to animate
         * @param endPos   The position after the one the animated range ends at
         *                 (just like in String#substring())
         *
         * @see #setIsWave(boolean)
         */
        public Builder makeTextJump(TextView textView, int startPos, int endPos) {
            if (textView == null || textView.getText() == null) {
                throw new NullPointerException("The textView and its text must not be null");
            }

            if (endPos < startPos) {
                throw new IllegalArgumentException("The start position must be smaller than the end position");
            }

            if (startPos < 0) {
                throw new IndexOutOfBoundsException("The start position must be non-negative");
            }

            this.text = textView.getText();
            if (endPos > text.length()) {
                throw new IndexOutOfBoundsException("The end position must be smaller than the text length");
            }

            this.wave = true;
            this.textView = textView;
            this.startPos = startPos;
            this.endPos = endPos;
            return this;
        }

        /**
         * Sets the jumping loop duration. The default value is
         * {@link net.frakbot.jumpingbeans.JumpingBeans#DEFAULT_LOOP_DURATION}.
         *
         * @param loopDuration The jumping animation loop duration, in milliseconds
         */
        public void setLoopDuration(int loopDuration) {
            if (loopDuration < 1) {
                throw new IllegalArgumentException("The loop duration must be bigger than zero");
            }
            this.loopDuration = loopDuration;
        }

        /**
         * Sets the delay for starting the animation of every single dot over the
         * start of the previous one, in milliseconds. The default value is
         * {@link net.frakbot.jumpingbeans.JumpingBeans#DEFAULT_WAVE_CHAR_DELAY}.
         * <p/>
         * Only has a meaning when the animation is a wave.
         *
         * @param waveCharOffset The start delay for the animation of every single
         *                       character over the previous one, in milliseconds
         *
         * @see #setIsWave(boolean)
         */
        public void setWaveCharOffset(int waveCharOffset) {
            if (waveCharOffset < 0) {
                throw new IllegalArgumentException("The wave char offset must be non-negative");
            }
            this.waveCharOffset = waveCharOffset;
        }

        /**
         * Sets a flag that determines if the characters will jump in a wave
         * (i.e., with a delay between each other) or all at the same
         * time.
         *
         * @param wave If true, the animation is going to be a wave; if
         *             false, all characters will jump ay the same time
         *
         * @see #setWaveCharOffset(int)
         */
        public void setIsWave(boolean wave) {
            this.wave = wave;
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
        public JumpingBeans build() {
            SpannableStringBuilder sbb = new SpannableStringBuilder(text);
            JumpingBeansSpan[] jumpingBeans;
            if (!wave) {
                jumpingBeans = new JumpingBeansSpan[] {new JumpingBeansSpan(textView, loopDuration, 0, 0)};
                sbb.setSpan(jumpingBeans[0], startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else {
                jumpingBeans = new JumpingBeansSpan[endPos - startPos];
                for (int pos = startPos; pos < endPos; pos++) {
                    JumpingBeansSpan jumpingBean =
                        new JumpingBeansSpan(textView, loopDuration, pos - startPos, waveCharOffset);
                    sbb.setSpan(jumpingBean, pos, pos + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    jumpingBeans[pos - startPos] = jumpingBean;
                }
            }

            textView.setText(sbb);
            return new JumpingBeans(jumpingBeans);
        }
    }
}
