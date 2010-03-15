/*
 * Copyright 2004-2010 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.java;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * A class or interface.
 */
public class ClassObj {

    /**
     * The fully qualified class name.
     */
    String name;

    /**
     * Whether this is an interface.
     */
    boolean isInterface;

    /**
     * Whether this class is public.
     */
    boolean isPublic;

    /**
     * Whether this is a primitive class (int, char,...)
     */
    boolean isPrimitive;

    /**
     * The imported classes.
     */
    ArrayList<ClassObj> imports = new ArrayList<ClassObj>();

    /**
     * The per-instance fields.
     */
    LinkedHashMap<String, FieldObj> instanceFields = new LinkedHashMap<String, FieldObj>();

    /**
     * The static fields of this class.
     */
    LinkedHashMap<String, FieldObj> staticFields = new LinkedHashMap<String, FieldObj>();

    /**
     * The methods.
     */
    LinkedHashMap<String, MethodObj> methods = new LinkedHashMap<String, MethodObj>();

    /**
     * The list of native statements.
     */
    ArrayList<Statement> nativeCode = new ArrayList<Statement>();

    /**
     * The class number.
     */
    int id;

    /**
     * Add a method.
     *
     * @param method the method
     */
    void addMethod(MethodObj method) {
        if (methods.containsKey(method.name)) {
            throw new RuntimeException("Method overloading is not supported: " + method.name);
        }
        methods.put(method.name, method);
    }

    /**
     * Add an instance field.
     *
     * @param field the field
     */
    void addInstanceField(FieldObj field) {
        instanceFields.put(field.name, field);
    }

    /**
     * Add a static field.
     *
     * @param field the field
     */
    void addStaticField(FieldObj field) {
        staticFields.put(field.name, field);
    }

    public String toString() {
        if (isPrimitive) {
            return "j" + name;
        }
        return name;
    }

}

/**
 * A method.
 */
class MethodObj {

    /**
     * Whether this method is static.
     */
    boolean isStatic;

    /**
     * Whether this method is private.
     */
    boolean isPrivate;

    /**
     * The name.
     */
    String name;

    /**
     * The statement block (if any).
     */
    Statement block;

    /**
     * The return type.
     */
    Type returnType;

    /**
     * The parameter list.
     */
    LinkedHashMap<String, FieldObj> parameters = new LinkedHashMap<String, FieldObj>();

    /**
     * Whether this method is final.
     */
    boolean isFinal;

    /**
     * Whether this method is public.
     */
    boolean isPublic;

    /**
     * Whether this method is native.
     */
    boolean isNative;

    /**
     * Whether this is a constructor.
     */
    boolean isConstructor;
}

/**
 * A field.
 */
class FieldObj {

    /**
     * The type.
     */
    Type type;

    /**
     * Whether this is a local field.
     */
    boolean isLocal;

    /**
     * The field name.
     */
    String name;

    /**
     * Whether this field is static.
     */
    boolean isStatic;

    /**
     * Whether this field is final.
     */
    boolean isFinal;

    /**
     * Whether this field is private.
     */
    boolean isPrivate;

    /**
     * Whether this field is public.
     */
    boolean isPublic;

    /**
     * The initial value expression (may be null).
     */
    Expr value;

    /**
     * The class where this field is declared.
     */
    ClassObj declaredClass;

}

/**
 * A type.
 */
class Type {

    /**
     * The class.
     */
    ClassObj type;

    /**
     * The array nesting level. 0 if not an array.
     */
    int arrayLevel;

    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(JavaParser.toC(type.toString()));
        if (!type.isPrimitive) {
            buff.append("*");
        }
        for (int i = 0; i < arrayLevel; i++) {
            buff.append("*");
        }
        return buff.toString();
    }
}
