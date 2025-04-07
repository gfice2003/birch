package tech.grove.birch.reflection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import org.apache.commons.lang3.ClassUtils;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Reflector {

    private static final Set<Class<?>> primitiveWrappers = new HashSet<>();
    private static final Set<Class<?>> primitives        = new HashSet<>();

    static {
        primitiveWrappers.add(String.class);
        primitiveWrappers.add(Boolean.class);
        primitiveWrappers.add(Character.class);
        primitiveWrappers.add(Byte.class);
        primitiveWrappers.add(Short.class);
        primitiveWrappers.add(Integer.class);
        primitiveWrappers.add(Long.class);
        primitiveWrappers.add(Float.class);
        primitiveWrappers.add(Double.class);
        primitiveWrappers.add(Void.class);
        primitiveWrappers.add(Duration.class);
        primitiveWrappers.add(LocalDateTime.class);
        primitiveWrappers.add(LocalDate.class);
        primitiveWrappers.add(Instant.class);
        primitiveWrappers.add(BigDecimal.class);
        primitiveWrappers.add(Period.class);
        primitiveWrappers.add(UUID.class);

        primitives.add(int.class);
        primitives.add(short.class);
        primitives.add(long.class);
        primitives.add(float.class);
        primitives.add(double.class);
        primitives.add(char.class);
        primitives.add(boolean.class);
    }

    private static final TypeAccessor   TYPE   = new TypeAccessor();
    private static final MethodAccessor METHOD = new MethodAccessor();
    private static final FieldAccessor  FIELD  = new FieldAccessor();

    private final static class Token {
        public final static String GETTER_PREFIX        = "get";
        public final static String GENERIC_TYPE_PATTERN = ".*<(.*)>";
    }

    private final static class Constant {
        public static final Pattern GENERIC_TYPE_PATTERN  = Pattern.compile(Token.GENERIC_TYPE_PATTERN);
        public static final int     GENERIC_TYPE_GROUP_ID = 1;
    }

    public static <T> Class<T> getType(T instance) {
        return cast(instance.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object instance) {
        return (T) instance;
    }

    public static TypeAccessor type(String typeName) {
        try {
            return type(Class.forName(typeName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static TypeAccessor type(Class<?> type) {
        return TYPE.initialize(type);
    }

    public static MethodAccessor method(Method method) {
        return METHOD.initialize(method);
    }

    public static FieldAccessor field(Field field) {
        return FIELD.initialize(field);
    }

    public static final class TypeAccessor extends Accessor<Class<?>, TypeAccessor> {

        public Class<?> get() {
            try (var type = accessValue()) {
                return type.get();
            }
        }

        public boolean isEffectivelyPrimitive() {
            try (var type = accessValue()) {
                return type.get().isPrimitive() || type.get().isEnum() || primitiveWrappers.contains(type.get());
            }
        }

        public boolean isPrimitive() {
            try (var type = accessValue()) {
                return type.get().isPrimitive();
            }
        }

        public boolean isPrimitiveWrapper() {
            try (var type = accessValue()) {
                return primitiveWrappers.contains(type.get());
            }
        }

        public boolean isEnum() {
            try (var type = accessValue()) {
                return type.get().isEnum();
            }
        }

        public boolean isMap() {
            try (var type = accessValue()) {
                return Map.class.isAssignableFrom(type.get());
            }
        }

        public boolean isIterable() {
            try (var type = accessValue()) {
                return Iterable.class.isAssignableFrom(type.get());
            }
        }

        public boolean isArray() {
            try (var type = accessValue()) {
                return type.get().isArray();
            }
        }

        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
            try (var type = accessValue()) {
                return type.get().getAnnotation(annotationClass);
            }
        }

        public <A extends Annotation> boolean hasAnnotation(Class<A> annotationClass) {
            return (getAnnotation(annotationClass) != null);
        }

        //-- Works only with reference type argument. Primitives are boxed and get another class
        public <T> T newInstance(Object... args) {
            try (var type = accessValue()) {
                Class<?>[] argTypes = null;

                if (args != null && args.length > 0) {
                    argTypes = Arrays.stream(args)
                            .map(Object::getClass)
                            .toArray(x -> new Class<?>[x]);
                }

                return cast(type.get().getDeclaredConstructor(argTypes).newInstance(args));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(String.format("Failed to create new instance (Error: %s)", e.getMessage()), e);
            }
        }

        public Class<?> extractSuperclassGenericParameter() {
            try (var type = accessValue()) {
                var matcher = Constant.GENERIC_TYPE_PATTERN.matcher(type.get().getGenericSuperclass().getTypeName());

                if (matcher.find()) {
                    try {
                        return cast(Class.forName(matcher.group(Constant.GENERIC_TYPE_GROUP_ID)));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(String.format("Unknown class: %s", matcher.group(Constant.GENERIC_TYPE_GROUP_ID)));
                    }
                } else {
                    throw new RuntimeException(String.format("Unsupported super class: %s", get()));
                }
            }
        }
    }

    public static final class MethodAccessor extends Accessor<Method, MethodAccessor> {

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            try (var method = accessValue()) {
                return getAnnotation(method.get(), annotationClass);
            }
        }

        public <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass) {
            return (getAnnotation(annotationClass) != null);
        }

        private <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
            var declaringClass  = method.getDeclaringClass();
            var allSuperclasses = ClassUtils.getAllSuperclasses(declaringClass);
            allSuperclasses.add(declaringClass);
            var classesInterfacesStream = allSuperclasses.stream()
                    .flatMap(e -> ClassUtils.getAllInterfaces(e).stream());
            var allInterfaces = ClassUtils.getAllInterfaces(declaringClass);

            var superclassesAndTheirInterfaces = Stream
                    .concat(allSuperclasses.stream(), classesInterfacesStream);

            return Stream.concat(superclassesAndTheirInterfaces, allInterfaces.stream())
                    .map(Class::getDeclaredMethods)
                    .flatMap(Arrays::stream)
                    .filter(e -> e.getName().equals(method.getName()) &&
                                 Arrays.equals(e.getParameterTypes(), method.getParameterTypes()))
                    .map(e -> e.getDeclaredAnnotation(annotationClass))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        public String jsonName() {
            try (var method = accessValue()) {
                var getter     = method.get();
                var annotation = getAnnotation(getter, JsonProperty.class);

                if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
                    if (getter.getName().startsWith(Token.GETTER_PREFIX)) {
                        return getter.getName().substring(Token.GETTER_PREFIX.length());
                    } else {
                        return getter.getName();
                    }
                } else {
                    return annotation.value();
                }
            }
        }
    }

    public static final class FieldAccessor extends Accessor<Field, FieldAccessor> {

        public Field get() {
            try (var field = accessValue()) {
                return field.get();
            }
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            try (var field = accessValue()) {
                return field.get().getAnnotation(annotationClass);
            }
        }

        public <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass) {
            return (getAnnotation(annotationClass) != null);
        }
    }

    protected abstract static class Accessor<V, A extends Accessor<V, A>> {

        private final ThreadLocal<V> value    = new ThreadLocal<>();
        private final ValueAccessor  accessor = new ValueAccessor();

        protected A initialize(V value) {
            if (value != null) {
                this.value.set(value);
            }

            return cast(this);
        }

        protected ValueAccessor accessValue() {
            return accessor;
        }

        protected final class ValueAccessor implements Closeable {

            public V get() {
                return value.get();
            }

            @Override
            public void close() {
                value.remove();
            }
        }
    }
}