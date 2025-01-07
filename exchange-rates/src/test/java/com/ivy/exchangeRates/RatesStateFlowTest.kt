package com.ivy.exchangeRates

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.hasSize
import com.ivy.MainCoroutineExtension
import com.ivy.core.domain.action.settings.basecurrency.BaseCurrencyFlow
import com.ivy.core.persistence.algorithm.calc.Rate
import com.ivy.core.persistence.algorithm.calc.RatesDao
import com.ivy.exchangeRates.data.RateUi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainCoroutineExtension::class)
class RatesStateFlowTest{

    private lateinit var ratesStateFlow: RatesStateFlow
    private lateinit var baseCurrencyFlow: BaseCurrencyFlow
    private lateinit var ratesDao: RatesDaoFake

    @BeforeEach
    fun setup(){

        baseCurrencyFlow = mockk()
        every { baseCurrencyFlow.invoke() } returns flowOf("", "EUR")
        ratesDao = RatesDaoFake()


        ratesStateFlow = RatesStateFlow(
            baseCurrencyFlow, ratesDao
        )
    }

    @Test
    fun `Test Rates state`() = runTest {

        ratesStateFlow.invoke().test {
            awaitItem() // ignore first emission

            val rate1 = awaitItem()

            val overridenRate = RateUi(from = "EUR", to = "USD", rate = 1.3)

            assertThat(rate1.manual).hasSize(1)
            assertThat(rate1.manual).contains(overridenRate)
            assertThat(rate1.automatic).doesNotContain(overridenRate)
            assertThat(rate1.automatic).hasSize(2)

            ratesDao.rates.value += Rate(rate = 0.00004, currency = "BTC")

            val rate2 = awaitItem()
            val rate = RateUi( from = "EUR", to = "BTC", rate = 0.00004)
            assertThat(rate2.automatic).hasSize(3)
            assertThat(rate2.automatic).contains(rate)
            assertThat(rate2.manual).doesNotContain(rate)


        }

    }

}