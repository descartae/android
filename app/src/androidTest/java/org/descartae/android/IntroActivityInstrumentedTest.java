package org.descartae.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntroActivityInstrumentedTest {

    @Test
    public void useAppContext() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("org.descartae.android", appContext.getPackageName());
    }

    @Test
    public void pages() throws Exception {

    }
}
