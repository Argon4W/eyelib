package io.github.tt432.eyelib.molang.function;

import io.github.tt432.eyelib.molang.MolangScope;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author TT432
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MolangFunctionParameters {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class S {
        private static final MolangFunctionParameters INSTANCE = new MolangFunctionParameters();
    }

    public static MolangFunctionParameters upload(MolangScope scope, List<Object> parameters) {
        S.INSTANCE.parameters = parameters;
        S.INSTANCE.scope = scope;
        return S.INSTANCE;
    }

    MolangScope scope;
    List<Object> parameters;

    public int size() {
        return parameters.size();
    }

    public float value(int index) {
        return (float) parameters.get(index);
    }

    public String svalue(int index) {
        return parameters.get(index).toString();
    }

    public Stream<String> svalues() {
        return parameters.stream().map(Object::toString);
    }

    public MolangScope scope() {
        return scope;
    }
}
