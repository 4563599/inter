package com.example.androiddemo.kotlin.Lambda;

import java.util.function.Consumer;

/**
 * 【小白版】Java Lambda 表达式超详细入门
 * <p>
 * Java 中的 Lambda 表达式，本质上是“一个只包含单个抽象方法的接口”的匿名实现。
 * 听起来很绕口，别担心，我们通过下面的步骤来彻底理解它。
 * <p>
 * 这个接口我们称之为“函数式接口”（Functional Interface）。
 * 我们可以自己定义，也可以用 Java 内置的（比如 Runnable, Consumer<T> 等）。
 */
public class LambdaJavaDemo {

    // 为了演示，我们先自己定义一个最简单的函数式接口。
    // @FunctionalInterface 注解不是必需的，但它是一个好习惯。
    // 它可以让编译器帮你检查这个接口是否真的只有一个抽象方法。
    @FunctionalInterface
    interface MyCalculator {
        // 这个接口只有一个任务：接收两个整数，然后返回一个整数。
        int calculate(int a, int b);
    }

    /**
     * 这是程序的入口，我们将在这里一步步演示 Lambda 的演进过程。
     * 你可以在你的 Activity 或者其他 main 方法中调用 `runAllSteps()` 来查看结果。
     */
    public void runAllSteps() {
        System.out.println("======== 第 1 步：古老的匿名内部类写法 ========");
        step1_AnonymousInnerClass();

        System.out.println("\n======== 第 2 步：Lambda 表达式的诞生 ========");
        step2_LambdaExpression();

        System.out.println("\n======== 第 3 步：省略参数类型 ========");
        step3_TypeInference();

        System.out.println("\n======== 第 4 步：简化函数体 ========");
        step4_SimplifyBody();

        System.out.println("\n======== 第 5 步：使用 Java 内置的函数式接口 ========");
        step5_BuiltInInterfaces();
    }

    /**
     * 第 1 步：在没有 Lambda 之前，我们如何实现 MyCalculator 接口。
     * 我们使用一种叫做“匿名内部类”的方式。这种方式非常繁琐。
     */
    private void step1_AnonymousInnerClass() {
        // 我们想实现 MyCalculator 接口，但又不想专门创建一个新的 .java 文件去实现它。
        // 所以我们直接 `new MyCalculator()` 并且当场实现了它的 `calculate` 方法。
        MyCalculator addition = new MyCalculator() {
            @Override
            public int calculate(int a, int b) {
                return a + b;
            }
        };

        // 调用我们刚刚实现的加法功能
        int result = addition.calculate(10, 5);
        System.out.println("匿名内部类计算结果: " + result);

        // 这种写法的缺点：
        // 1. 语法啰嗦：`new MyCalculator()`, `@Override`, `public int calculate(...)` 这些都是模板代码。
        // 2. 核心代码不突出：我们真正关心的只有 `a + b` 这部分，但它被包裹在大量样板代码里。
    }

    /**
     * 第 2 步：使用 Lambda 表达式重写第 1 步的功能。
     * Lambda 的目标就是消除所有不必要的模板代码！
     */
    private void step2_LambdaExpression() {
        // 语法拆解：
        // 1. `(int a, int b)`: 这是 Lambda 的参数列表。它直接对应接口方法 `calculate(int a, int b)` 的参数。
        // 2. `->`: 这是 Lambda 的核心符号，读作 "goes to"，分隔了参数和函数体。
        // 3. `{ return a + b; }`: 这是 Lambda 的函数体，也就是接口方法的具体实现。
        MyCalculator addition = (int a, int b) -> {
            return a + b;
        };

        int result = addition.calculate(10, 5);
        System.out.println("Lambda 计算结果: " + result);

        // 对比第 1 步，我们发现所有模板代码都不见了！
        // `new MyCalculator()`, `@Override`, `public int calculate` 这些全被省略了。
        // 编译器知道 `addition` 是 `MyCalculator` 类型，所以它能自动推断出这个 Lambda 就是在实现 `calculate` 方法。
    }

    /**
     * 第 3 步：类型推断，让代码更简洁。
     * 编译器足够聪明，可以根据上下文推断出参数的类型。
     */
    private void step3_TypeInference() {
        // 对比第 2 步：
        // 因为编译器已经从变量 `addition` 的类型 `MyCalculator` 中知道了 `calculate` 方法的参数是两个 `int`，
        // 所以在 Lambda 表达式中，我们可以省略参数的类型 `int`。
        MyCalculator addition = (a, b) -> {
            return a + b;
        };
        int result = addition.calculate(10, 5);
        System.out.println("省略参数类型的 Lambda: " + result);
    }

    /**
     * 第 4 步：当函数体只有一行代码时，可以进一步简化。
     */
    private void step4_SimplifyBody() {
        // 对比第 3 步：
        // 如果 Lambda 的函数体只有一行表达式，你可以：
        // 1. 去掉花括号 `{}`
        // 2. 去掉 `return` 关键字和末尾的分号 `;`
        // 编译器会自动把这一行表达式的结果作为返回值。
        MyCalculator addition = (a, b) -> a + b;
        MyCalculator subtraction = (a, b) -> a - b;

        System.out.println("最简化的 Lambda (加法): " + addition.calculate(10, 5));
        System.out.println("最简化的 Lambda (减法): " + subtraction.calculate(10, 5));
    }


    /**
     * 第 5 步：使用 Java 已经为我们准备好的函数式接口。
     * 我们不必总是自己定义接口，`java.util.function` 包里有很多现成的。
     */
    private void step5_BuiltInInterfaces() {
        // 比如 `Consumer<T>` 接口，它的方法是 `void accept(T t)`。
        // 作用是：接收一个参数，然后“消费”掉它（比如打印），没有返回值。

        // 我们创建一个 Consumer，它接收一个 String 类型的参数，然后打印出来。
        Consumer<String> printer = (String message) -> {
            System.out.println("Consumer 正在打印: " + message);
        };

        // 简化后的写法
        Consumer<String> simplePrinter = message -> System.out.println("Consumer 正在打印: " + message);

        // 调用
        simplePrinter.accept("Hello Java Lambda!");
    }
}
