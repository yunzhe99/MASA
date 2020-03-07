package com.zcw.payview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    private PasswordView pwdView;
    

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         pwdView = new PasswordView(this);
        setContentView(pwdView);
        pwdView.jump_on_send(new PasswordView.OnPasswordInputFinish() {        	
            @Override
            public void afterTrain() {
            	Intent intent = new Intent(MainActivity.this, MainActivity_afterTrain.class);
                startActivity(intent);
            }
            public void validYes() {
            	Intent intent = new Intent(MainActivity.this, MainActivity_validYes.class);
                startActivity(intent);
            }
            public void validNo() {
            	Intent intent = new Intent(MainActivity.this, MainActivity_validNo.class);
                startActivity(intent);
            }
        });
    }
	
}
