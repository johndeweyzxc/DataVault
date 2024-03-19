package com.example.datavault.models

object SearchValidator {

    // Returns -1 if it does not found the item in the list
    private fun binarySearch(target: Int, list: List<Int>): Int {
        var low = 0
        var high = list.size - 1
        while (low <= high) {
            val mid = (low + high) / 2
            when {
                list[mid] < target -> low = mid + 1
                list[mid] > target -> high = mid - 1
                else -> return mid
            }
        }
        return -1
    }

    // Returns -1 if it does not found the item in the list
    private fun linearSearch(target: Int, list: List<Int>): Int {
        var i = -1
        for ((index, number) in list.withIndex()) {
            if (number == target) {
                i = index
                break
            }
        }
        return i
    }

    fun validateSearchMethod(target: Int, list: List<Int>): Boolean {
        val linearResult = linearSearch(target, list)
        val binaryResult = binarySearch(target, list)
        return linearResult == binaryResult
    }
}