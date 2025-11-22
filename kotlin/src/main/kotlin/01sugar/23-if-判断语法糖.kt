fun pickMax(a: Int, b: Int): Int =
    if (a > b) a else b // 语法糖：if 直接返回值

fun describeScore(score: Int): String {
    // 语法糖：if/else-if/else 本身是表达式，可在 block 中包含逻辑
    val level = if (score >= 90) {
        "优秀"
    } else if (score >= 60) {
        "及格"
    } else {
        "不及格"
    }
    return "$score -> $level"
}

fun main1() {
    val max = pickMax(3, 7)
    println("max=$max")

    listOf(95, 70, 40)
        .map(::describeScore)
        .forEach { println(it) }
}


fun pickMax1(a: Int, b: Int): Int =
    if (a > b) a else b

fun main() {
    val max = pickMax(3, 5);
    println("max=$max")
    println(describeScore1(30));
}

fun describeScore1(score: Int ): String{
    val level =if(score>90){
        "优秀"
    }else if(score>=60){
        "及格 "
    }else{
        "垃圾"
    }
//    return "$score -> $level"
    return level;
}