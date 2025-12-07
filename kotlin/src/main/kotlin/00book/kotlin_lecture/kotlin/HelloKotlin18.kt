package com.shengsiyuan.kotlin

open class BaseClass {

    open fun method() {

    }
}

abstract class ChildClass: BaseClass() {

    override abstract fun method()
}