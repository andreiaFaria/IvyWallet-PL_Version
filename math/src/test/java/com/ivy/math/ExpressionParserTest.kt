package com.ivy.math

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ivy.parser.Parser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import timber.log.Timber.Tree

class ExpressionParserTest{

    private lateinit var parser: Parser<TreeNode>

    @BeforeEach
    fun setUp(){
        parser = expressionParser()
    }

    @Test
    fun `Validate expression`(){

        val result = parser("5+4*2").first()

        val actual = result.value.eval()

        assertThat(actual).isEqualTo(13.0)

    }

    @ParameterizedTest
    @CsvSource(
        "9*(2-1), 9.0",
        "0*100000, 0.0",
        "1-10, -9.0"
    )
    fun `Validate several expression`(expression: Stringr, expectedResult: Double){

        val result = parser(expression).first()

        val actual = result.value.eval()

        assertThat(actual).isEqualTo(expectedResult)

    }

}