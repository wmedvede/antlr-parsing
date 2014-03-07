package util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: wmedvede
 * Date: 3/6/14
 * Time: 10:15 AM
 * To change this template use File | Settings | File Templates.
 */

@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD})
public @interface TestAnnotation {
}
