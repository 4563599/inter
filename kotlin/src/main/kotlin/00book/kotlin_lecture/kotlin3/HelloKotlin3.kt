package com.shengsiyuan.kotlin3

/*
    Kotlin访问外部类变量的方式：this@OuterClass.str
    Java访问外部类变量的方式：OuterClass.this.str
 */

class Person(val name: String, var age: Int) {

    private val str: String = "Person属性"

    private inner class PersonFeature(var height: Int, var weight: Int) {

        private val str: String = "PersonFeature属性"

        fun getPersonFeature() {
            val str: String = "局部变量"

            println("身高： $height， 体重：$weight")

            println("${this@Person.str}")
            println("${this.str}")
            println(str)

            this@Person.method()
        }
    }

    private fun method() {
        println("执行了Person的method方法")
    }

    fun getPerson() {
        val personFeature = PersonFeature(120, 180)
        personFeature.getPersonFeature()
    }
}

fun main(args: Array<String>) {
    val person = Person("zhangsan", 20)
    person.getPerson()
}