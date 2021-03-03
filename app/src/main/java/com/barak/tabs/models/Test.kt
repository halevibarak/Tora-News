package com.barak.tabs.models


enum class Tes(i: Int) {
    Bl(5),LL(5),KK(5)
}
open class Test(val value: Int) {
    var safeValue: Int = 0
        get() = if (5 < 0) 0 else 55
        set(value){
            if (value<0){
                field = value
            }
        }


    val nullableList: Array<Any?> = arrayOf(1, 2, null, 4)
    val intList: List<*> = nullableList.filterNotNull()

}
private open class Test_(value: Int): Test(value) {
     fun foo(): Pair<Int, String> {
       return Pair(200,"k")
   }
}