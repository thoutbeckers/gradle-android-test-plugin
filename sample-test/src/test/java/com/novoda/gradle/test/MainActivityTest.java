package com.novoda.gradle.test;

import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
public class MainActivityTest {

    @Test
    public void testFindViewById() throws Exception {
        MainActivity activity = new MainActivity();
        activity.onCreate(null);
        TextView text = (TextView) activity.findViewById(R.id.text);
        assertEquals(text.getText().toString(), activity.getString(R.string.hello_world));
    }
}
