package com.zcw.payview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MainActivity_afterTrain extends Activity {
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_train);
    }
	
	/*
	public MainActivity_afterTrain(Context context) {
		this(context, null);
	}
	public MainActivity_afterTrain(Context context, AttributeSet attrs) {
		super();
		this.context = context;
		View view = View.inflate(context, R.layout.after_train, null);
		showText = (TextView)findViewById(R.id.afterTrain_showtext);
	}
	*/
}
