package io.github.tt432.eyelib.molang.function.functions.limit;


import io.github.tt432.eyelib.molang.function.MolangFunction;
import io.github.tt432.eyelib.molang.function.MolangFunctionHolder;
import io.github.tt432.eyelib.molang.function.MolangFunctionParameters;

@MolangFunctionHolder("math.max")
public class Max extends MolangFunction {
    @Override
    public float invoke(MolangFunctionParameters params) {
        return Math.max(params.value(0), params.value(1));
    }
}