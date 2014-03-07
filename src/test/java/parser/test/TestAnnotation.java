package parser.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 * Just for testing purposes
 *
 */
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD})
public @interface TestAnnotation {
}
