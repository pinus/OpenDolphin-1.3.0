package open.dolphin.helper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.swing.event.DocumentListener;

/**
 * CallbackDocumentListener
 *
 * @author Minagawa,Kazushi
 */
public class ProxyDocumentListener {

    public static DocumentListener create(final Object target, final String methodName) {

        Class cls = DocumentListener.class;
        ClassLoader cl = cls.getClassLoader();

        InvocationHandler handler = new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {

                Object result = null;

                try {

                    Method targetMethod = target.getClass().getMethod(methodName, (Class[]) null);
                    //System.out.println("target=" + targetMethod);

                    result = targetMethod.invoke(target, (Object[]) null);

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

        return (DocumentListener) Proxy.newProxyInstance(cl, new Class[]{cls}, handler);

    }
}
