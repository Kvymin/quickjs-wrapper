package com.whl.quickjs.wrapper;

import com.whl.quickjs.android.QuickJSLoader;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Harlon Wang on 2022/11/21.
 */
public class QuickJSRuntimeTest {

    QuickJSContext context;

    @Before
    public void initSo() {
        QuickJSLoader.init();
    }

    @Test
    public void testRuntimeDestroy() {
        context = QuickJSContext.create();
        byte[] code = context.compile("'hello, world!'.toUpperCase();");
        context.destroy();
        QuickJSContext.destroyRuntime(context);

        context = QuickJSContext.create();
        Object hello = context.execute(code);
        assertEquals(hello, "HELLO, WORLD!");
        context.destroy();
        QuickJSContext.destroyRuntime(context);
    }

    @Test
    public void testRuntimeNotEquals() {
        QuickJSContext context = QuickJSContext.create(QuickJSContext.createRuntime());
        QuickJSContext context1 = QuickJSContext.create(QuickJSContext.createRuntime());

        assertNotEquals(context.getRuntime(), context1.getRuntime());

        context.destroy();
        QuickJSContext.destroyRuntime(context.getRuntime());
        context1.destroy();
        QuickJSContext.destroyRuntime(context1.getRuntime());
    }

    @Test
    public void testRuntimeEquals() {
        QuickJSContext context = QuickJSContext.create(QuickJSContext.createRuntime());
        QuickJSContext context1 = QuickJSContext.create(context.getRuntime());

        assertEquals(context.getRuntime(), context1.getRuntime());

        context.destroy();
        context1.destroy();
        QuickJSContext.destroyRuntime(context.getRuntime());
    }

}
