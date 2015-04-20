/*
 * Copyright (C) 2013 Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.shop.util.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.ejb.MessageDriven;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import static java.util.logging.Level.FINER;


/**
 * Interceptor zur Protokollierung von public-Methoden der CDI-faehigen Beans und ggf. EJBs.
 * Sowohl der Methodenaufruf als auch der Rueckgabewert werden mit Level FINER protokolliert.
 * Exceptions werden *NICHT* protokolliert, um den Stacktrace zu erhalten.
 * Der Interceptor ist serialisierbar, damit er auch auf "passivating Scopes",
 * wie z.B. @ViewScoped, @ConversationScoped, @SessionScoped und @ApplicationScoped anwendbar ist.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Interceptor
@Log
public class LogInterceptor implements Serializable {
    private static final long serialVersionUID = 6225006198548883927L;
    
    private static final String COUNT = "=Anzahl:";
    // bei Collections wird ab 5 Elementen nur die Anzahl ausgegeben
    private static final int MAX_ELEM = 4;
    
    private static final int CHAR_POS_AFTER_SET = 3; // getX...
    private static final int CHAR_POS_AFTER_IS = 2; // isX...
    private static final int CHAR_POS_AFTER_GET = 3; // setX...
    private static final int STRING_BUILDER_INITIAL_SIZE = 64;
    private static final String PARAM_SEPARATOR = ", ";
    private static final int PARAM_SEPARATOR_LENGTH = PARAM_SEPARATOR.length();
    
    private static final Map<Class<?>, Logger> LOGGER_MAP = new ConcurrentHashMap<>();
    
    @AroundConstruct
    public void logConstructor(InvocationContext ctx) throws Exception {   //NOSONAR
        final Class<?> clazz = ctx.getConstructor().getDeclaringClass();
        Logger logger = LOGGER_MAP.get(clazz);                         //NOSONAR
        if (logger == null) {
            logger = Logger.getLogger(clazz.getName());
            LOGGER_MAP.putIfAbsent(clazz, logger);
        }

        if (logger.isLoggable(FINER)) {
            if (clazz.getAnnotation(Stateless.class) != null) {
                logger.finer("Stateless EJB wurde erzeugt");
            } else if (clazz.getAnnotation(Stateful.class) != null) {
                logger.finer("Stateful EJB wurde erzeugt");
            } else if (clazz.getAnnotation(MessageDriven.class) != null) {
                logger.finer("MessageDriven EJB wurde erzeugt");
            } else {
                logger.finer("CDI-faehiges Bean wurde erzeugt");
            }
        }
        
        ctx.proceed();
    }
    
    @AroundInvoke
    public Object log(InvocationContext ctx) throws Exception {        //NOSONAR
        final Class<?> clazz = ctx.getTarget().getClass();
        Logger logger = LOGGER_MAP.get(clazz);                         //NOSONAR
        if (logger == null) {
            logger = Logger.getLogger(clazz.getName());
            LOGGER_MAP.putIfAbsent(clazz, logger);
        }

        if (!logger.isLoggable(FINER)) {
            return ctx.proceed();
        }

        final Method method = ctx.getMethod();
        final String methodName = method.getName();

        // KEINE Protokollierung von get-, set-, is-Methoden sowie toString(), equals() und hashCode()
        if (isGetterOrSetter(methodName) || isBasic(methodName)) {
            return ctx.proceed();
        }
        
        final Object[] params = ctx.getParameters();
        final Parameter[] paramReps = method.getParameters();

        // Methodenaufruf protokollieren
        logMethodBegin(logger, methodName, params, paramReps);
        
        // Eigentlicher Methodenaufruf
        final Object result = ctx.proceed();

        // Ende der eigentlichen Methode protokollieren
        logMethodEnd(logger, methodName, result);
        
        return result;
    }
    
    private boolean isGetterOrSetter(String methodName) {
        if (methodName.startsWith("get") && Character.isUpperCase(methodName.charAt(CHAR_POS_AFTER_GET))) {
            return true;
        }
        if (methodName.startsWith("set") && Character.isUpperCase(methodName.charAt(CHAR_POS_AFTER_SET))) {
            return true;
        }
        
        return methodName.startsWith("is") && Character.isUpperCase(methodName.charAt(CHAR_POS_AFTER_IS));
    }
    
    private boolean isBasic(String methodName) {
        switch (methodName) {
            case "toString":
            case "equals":
            case "hashCode":
                return true;

            default:
                return false;
        }
    }
    
    private void logMethodBegin(Logger logger, String methodName, Object[] params, Parameter[] paramReps) {   //NOSONAR
        final StringBuilder sb = new StringBuilder(STRING_BUILDER_INITIAL_SIZE);
        if (params != null) {
            final int anzahlParams = params.length;
            sb.append(": ");
            IntStream.range(0, anzahlParams)
                     .forEach( i -> {
                if (params[i] == null) {
                    sb.append(paramReps[i].getName());
                    sb.append("=null");
                } else {
                    final String paramStr = toString(params[i], paramReps[i].getName());
                    sb.append(paramStr);
                }
                sb.append(PARAM_SEPARATOR);
            });
            final int laenge = sb.length();
            sb.delete(laenge - PARAM_SEPARATOR_LENGTH, laenge - 1);
        }
        logger.finer(methodName + " BEGINN" + sb);
    }
    
    private void logMethodEnd(Logger logger, String methodName, Object result) {   //NOSONAR
        if (result == null) {
            // Methode vom Typ void oder Rueckgabewert null
            logger.finer(methodName + " ENDE");
        } else {
            logger.finer(methodName + " ENDE: " + toString(result, "result"));
        }
    }

    /**
     * Collection oder Array oder Objekt in einen String konvertieren
     * @param obj Eine Collection, ein Array oder ein sonstiges Objekt
     * @param paramName Parametername innerhalb einer Methodensignatur
     * @return obj als String
     */
    private String toString(Object obj, String paramName) {
        if (obj instanceof Collection<?>) {
            // Collection: Elemente bei kleiner Anzahl ausgeben; sonst nur die Anzahl
            final Collection<?> coll = (Collection<?>) obj;
            final int anzahl = coll.size();
            if (anzahl > MAX_ELEM) {
                return paramName + COUNT + coll.size();
            }

            return paramName + "=" + coll.toString();
        }
        
        if (obj.getClass().isArray()) {
            // Array in String konvertieren: Element fuer Element
            return arrayToString(obj, paramName);
        }

        // Objekt, aber keine Collection und kein Array
        return paramName + "=" + obj.toString();
    }
    
    /**
     * Array in einen String konvertieren
     * @param obj ein Array
     * @param paramName Parametername innerhalb einer Methodensignatur
     * @return das Array als String
     */
    private String arrayToString(Object obj, String paramName) {
        final Class<?> componentClass = obj.getClass().getComponentType();

        if (!componentClass.isPrimitive()) {
            return arrayOfObject(obj, paramName);
        }
        
        // Array von primitiven Werten: byte, short, int, long, float, double, boolean, char
        final String className = componentClass.getName();
        return arrayOfPrimitive(obj, paramName, className);
    }
    
    private String arrayOfObject(Object obj, String paramName) {
        final Object[] arr = (Object[]) obj;
        if (arr.length > MAX_ELEM) {
            return paramName + COUNT + arr.length;
        }
        return paramName + "=" + Arrays.toString(arr);        
    }
    
    private String arrayOfPrimitive(Object obj, String paramName, String className) {
        switch (className) {
            case "byte":
                return arrayOfByte(obj, paramName);
            case "short":
                return arrayOfShort(obj, paramName);
            case "int":
                return arrayOfInt(obj, paramName);
            case "long":
                return arrayOfLong(obj, paramName);
            case "float":
                return arrayOfFloat(obj, paramName);
            case "double":
                return arrayOfDouble(obj, paramName);
            case "boolean":
                return arrayOfBoolean(obj, paramName);
            case "char":
                return arrayOfChar(obj, paramName);
            default:
                return paramName + "=<<UNKNOWN PRIMITIVE ARRAY>>";
        }
    }
    
    private String arrayOfByte(Object obj, String paramName) {
        final byte[] arr = (byte[]) obj;
        if (arr.length > MAX_ELEM) {
            return paramName + COUNT + arr.length;
        }
        return paramName + "=" + Arrays.toString(arr);
    }
    
    private String arrayOfShort(Object obj, String paramName) {
        final short[] arr = (short[]) obj;
        if (arr.length > MAX_ELEM) {
            return paramName + COUNT + arr.length;
        }
        return paramName + "=" + Arrays.toString(arr);
    }
    
    private String arrayOfInt(Object obj, String paramName) {
        final int[] arr = (int[]) obj;
        if (arr.length > MAX_ELEM) {
            return paramName + COUNT + arr.length;
        }
        return paramName + "=" + Arrays.toString(arr);
    }
    
    private String arrayOfLong(Object obj, String paramName) {
        final long[] arr = (long[]) obj;
        if (arr.length > MAX_ELEM) {
            return paramName + COUNT + arr.length;
        }
        return paramName + "=" + Arrays.toString(arr);
    }
    
    private String arrayOfFloat(Object obj, String paramName) {
        final float[] arr = (float[]) obj;
        if (arr.length > MAX_ELEM) {
            return paramName + COUNT + arr.length;
        }
        return paramName + "=" + Arrays.toString(arr);
    }
    
    private String arrayOfDouble(Object obj, String paramName) {
        final double[] arr = (double[]) obj;
        if (arr.length > MAX_ELEM) {
            return paramName + COUNT + arr.length;
        }
        return paramName + "=" + Arrays.toString(arr);
    }
    
    private String arrayOfBoolean(Object obj, String paramName) {
        final boolean[] arr = (boolean[]) obj;
        if (arr.length > MAX_ELEM) {
            return paramName + COUNT + arr.length;
        }
        return paramName + "=" + Arrays.toString(arr);
    }
    
    private String arrayOfChar(Object obj, String paramName) {
        final char[] arr = (char[]) obj;
        if (arr.length > MAX_ELEM) {
            return paramName + COUNT + arr.length;
        }
        return paramName + "=" + Arrays.toString(arr);
    }
}
