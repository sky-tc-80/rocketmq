package com.sky.dev.stream;

import com.sky.dev.function.EvalFunction;

/**
 * 下一个元素求值过程
 */
public class NextItemEvalProcess {

    /**
     * 求值方法
     * */
    private EvalFunction evalFunction;

    public NextItemEvalProcess(EvalFunction evalFunction) {
        this.evalFunction = evalFunction;
    }

    MyStream eval(){
        return evalFunction.apply();
    }
}
