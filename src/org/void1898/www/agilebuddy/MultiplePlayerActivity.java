package org.void1898.www.agilebuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.feiyang.agilebuddy.client.control.MultiplePlayerController;

/**
 * Created by sina-001 on 13-6-7.
 */
public class MultiplePlayerActivity extends Activity implements View.OnClickListener {
    private MultiplePlayerController controller;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.multiple);

        Button startButton = (Button) findViewById(R.id.start);
        startButton.setOnClickListener(this);

        controller = MultiplePlayerController.getInstance();
        controller.setMultiplePlayerActivity(this);
        controller.searching();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        Intent i = new Intent(this, AgileBuddyActivity.class);
        startActivity(i);
    }
}
