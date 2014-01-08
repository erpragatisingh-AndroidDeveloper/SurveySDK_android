package com.survey.android.custom_widgets;

import com.survey.android.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class FontTextView extends TextView {

	private boolean rotated = false;

	public boolean isRotated() {
		return this.rotated;
	}

	public void setRotated(boolean rotated) {
		this.rotated = rotated;
	}

	public FontTextView(Context context) {
		super(context);
		initiliazeFont(context);
	}

	public FontTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initiliazeFont(context);
	}

	public FontTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initiliazeFont(context);
	}

	private void initiliazeFont(Context context) {
		Typeface tf = Typeface.createFromAsset(context.getAssets(),
				"fonts/Trebuchet_MS.ttf");
		this.setTypeface(tf);
		this.setTextColor(context.getResources().getColor(R.color.text));
		this.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_medium));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!rotated) {
			super.onDraw(canvas);
		} else {
			canvas.save();
			canvas.rotate(-80, 1, 1);
			super.onDraw(canvas);
			canvas.restore();
		}

	}

}
