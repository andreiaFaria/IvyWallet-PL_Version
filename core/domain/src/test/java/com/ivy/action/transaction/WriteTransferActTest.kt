package com.ivy.action.transaction

import androidx.annotation.ColorInt
import assertk.assertThat
import com.ivy.core.data.Transaction
import com.ivy.core.domain.action.transaction.WriteTrnsAct
import com.ivy.core.domain.action.transaction.WriteTrnsBatchAct
import com.ivy.core.domain.action.transaction.transfer.ModifyTransfer
import com.ivy.core.domain.action.transaction.transfer.TransferByBatchIdAct
import com.ivy.core.domain.action.transaction.transfer.TransferData
import com.ivy.core.domain.action.transaction.transfer.WriteTransferAct
import com.ivy.data.Sync
import com.ivy.data.SyncState
import com.ivy.data.Value
import com.ivy.data.account.Account
import com.ivy.data.account.AccountState
import com.ivy.data.category.Category
import com.ivy.data.transaction.TransactionType
import com.ivy.data.transaction.TrnTime
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class WriteTransferActTest{
    private lateinit var writeTransferAct: WriteTransferAct
    private lateinit var writeTrnsBatchAct: WriteTrnsBatchAct
    private lateinit var transferByBatchIdAct: TransferByBatchIdAct
    private lateinit var writeTrnsAct: WriteTrnsAct

    private var account1 = Account(
        id = UUID.randomUUID(),
        name = "Test Account 1",
        currency = "",
        color = 0,
        icon = "",
        excluded = false,
        folderId = UUID.randomUUID(),
        orderNum = 0.0,
        state = AccountState.Default,
        sync = Sync(SyncState.Synced, LocalDateTime.now())
    )

    private var account2 = Account(
        id = UUID.randomUUID(),
        name = "Test Account 2",
        currency = "",
        color = 0,
        icon = "",
        excluded = false,
        folderId = UUID.randomUUID(),
        orderNum = 0.0,
        state = AccountState.Default,
        sync = Sync(SyncState.Synced, LocalDateTime.now())
    )

    @BeforeEach
    fun setUp(){

        writeTrnsBatchAct = mockk(relaxed = true)
        transferByBatchIdAct = mockk(relaxed = true)
        writeTrnsAct = mockk(relaxed = true)

        writeTransferAct = WriteTransferAct (
            writeTrnsBatchAct = writeTrnsBatchAct,
            transferByBatchIdAct = transferByBatchIdAct,
            writeTrnsAct = writeTrnsAct,
        )
    }

    @Test
    fun `Add transfer, feed are considered`(): Unit = runBlocking {

//        val data: TransferData = mockk()
//        every { data.fee } returns Value(amount = 2.0, currency = "EUR")
//
//        val trns = mockkConstructor(Transaction::class)
//        every { anyConstructed<Transaction>(). }
//
//        assertThat(data)


        writeTransferAct(
            ModifyTransfer.add(
                data = TransferData(
                    amountFrom = Value(amount = 50.0, currency = "EUR"),
                    amountTo =  Value(amount = 60.0, currency = "USD"),
                    accountFrom = account1,
                    accountTo = account2,
                    category = null,
                    time = TrnTime.Actual(LocalDateTime.now()),
                    title = "test transfer",
                    description = "Test transfer description",
                    fee = Value(amount = 2.0, currency = "EUR"),
                    sync = Sync(
                        state = SyncState.Synced,
                        lastUpdated = LocalDateTime.now()
                    )
                )
            )
        )

        coVerify {
            writeTrnsBatchAct(
                match {
                    it as WriteTrnsBatchAct.ModifyBatch.Save

                    val from = it.batch.trns[0]
                    val to = it.batch.trns[1]
                    val fee = it.batch.trns[2]

                    from.value.amount == 50.0 &&
                            to.value.amount == 60.0 &&
                            fee.value.amount == 2.0 &&
                            fee.type == TransactionType.Expense
                }
            )
        }



    }

}