/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.h2.util.New;
import org.h2.util.SourceCompiler;

/**
 * A code generator for class proxies.
 */
public class ProxyCodeGenerator {

    static SourceCompiler compiler = new SourceCompiler();
    static HashMap<Class<?>, Class<?>> proxyMap = New.hashMap();

    private TreeSet<String> imports = new TreeSet<String>();
    private TreeMap<String, Method> methods = new TreeMap<String, Method>();
    private String packageName;
    private String className;
    private Class<?> extendsClass;

    public static Class<?> getClassProxy(Class<?> c) throws ClassNotFoundException {
        Class<?> p = proxyMap.get(c);
        if (p != null) {
            return p;
        }
        ProxyCodeGenerator cg = new ProxyCodeGenerator();
        cg.setPackageName("bytecode");
        cg.generateClassProxy(c);
        StringWriter sw = new StringWriter();
        cg.write(new PrintWriter(sw));
        String code = sw.toString();
        String proxy = "bytecode."+ c.getSimpleName() + "Proxy";
        compiler.setSource(proxy, code);
        Class<?> px = compiler.getClass(proxy);
        proxyMap.put(c, px);
        return px;
    }

    void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    void generateStaticProxy(Class<?> clazz) {
        imports.clear();
        addImport(InvocationHandler.class);
        addImport(Method.class);
        addImport(clazz);
        className = getClassName(clazz) + "Proxy";
        for (Method m : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers())) {
                if (!Modifier.isPrivate(m.getModifiers())) {
                    addMethod(m);
                }
            }
        }
    }

    void generateClassProxy(Class<?> clazz) {
        imports.clear();
        addImport(InvocationHandler.class);
        addImport(Method.class);
        addImport(clazz);
        className = getClassName(clazz) + "Proxy";
        extendsClass = clazz;
        int finalOrStaticOrPrivate = Modifier.FINAL | Modifier.STATIC | Modifier.PRIVATE;
        while (clazz != null) {
            for (Method m : clazz.getDeclaredMethods()) {
                if ((m.getModifiers() & finalOrStaticOrPrivate) == 0) {
                    addMethod(m);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    void addMethod(Method m) {
        if (methods.containsKey(getMethodName(m))) {
            // already declared in a subclass
            return;
        }
        addImport(m.getReturnType());
        for (Class<?> c : m.getParameterTypes()) {
            addImport(c);
        }
        for (Class<?> c : m.getExceptionTypes()) {
            addImport(c);
        }
        methods.put(getMethodName(m), m);

    }

    private String getMethodName(Method m) {
        StringBuilder buff = new StringBuilder();
        buff.append(m.getReturnType()).append(' ');
        buff.append(m.getName());
        for (Class<?> p : m.getParameterTypes()) {
            buff.append(' ');
            buff.append(p.getName());
        }
        return buff.toString();
    }

    void addImport(Class<?> c) {
        while (c.isArray()) {
            c = c.getComponentType();
        }
        if (!c.isPrimitive()) {
            if (!"java.lang".equals(c.getPackage().getName())) {
                imports.add(c.getName());
            }
        }
    }
    private static String getClassName(Class<?> c) {
        String s = c.getSimpleName();
        while (true) {
            c = c.getEnclosingClass();
            if (c == null) {
                break;
            }
            s = c.getSimpleName() + "." + s;
        }
        return s;
    }
    void write(PrintWriter writer) {
        if (packageName != null) {
            writer.println("package " + packageName + ";");
        }
        for (String imp : imports) {
            writer.println("import " + imp + ";");
        }
        writer.print("public class " + className);
        if (extendsClass != null) {
            writer.print(" extends " + getClassName(extendsClass));
        }
        writer.println(" {");
        writer.println("    private final InvocationHandler ih;");
        writer.println("    public " + className + "() {");
        writer.println("        this(new InvocationHandler() {");
        writer.println("            public Object invoke(Object proxy,");
        writer.println("                    Method method, Object[] args) throws Throwable {");
        writer.println("                return method.invoke(proxy, args);");
        writer.println("            }});");
        writer.println("    }");
        writer.println("    public " + className + "(InvocationHandler ih) {");
        writer.println("        this.ih = ih;");
        writer.println("    }");
        writer.println("    @SuppressWarnings(\"unchecked\")");
        writer.println("    private static <T extends RuntimeException> T convertException(Throwable e) {");
        writer.println("        return (T) e;");
        writer.println("    }");
        for (Method m : methods.values()) {
            Class<?> retClass = m.getReturnType();
            writer.print("    public " + getClassName(retClass) +
                " " + m.getName() + "(");
            int i = 0;
            for (Class<?> p : m.getParameterTypes()) {
                if (i > 0) {
                    writer.print(", ");
                }
                writer.print(getClassName(p) + " p" + i);
                i++;
            }
            writer.print(")");
            Class<?>[] ec = m.getExceptionTypes();
            writer.print(" throws RuntimeException");
            if (ec.length > 0) {
                for (Class<?> e : ec) {
                    writer.print(", ");
                    writer.print(getClassName(e));
                }
            }
            writer.println(" {");
            writer.println("        try {");
            writer.print("            ");
            if (retClass != void.class) {
                writer.print("return (");
                if (retClass == boolean.class) {
                    writer.print("Boolean");
                } else if (retClass == byte.class) {
                    writer.print("Byte");
                } else if (retClass == char.class) {
                    writer.print("Char");
                } else if (retClass == short.class) {
                    writer.print("Short");
                } else if (retClass == int.class) {
                    writer.print("Integer");
                } else if (retClass == long.class) {
                    writer.print("Long");
                } else if (retClass == float.class) {
                    writer.print("Float");
                } else if (retClass == double.class) {
                    writer.print("Double");
                } else {
                    writer.print(getClassName(retClass));
                }
                writer.print(") ");
            }
            writer.print("ih.invoke(this, ");
            writer.println(getClassName(m.getDeclaringClass()) +
                    ".class.getMethod(\"" + m.getName() +
                    "\",");
            writer.print("                new Class[] {");
            i = 0;
            for (Class<?> p : m.getParameterTypes()) {
                if (i > 0) {
                    writer.print(", ");
                }
                writer.print(getClassName(p) + ".class");
                i++;
            }
            writer.println("}),");
            writer.print("                new Object[] {");
            for (i = 0; i < m.getParameterTypes().length; i++) {
                if (i > 0) {
                    writer.print(", ");
                }
                writer.print("p" + i);
            }
            writer.println("});");
            writer.println("        } catch (Throwable e) {");
            writer.println("            throw convertException(e);");
            writer.println("        }");
            writer.println("    }");
        }
        writer.println("}");
        writer.flush();
    }

}
