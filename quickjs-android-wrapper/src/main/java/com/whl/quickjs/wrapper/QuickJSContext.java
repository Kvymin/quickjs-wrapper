package com.whl.quickjs.wrapper;

public class QuickJSContext {

    static {
        System.loadLibrary("quickjs-android-wrapper");
    }

    private static final String UNDEFINED = "undefined.js";


    public static QuickJSContext create() {
        return new QuickJSContext();
    }

    private final long context;
    private final NativeCleaner<JSObject> nativeCleaner = new NativeCleaner<JSObject>() {
        @Override
        public void onRemove(long pointer) {
            freeDupValue(context, pointer);
        }
    };

    private QuickJSContext() {
        context = createContext();
    }

    public Object evaluate(String script) {
        return evaluate(script, UNDEFINED);
    }

    public Object evaluate(String script, String fileName) {
        return evaluate(context, script, fileName);
    }

    public JSObject getGlobalObject() {
        return getGlobalObject(context);
    }

    public void destroyContext() {
        nativeCleaner.forceClean();
        destroyContext(context);
    }

    public String stringify(JSObject jsObj) {
        return stringify(context, jsObj.getPointer());
    }

    public Object getProperty(JSObject jsObj, String name) {
        return getProperty(context, jsObj.getPointer(), name);
    }

    public void setProperty(JSObject jsObj, String name, Object value) {
        setProperty(context, jsObj.getPointer(), name, value);
    }

    public void freeValue(JSObject jsObj) {
        freeValue(context, jsObj.getPointer());
    }

    public void dupValue(JSObject jsObj) {
        dupValue(context, jsObj.getPointer());
    }

    public void freeDupValue(JSObject jsObj) {
        freeDupValue(context, jsObj.getPointer());
    }

    public int length(JSArray jsArray) {
        return length(context, jsArray.getPointer());
    }

    public Object get(JSArray jsArray, int index) {
        return get(context, jsArray.getPointer(), index);
    }

    public Object call(JSObject func, JSObject thisObj, Object... args) {
        return call(context, func.getPointer(), thisObj.getPointer(), args);
    }

    public void hold(JSObject jsObj) {
        jsObj.dupValue();
        nativeCleaner.register(jsObj, jsObj.getPointer());
    }

    public JSObject parseJSON(String json) {
        return parseJSON(context, json);
    }

    public byte[] compile(String sourceCode) {
        return compile(context, sourceCode);
    }

    public Object execute(byte[] code) {
        return execute(context, code);
    }

    private native long createContext();
    private native void destroyContext(long context);
    private native Object evaluate(long context, String script, String fileName);
    private native JSObject getGlobalObject(long context);
    private native Object call(long context, long func, long thisObj, Object[] args);

    private native Object getProperty(long context, long objValue, String name);
    private native void setProperty(long context, long objValue, String name, Object value);
    private native String stringify(long context, long objValue);
    private native int length(long context, long objValue);
    private native Object get(long context, long objValue, int index);
    private native void freeValue(long context, long objValue);
    private native void dupValue(long context, long objValue);
    private native void freeDupValue(long context, long objValue);
    private native JSObject parseJSON(long context, String json);
    private native byte[] compile(long context, String sourceCode);
    private native Object execute(long context, byte[] bytecode);
}
