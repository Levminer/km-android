package com.nje.km;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentált teszt, amely Android eszközön fut.
 *
 * @see <a href="http://d.android.com/tools/testing">Tesztelési dokumentáció</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // A tesztelt alkalmazás kontextusa.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.nje.km", appContext.getPackageName());
    }
}