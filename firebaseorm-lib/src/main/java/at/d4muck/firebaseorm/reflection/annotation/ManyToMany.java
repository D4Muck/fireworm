package at.d4muck.firebaseorm.reflection.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Christoph Muck
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {
    String mappedBy();

    boolean reverse() default false;

    boolean redundant() default true;
}
