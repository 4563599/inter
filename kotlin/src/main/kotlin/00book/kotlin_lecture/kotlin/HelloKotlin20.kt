package com.shengsiyuan.kotlin

/*
    var propertyName: propertyType = initializer
        getter()...
        setter()...

    backing field，支撑字段（域）
    backing property，支撑属性
 */

class ThePerson(address: String, name: String) {

    val age: Int
        get() = 20

    var address: String = address
        get() {
            println("get invoked")
            return field
        }
        set(value) {
            println("set invoked")
            field = value
        }

    var name: String = name
}

fun main(args: Array<String>) {
    var person = ThePerson("shanghai", "zhangsan")

    println(person.age)

    println(person.address)

    person.address = "tianjin"
    println(person.address)

    println(person.name)

    person.name = "lisi"

    println(person.name)
}