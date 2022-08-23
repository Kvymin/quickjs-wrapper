package com.whl.quickjs.wrapper;

import android.text.TextUtils;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

import com.whl.quickjs.android.QuickJSLoader;

public class QuickJSTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void initSo() {
        QuickJSLoader.init();
    }

    @Test
    public void createQuickJSContextTest() {
        QuickJSContext context = QuickJSContext.create();
        context.destroyContext();
    }

    @Test
    public void destroyQuickJSContextTest() {
        QuickJSContext context = QuickJSContext.create();
        context.evaluate("var a = 123;");

        JSObject gloObj = context.getGlobalObject();
        gloObj.release();

        JSObject globalObject = context.getGlobalObject();
        assertEquals(123, globalObject.getProperty("a"));
        context.destroyContext();
    }

    @Test
    public void setPropertyTest() {
        QuickJSContext context = QuickJSContext.create();
        JSObject globalObj = context.getGlobalObject();
        JSObject obj1 = context.createNewJSObject();
        obj1.setProperty("stringProperty", "hello");
        obj1.setProperty("intProperty", 1);
        obj1.setProperty("doubleProperty", 0.1);
        obj1.setProperty("booleanProperty", true);
        obj1.setProperty("functionProperty", (JSCallFunction) args -> args[0] + "Wang");
        obj1.setProperty("nullProperty", (String) null);
        globalObj.setProperty("obj1", obj1);

        assertEquals("hello", context.evaluate("obj1.stringProperty;"));
        assertEquals(1, context.evaluate("obj1.intProperty;"));
        assertEquals(0.1, context.evaluate("obj1.doubleProperty;"));
        assertEquals(true, context.evaluate("obj1.booleanProperty;"));
        assertEquals("HarlonWang", context.evaluate("obj1.functionProperty(\"Harlon\");"));
        assertNull(context.evaluate("obj1.nullProperty;"));

        context.destroyContext();
    }

    @Test
    public void getPropertyTest() {
        QuickJSContext context = QuickJSContext.create();
        context.evaluate("var obj1 = {\n" +
                "\tstringProperty: 'hello',\n" +
                "\tintProperty: 1,\n" +
                "\tdoubleProperty: 0.1,\n" +
                "\tbooleanProperty: true,\n" +
                "\tnullProperty: null,\n" +
                "\tfunctionProperty: (name) => { return name + 'Wang'; }\n" +
                "}");
        JSObject globalObject = context.getGlobalObject();
        JSObject obj1 = globalObject.getJSObject("obj1");
        assertEquals("hello", obj1.getProperty("stringProperty"));
        assertEquals(1, obj1.getProperty("intProperty"));
        assertEquals(0.1, obj1.getProperty("doubleProperty"));
        assertEquals(true, obj1.getProperty("booleanProperty"));
        assertNull(obj1.getProperty("nullProperty"));
        assertEquals("HarlonWang", obj1.getJSFunction("functionProperty").call("Harlon"));

        context.destroyContext();
    }

    @Test
    public void getJSArrayTest() {
        QuickJSContext context = QuickJSContext.create();
        JSArray ret = (JSArray) context.evaluate("function test(value) {\n" +
                "\treturn [1, 2, value];\n" +
                "}\n" +
                "\n" +
                "test(3);");
        assertEquals(3, ret.get(2));

        context.destroyContext();
    }

    @Test
    public void JSFunctionArgsTest() {
        QuickJSContext context = QuickJSContext.create();
        context.evaluate("function test(intValue, stringValue, doubleValue, booleanValue) {\n" +
                "\treturn \"hello, \" + intValue + stringValue + doubleValue + booleanValue;\n" +
                "}");
        JSObject globalObject = context.getGlobalObject();
        JSFunction func = (JSFunction) globalObject.getProperty("test");
        assertEquals("hello, 1string123.11true", func.call(1, "string", 123.11, true));

        context.destroyContext();
    }

    @Test
    public void JSFunctionNullArgsTest() {
        QuickJSContext context = QuickJSContext.create();
        context.evaluate("function test(arg1, arg2, arg3) {\n" +
                "\treturn \"hello, \" + arg1 + arg2 + arg3;\n" +
                "}");
        JSObject globalObject = context.getGlobalObject();
        JSFunction func = (JSFunction) globalObject.getProperty("test");
        assertEquals("hello, undefined-13", func.call(null, -1, 3));

        context.destroyContext();
    }

    @Test
    public void JSFunctionArgsTestWithUnSupportType() {
        QuickJSContext context = QuickJSContext.create();
        context.evaluate("function test(name) {\n" +
                "\treturn \"hello, \" + name;\n" +
                "}");
        JSObject globalObject = context.getGlobalObject();
        JSFunction func = (JSFunction) globalObject.getProperty("test");
        try {
            func.call(new int[]{1, 2});
            fail();
        } catch (Exception e) {
            assertTrue(e.toString().contains("Unsupported Java type"));
        }

        context.destroyContext();
    }

    @Test
    public void setConsoleLogTest() {
        QuickJSContext context = QuickJSContext.create();
        context.evaluate("var console = {};");
        JSObject console = (JSObject) context.getGlobalObject().getProperty("console");
        console.setProperty("log", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                StringBuilder b = new StringBuilder();
                for (Object o: args) {
                    b.append(o == null ? "null" : o.toString());
                }

                assertEquals("123", b.toString());
                return null;
            }
        });

        context.evaluate("console.log(123)");

        context.destroyContext();
    }

    @Test
    public void arrowFuncTest() {
        // set console.log
        QuickJSContext context = QuickJSContext.create();
        context.evaluate("var console = {};");
        JSObject console = (JSObject) context.getGlobalObject().getProperty("console");
        console.setProperty("log", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                StringBuilder b = new StringBuilder();
                for (Object o: args) {
                    b.append(o == null ? "null" : o.toString());
                }

                Log.d("tiny-console", b.toString());
                return null;
            }
        });


        context.evaluate("function test(index) {\n" +
                "\tconsole.log(index);\n" +
                "}\n" +
                "\n" +
                "function render() {\n" +
                "\n" +
                "\tvar index = 123;\n" +
                "\tvar invokeTest = () => {\n" +
                "\t\ttest(index);\n" +
                "\t}\n" +
                "\n" +
                "\treturn {\n" +
                "\t\tfunc: invokeTest\n" +
                "\t};\n" +
                "}");

        JSObject jsObj = (JSObject) context.evaluate("render();");
        JSFunction jsFunction = (JSFunction) jsObj.getProperty("func");
        jsFunction.call();

        context.destroyContext();
    }

    @Test
    public void setPropertyWithJSObjectTest() {
        QuickJSContext context = QuickJSContext.create();
        context.evaluate("var test = {count: 0};");
        context.getGlobalObject().setProperty("test1", (JSObject) context.getGlobalObject().getProperty("test"));

        assertEquals("{\"count\":0}", context.getGlobalObject().getJSObject("test1").stringify());
        context.destroyContext();
    }

    @Test
    public void jsonParseTest() {
        String text = "{\"phoneNumber\":\"呼叫 18505815627\",\"leadsId\":\"270\",\"leadsBizId\":\"xxx\",\"options\":[{\"type\":\"aliyun\",\"avatarUrl\":\"https://gw.alicdn.com/tfs/TB1BYz0vpYqK1RjSZLeXXbXppXa-187-187.png\",\"personName\":\"老板\",\"storeName\":\"小店名称\",\"title\":\"智能办公电话\",\"content\":\"免费拨打\"},{\"type\":\"direct\",\"title\":\"普通电话\",\"content\":\"运营商拨打\"}]}";

        QuickJSContext context = QuickJSContext.create();
        JSObject result = context.parseJSON(text);
        assertEquals("270", result.getProperty("leadsId"));

        context.getGlobalObject().setProperty("test", result);
        assertEquals(text, context.getGlobalObject().getJSObject("test").stringify());

        context.destroyContext();
    }

    @Test
    public void jsonParseTest3() {
        String text = "{\"phoneNumber\":\"呼叫 18505815627\",\"leadsId\":\"270\",\"leadsBizId\":\"xxx\",\"options\":[{\"type\":\"aliyun\",\"avatarUrl\":\"https://gw.alicdn.com/tfs/TB1BYz0vpYqK1RjSZLeXXbXppXa-187-187.png\",\"personName\":\"老板\",\"storeName\":\"小店名称\",\"title\":\"智能办公电话\",\"content\":\"免费拨打\"},{\"type\":\"direct\",\"title\":\"普通电话\",\"content\":\"运营商拨打\"}]}\n";
        QuickJSContext context = QuickJSContext.create();
        JSObject a = (JSObject) context.evaluate("var a = {}; a;");
        a.setProperty("test", context.parseJSON(text));
        Object ret = context.evaluate("a.test.leadsId;");
        assertEquals("270", ret);
        context.destroyContext();
    }

    @Test
    public void jsonParseTest4() {
        String text = "{\"phoneNumber\":\"呼叫 18505815627\",\"leadsId\":\"270\",\"leadsBizId\":\"xxx\",\"options\":[{\"type\":\"aliyun\",\"avatarUrl\":\"https://gw.alicdn.com/tfs/TB1BYz0vpYqK1RjSZLeXXbXppXa-187-187.png\",\"personName\":\"老板\",\"storeName\":\"小店名称\",\"title\":\"智能办公电话\",\"content\":\"免费拨打\"},{\"type\":\"direct\",\"title\":\"普通电话\",\"content\":\"运营商拨打\"}]}\n";
        QuickJSContext context = QuickJSContext.create();
        JSObject a = (JSObject) context.evaluate("var a = {b: {}}; a;");
        a.getJSObject("b").setProperty("test", context.parseJSON(text));
        Object ret = context.evaluate("a.b.test.leadsId;");
        assertEquals("270", ret);
        context.destroyContext();
    }

    @Test
    public void jsonParseTest5() {
        String text = "{\"phoneNumber\":\"呼叫 18505815627\",\"leadsId\":\"270\",\"leadsBizId\":\"xxx\",\"options\":[{\"type\":\"aliyun\",\"avatarUrl\":\"https://gw.alicdn.com/tfs/TB1BYz0vpYqK1RjSZLeXXbXppXa-187-187.png\",\"personName\":\"老板\",\"storeName\":\"小店名称\",\"title\":\"智能办公电话\",\"content\":\"免费拨打\"},{\"type\":\"direct\",\"title\":\"普通电话\",\"content\":\"运营商拨打\"}]}";
        QuickJSContext context = QuickJSContext.create();
        context.getGlobalObject().setProperty("test", (JSCallFunction) args -> context.parseJSON(text));

        JSObject ret = (JSObject) context.evaluate("var a = test(); a;");
        assertEquals(text, ret.stringify());
        context.destroyContext();
    }

    @Test
    public void testFlat() {
        QuickJSContext context = QuickJSContext.create();
        JSArray ret = (JSArray) context.evaluate("let a = [1,[2,3]];  \n" +
                "a = a.flat();\n" +
                "a;");

        assertEquals(1, ret.get(0));
        assertEquals(2, ret.get(1));
        assertEquals(3, ret.get(2));

        context.destroyContext();
    }

    @Test
    public void testClass() {
        QuickJSContext context = QuickJSContext.create();
        Object ret = context.evaluate("class User {\n" +
                "\tconstructor() {\n" +
                "\t\tthis.name = \"HarlonWang\";\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "var user = new User();\n" +
                "user.name;");
        assertEquals("HarlonWang", ret);
        context.destroyContext();
    }

    @Test
    public void testGetOwnPropertyNames() {
        QuickJSContext context = QuickJSContext.create();
        context.evaluate("var a = {age: 1, ff: () => {}};");
        JSArray array = context.getGlobalObject().getJSObject("a").getNames();
        for (int i = 0; i < ((JSArray) array).length(); i++) {
            String item = (String) ((JSArray) array).get(i);
            if (i == 0) {
                assertEquals("age", item);
            } else {
                assertEquals("ff", item);
            }
        }

        context.destroyContext();
    }

    @Test
    public void testDumpStackError() {
        QuickJSContext context = QuickJSContext.create();
        try {
            context.evaluate("var a = 1; a();");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("not a function"));
        }
        context.destroyContext();
    }

    @Test
    public void testPromise2() {
        QuickJSContext context = QuickJSContext.create();
        context.getGlobalObject().setProperty("assert", args -> {
            assertEquals("哈哈", args[0]);
            return null;
        });
        context.evaluate("    var defer =\n" +
                "        'function' == typeof Promise\n" +
                "            ? Promise.resolve().then.bind(Promise.resolve())\n" +
                "            : setTimeout;\n" +
                "    defer(() => { assert('哈哈'); });");
        context.destroyContext();
    }

    @Test
    public void testProxy() {
        QuickJSContext context = QuickJSContext.create();
        context.getGlobalObject().setProperty("assert0", args -> {
            assertEquals(1, args[0]);
            assertNull(args[1]);
            return null;
        });
        context.getGlobalObject().setProperty("assert1", args -> {
            assertEquals(false, args[0]);
            assertEquals(37, args[1]);
            return null;
        });
        context.evaluate("const handler = {\n" +
                "    get: function(obj, prop) {\n" +
                "        return prop in obj ? obj[prop] : 37;\n" +
                "    }\n" +
                "};\n" +
                "\n" +
                "const p = new Proxy({}, handler);\n" +
                "p.a = 1;\n" +
                "p.b = undefined;\n" +
                "\n" +
                "assert0(p.a, p.b);      // 1, undefined\n" +
                "assert1('c' in p, p.c); // false, 37");

        context.destroyContext();
    }

    @Test(expected = QuickJSException.class)
    public void testQuickJSException() {
        QuickJSContext context = QuickJSContext.create();
        context.evaluate("a;");
        context.destroyContext();
    }

    @Test
    public void testReturnParseJSON() {
        QuickJSContext context = QuickJSContext.create();
        context.getGlobalObject().setProperty("test", (JSCallFunction) args -> context.parseJSON("{}"));
        context.evaluate("test();test();test();");
        context.destroyContext();
    }

    @Test
    public void testCreateNewJSObject() {
        QuickJSContext context = QuickJSContext.create();
        JSObject jsObject = context.createNewJSObject();
        jsObject.setProperty("name", context.createNewJSObject());
        JSFunction function = (JSFunction) context.evaluate("var test = (arg) => { return arg; };test;");
        Object result = function.call(jsObject);
        assertEquals("{ name: {  } }", result.toString());
        context.destroyContext();
    }

    @Test
    public void testCreateNewJSArray() {
        QuickJSContext context = QuickJSContext.create();
        JSArray jsArray = context.createNewJSArray();
        jsArray.set(11, 0);
        jsArray.set("222", 1);
        JSFunction function = (JSFunction) context.evaluate("var test = (arg) => { return arg; };test;");
        Object result = function.call(jsArray);
        assertEquals("[ 11, 222 ]", result.toString());
        context.destroyContext();
    }

    @Test
    public void testFormatToString() {
        QuickJSContext context = QuickJSContext.create();
        String result = (String) context.evaluate("__format_string(this);");
        assertEquals(result, "{ __format_string: function __format_string() }");
        assertEquals(context.getGlobalObject().toString(), "{ __format_string: function __format_string() }");
        context.destroyContext();
    }

    @Test
    public void testNotAFunction() {
        QuickJSContext context = QuickJSContext.create();
        try {
            context.evaluate("var a = 1; a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'a' is not a function"));
        }

        context.destroyContext();
    }

    @Test
    public void testNotAFunction2() {
        QuickJSContext context = QuickJSContext.create();
        try {
            context.evaluate("function a() {\n" +
                    "\tvar b = {};\n" +
                    "\tb();\n" +
                    "}\n" +
                    "a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'b' is not a function"));
        }


        try {
            context.evaluate("function a() {\n" +
                    "\tvar b = {}; var d = 1;\n" +
                    "\td();\n" +
                    "}\n" +
                    "a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'d' is not a function"));
        }

        try {
            context.evaluate("function a() {\n" +
                    "\tvar b = {}; var d = 1; var c = 1;\n" +
                    "\tc();\n" +
                    "}\n" +
                    "a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'c' is not a function"));
        }

        try {
            context.evaluate("function a() {\n" +
                    "\tvar b = {}; var d = 1; var c = 1;var d = [];\n" +
                    "\td();\n" +
                    "}\n" +
                    "a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'d' is not a function"));
        }

        try {
            context.evaluate("function a() {\n" +
                    "\tvar b = {}; var d = 1; var c = 1;var d = []; var e = {};\n" +
                    "\te();\n" +
                    "}\n" +
                    "a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'e' is not a function"));
        }

        try {
            context.evaluate("function a(aa) {\n" +
                    "\taa();\n" +
                    "}\n" +
                    "a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'aa' is not a function"));
        }

        try {
            context.evaluate("function a(aa, bb) {\n" +
                    "\tbb();\n" +
                    "}\n" +
                    "a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'bb' is not a function"));
        }

        try {
            context.evaluate("function a(aa, bb, cc) {\n" +
                    "\tcc();\n" +
                    "}\n" +
                    "a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'cc' is not a function"));
        }

        try {
            context.evaluate("function a(aa, bb, cc, dd) {\n" +
                    "\tdd();\n" +
                    "}\n" +
                    "a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'dd' is not a function"));
        }

        try {
            context.evaluate("function a(aa, bb, cc, dd, ee) {\n" +
                    "\tee();\n" +
                    "}\n" +
                    "a();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'ee' is not a function"));
        }

        try {
            context.evaluate("function test (){var a = {}; function test1 () {a(); } test1();} test();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'a' is not a function"));
        }

        try {
            context.evaluate("function test (){var a = {}; var b = 1;var c = 1; var d = 1; var e = 1; function test1 () {b = a; c = a; d = e; c = b;e(); } test1();} test();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'e' is not a function"));
        }

        try {
            context.evaluate("var C={index:function(e){\n" +
                    "\tfunction t(){\n" +
                    "\t \tvar e = {router: {}};\n" +
                    "\t\te.router.navigsateTo(\"1\")\n" +
                    "\t}\n" +
                    "\tfor(var n=arguments.length,r=new Array(n>1?n-1:0),o=1;o<n;o++)\n" +
                    "\tr[o-1]=arguments[o]\n" +
                    "\treturn t.apply(null,r);\n" +
                    "}};\n" +
                    "\n" +
                    "C.index();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'navigsateTo' is not a function"));
        }

        context.destroyContext();
    }

    @Test
    public void testNotAFunctionInPromise() {
        QuickJSContext context = QuickJSContext.create();
        try {
            context.evaluate("new Promise({name: 'a'});");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("'[object Object]' is not a function"));
        }
        context.destroyContext();
    }

    @Test
    public void testStackOverflowWithStackSize() {
        QuickJSContext context = QuickJSContext.create(1024);
        try {
            context.evaluate("function y(){}");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("stack overflow"));
        }
        context.destroyContext();
    }

    @Test
    public void testMissingFormalParameter() {
        QuickJSContext context = QuickJSContext.create();
        try {
            context.evaluate("function y(1){}");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("missing formal parameter"));
        }
        context.destroyContext();
    }

    @Test
    public void testStackSizeWithLimited() {
        QuickJSContext context = QuickJSContext.create(1024 * 512);
        try {
            context.evaluate("function y(){y();} y();");
        } catch (QuickJSException e) {
            assertTrue(e.toString().contains("stack overflow"));
        }
        context.destroyContext();
    }

    public static class TestJava {
        @JSMethod
        public boolean test1(String message) {
            return TextUtils.equals("arg1", message);
        }

        @JSMethod
        public boolean test2(String message, int age) {
            return TextUtils.equals("arg1", message) && 18 == age;
        }

        @JSMethod
        public boolean testArray(JSArray array) {
            String arg1 = (String) array.get(0);
            int age = (int) array.get(1);
            return TextUtils.equals("arg1", arg1) && 18 == age;
        }

        @JSMethod
        public boolean testObject(JSObject jsObj) {
            return TextUtils.equals("{\"arg1\":\"a\",\"arg2\":18}", jsObj.stringify());
        }

        @JSMethod
        public boolean testAll(JSObject object, JSArray message, int age, String name) {
            return TextUtils.equals("{\"arg1\":\"a\",\"arg2\":18},[1,2,3],18,name", object.stringify() + "," + message.stringify() + "," + age + "," + name);
        }
    }

    @Test
    public void testAnnotationMethod() {
        QuickJSContext context = QuickJSContext.create();

        context.getGlobalObject().setProperty("test1", TestJava.class);
        assertTrue((boolean) context.evaluate("test1.test1('arg1');"));
        assertTrue((boolean) context.evaluate("test1.test2('arg1', 18);"));
        assertTrue((boolean) context.evaluate("test1.testArray(['arg1', 18]);"));
        assertTrue((boolean) context.evaluate("test1.testObject({'arg1':'a', 'arg2':18});"));
        assertTrue((boolean) context.evaluate("test1.testAll({'arg1':'a', 'arg2':18}, [1,2,3],18, 'name');"));

        context.destroyContext();
    }

    @Test(expected = QuickJSException.class)
    public void testOnError() {
        QuickJSContext context = QuickJSContext.create();
        context.getGlobalObject().setProperty("assertTrue", args -> {
            assertTrue((Boolean) args[0]);
            assertEquals("'a' is not defined", args[1]);
            return null;
        });
        context.evaluate("onError = (e) => { assertTrue(e instanceof Error, e.message); }; a();");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections() {
        thrown.expect(QuickJSException.class);
        thrown.expectMessage("'aaa' is not defined");

        QuickJSContext context = QuickJSContext.create();
        context.evaluate("new Promise(() => { aaa; });");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections2() {
        QuickJSContext context = QuickJSContext.create();
        context.getGlobalObject().setProperty("assert", args -> {
            String error = (String) args[0];
            assertEquals(error, "'aaa' is not defined");
            return null;
        });
        context.evaluate("new Promise(() => { aaa; }).catch((res) => { assert(res.message); });");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections3() {
        thrown.expect(QuickJSException.class);
        thrown.expectMessage("1");

        QuickJSContext context = QuickJSContext.create();
        context.evaluate("new Promise((resolve, reject) => { reject(1); });");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections4() {
        thrown.expect(QuickJSException.class);
        thrown.expectMessage("1");

        QuickJSContext context = QuickJSContext.create();
        context.evaluate("new Promise((resolve, reject) => { reject(1); }); new Promise(() => { aaa; });");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections5() {
        QuickJSContext context = QuickJSContext.create();
        context.getGlobalObject().setProperty("assert", args -> {
            assertEquals(1, args[0]);
            return null;
        });
        context.evaluate("new Promise((resolve, reject) => { resolve(1); }).then((res) => { assert(res); });");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections6() {
        thrown.expect(QuickJSException.class);
        thrown.expectMessage("1");

        QuickJSContext context = QuickJSContext.create();
        context.evaluate("new Promise((resolve, reject) => { reject(1); }).then((res) => { res; });");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections7() {
        thrown.expect(QuickJSException.class);
        thrown.expectMessage("'a' is not defined");

        QuickJSContext context = QuickJSContext.create();
        context.evaluate("new Promise((resolve, reject) => { reject(1); }).catch((res) => { a; });");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections8() {
        thrown.expect(QuickJSException.class);
        thrown.expectMessage("'t2' is not defined");


        QuickJSContext context = QuickJSContext.create();
        context.evaluate("(function(){\n" +
                "    return new Promise((resolve, reject) => {\n" +
                "        reject(1);\n" +
                "    });\n" +
                "})().then((res) => {t0(res);}).catch((res) => {\n" +
                "    t1(a);\n" +
                "}).catch((res) => {\n" +
                "    t2(res);\n" +
                "});\n");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections9() {
        QuickJSContext context = QuickJSContext.create();
        context.getGlobalObject().setProperty("assert", args -> {
            assertEquals(1, args[0]);
            return null;
        });
        context.evaluate("(function(){\n" +
                "    return new Promise((resolve, reject) => {\n" +
                "        reject(1);\n" +
                "    });\n" +
                "})().then((res) => {\n" +
                "    t0.log(res);\n" +
                "}, (res) => {\n" +
                "    assert(res);\n" +
                "}).catch((res) => {\n" +
                "    t1(a);\n" +
                "}).catch((res) => {\n" +
                "    t2(res);\n" +
                "});");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections10() {
        thrown.expect(QuickJSException.class);
        thrown.expectMessage("'t4' is not defined");

        QuickJSContext context = QuickJSContext.create();
        context.evaluate("(function(){\n" +
                "    return new Promise((resolve, reject) => {\n" +
                "        reject(1);\n" +
                "    });\n" +
                "})().then((res) => {\n" +
                "    t1.log(res);\n" +
                "}, (res) => {\n" +
                "    t2.log(aa);\n" +
                "}).catch((res) => {\n" +
                "    t3.log(a);\n" +
                "}).catch((res) => {\n" +
                "    t4.log(res);\n" +
                "});\n");
        context.destroyContext();
    }

    @Test
    public void testPromiseUnhandledRejections11() {
        thrown.expect(QuickJSException.class);
        thrown.expectMessage("bad");

        QuickJSContext context = QuickJSContext.create();
        context.evaluate("new Promise((resolve, reject) => {\n" +
                "  resolve();\n" +
                "}).then(() => {\n" +
                "  throw new Error('bad');\n" +
                "});");
        context.destroyContext();
    }

    // todo fix
//    @Test
//    public void testJSArraySetParseJSON() {
//        QuickJSContext context = QuickJSContext.create();
//        context.getGlobalObject().setProperty("getData", args -> {
//            JSArray jsArray = context.createNewJSArray();
//            JSObject jsObject = context.parseJSON("{\"name\": \"Jack\", \"age\": 33}");
//            jsArray.set(jsObject, 0);
//            // jsArray.set(context.parseJSON("{\"name\": \"Jack\", \"age\": 33}"), 1);
//            return jsArray;
//        });
//        context.evaluate("var array = getData();console.log(JSON.stringify(array));");
//        context.destroyContext();
//    }

}