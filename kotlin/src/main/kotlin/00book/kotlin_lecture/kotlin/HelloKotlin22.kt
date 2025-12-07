package com.shengsiyuan.kotlin

// 可见性 visibility
// Kotlin提供了4种可见性修饰符：private, protected, internal, public

fun method() {

}



open class Clazz {

    private val b = 3;

    protected open val c = 4;

    internal val d = 5;
}

class subClazz: Clazz() {

}

class Clazz2 {

}