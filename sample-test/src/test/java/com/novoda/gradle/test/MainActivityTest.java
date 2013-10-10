package com.novoda.gradle.test;

import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    @Test
    public void testFindViewById() throws Exception {
        MainActivity activity = new MainActivity();
        activity.onCreate(null);
        TextView text = (TextView) activity.findViewById(R.id.text);
        assertEquals(text.getText().toString(), "Hello world!");
    }
}
