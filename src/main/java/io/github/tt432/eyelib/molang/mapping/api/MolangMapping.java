package io.github.tt432.eyelib.molang.mapping.api;

/**
 * usage:
 *
 * <pre>{@code
 * @MolangMapping("math")
 * class MolangMath {
 *     public static float max(float a, float b) {
 *         return a > b ? a : b;
 *     }
 * }
 * }</pre>
 * <p>
 * 此处的 max 可以对应 math.max(a, b) <br/>
 * targetClass's method & field must is public & static
 *
 * @author TT432
 */
public @interface MolangMapping {
    /**
     * @return molang function or field name
     */
    String value();

    /**
     * @return will add MolangScope arg first if false
     */
    boolean pureFunction() default true;
}
