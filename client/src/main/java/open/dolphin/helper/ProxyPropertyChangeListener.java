package open.dolphin.helper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * CallbackDocumentListener
 *
 * @author Minagawa,Kazushi
 */
public class ProxyPropertyChangeListener {


    public static PropertyChangeListener create(Object target, String methodName) {
        return ProxyPropertyChangeListener.create(target, methodName, new Class[]{Object.class});
    }


    public static PropertyChangeListener create(final Object target, final String methodName, final Class[] paramTypes) {

        Class cls = PropertyChangeListener.class;
        ClassLoader cl = cls.getClassLoader();

        InvocationHandler handler = new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {

                Object result = null;
                PropertyChangeEvent e = (PropertyChangeEvent) args[0];
                Object newValue = e.getNewValue();

                try {

                    Method targetMethod = target.getClass().getMethod(methodName, paramTypes);
                    result = targetMethod.invoke(target, new Object[]{newValue});

                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace(System.err);
                } catch (SecurityException ex) {
                    ex.printStackTrace(System.err);
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace(System.err);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace(System.err);
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace(System.err);
                }

                return result;
            }
        };

        return (PropertyChangeListener) Proxy.newProxyInstance(cl, new Class[]{cls}, handler);

    }
}
