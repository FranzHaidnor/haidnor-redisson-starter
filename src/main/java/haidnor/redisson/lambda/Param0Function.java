package haidnor.redisson.lambda;

/**
 * 回调函数-无参数，无返回值
 */
@FunctionalInterface
public interface Param0Function {
    /**
     * Applies this function to the given argument.
     */
    void apply();
}
