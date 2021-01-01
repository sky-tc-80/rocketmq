package com.sky.dev.proxy;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Proxy {
    public static Object newProxyInstance(Class c, InvocationHandler h) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String methodStr = "";
        String rt = "\r\n";
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            methodStr += "@Override" + rt +
                    " public void " + method.getName() + "(){" + rt +
                    " try {" + rt +
                    " Method md = " + c.getName() + ".class.getMethod(\"" + method.getName() + "\");" + rt +
                    " h.invoke(this,md);" + rt +
                    " } catch(Exception e) { e.printStackTrace();}" + rt
                    + "}";

        }
        String src = "package com.sky.dev.proxy;" + rt
                + "import java.lang.reflect.Method;" + rt
                + "public class $Proxy1 implements " + c.getName() + "{" + rt
                + "    public $Proxy1(InvocationHandler h){" + rt
                + "       this.h = h;" + rt
                + "    }" + rt
                + "     com.sky.dev.proxy.InvocationHandler h;" + rt
                + methodStr + "}";

        String fileName = "/Users/apple/self/code/rocketmq/example/src/main/java/com/sky/dev/proxy/$Proxy1.java";
        File file = new File(fileName);
        FileWriter fw = new FileWriter(file);
        fw.write(src);
        fw.flush();
        fw.close();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjects(fileName);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, units);
        task.call();
        fileManager.close();

        URL[] urls = {new URL("file:/Users/apple/self/code/rocketmq/example/src/main/java/")};
        URLClassLoader ul = new URLClassLoader(urls);
        Class cl = ul.loadClass("com.sky.dev.proxy.$Proxy1");
        System.out.println(cl);
        Constructor ctr = cl.getConstructor(InvocationHandler.class);
        Object m = ctr.newInstance(h);
        return m;
    }
}
