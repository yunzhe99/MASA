package com.zcw.payview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MainActivity_validYes extends Activity{
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.valid_yes);
    }
	/*
	public MainActivity_validYes(Context context) {
		this(context, null);
	}
	public MainActivity_validYes(Context context, AttributeSet attrs) {
		super();
		this.context = context;
		View view = View.inflate(context, R.layout.valid_yes, null);
		showText=(TextView)findViewById(R.id.validYes_showtext);
	}
	*/
}
