package net.frakbot.jumpingbeans;

import android.animation.ValueAnimator;
import android.text.TextPaint;
import android.text.style.SuperscriptSpan;
import android.widget.TextView;

/*package*/ final class JumpingBeansSpan extends SuperscriptSpan implements ValueAnimator.AnimatorUpdateListener {

    private ValueAnimator jumpAnimator;
    private TextView textView;
    private int shift;
    private int delay;
    private int loopDuration;

    public JumpingBeansSpan(TextView textView, int loopDuration,  int position, int waveCharOffset) {
        this.textView = textView;
        this.delay = waveCharOffset * position;
        this.loopDuration = loopDuration;
    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        initIfNecessary(tp);
        tp.baselineShift = shift;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        initIfNecessary(tp);
        tp.baselineShift = shift;
    }

    private void initIfNecessary(TextPaint tp) {
        if (jumpAnimator != null) {
            return;
        }

        shift = (int) tp.ascent() / 2;
        jumpAnimator = ValueAnimator.ofInt(0, shift, 0, 0);
        jumpAnimator
            .setDuration(loopDuration)
            .setStartDelay(delay);
        jumpAnimator.setRepeatCount(ValueAnimator.INFINITE);
        jumpAnimator.setRepeatMode(ValueAnimator.RESTART);
        jumpAnimator.addUpdateListener(this);
        jumpAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        // No need for synchronization as this always run on main thread anyway
        if (textView.getParent() != null) {
            shift = (int) animation.getAnimatedValue();
            textView.invalidate();
        }
        else {
            animation.setCurrentPlayTime(0);
            animation.start();
        }
    }

    /*package*/ void teardown() {
        if (jumpAnimator != null) {
            jumpAnimator.cancel();
            jumpAnimator.removeAllListeners();
        }
        shift = 0;
        textView.invalidate();
    }
}
