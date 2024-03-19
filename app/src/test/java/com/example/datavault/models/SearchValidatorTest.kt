package com.example.datavault.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SearchValidatorTest{

    // Test if the result is the same when using binary searching and linear searching
    @Test
    fun resultInBinarySameAsLinear() {
        val listNumber = mutableListOf<Int>()
        listNumber.add(1)
        listNumber.add(5)
        listNumber.add(7)
        listNumber.add(8)
        listNumber.add(11)
        listNumber.add(12)
        val result = SearchValidator.validateSearchMethod(5, listNumber)
        assertThat(result).isEqualTo(true)
    }
}