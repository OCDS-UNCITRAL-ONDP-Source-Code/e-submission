package com.procurement.submission.infrastructure.dao

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.nhaarman.mockito_kotlin.spy
import com.procurement.submission.domain.extension.toDate
import com.procurement.submission.domain.model.Cpid
import com.procurement.submission.domain.model.Ocid
import com.procurement.submission.domain.model.enums.Stage
import com.procurement.submission.infrastructure.config.CassandraTestContainer
import com.procurement.submission.infrastructure.config.DatabaseTestConfiguration
import com.procurement.submission.model.dto.databinding.JsonDateDeserializer
import com.procurement.submission.model.dto.databinding.JsonDateSerializer
import com.procurement.submission.model.entity.PeriodEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DatabaseTestConfiguration::class])
class PeriodDaoIT {

    companion object {
        private const val PERIOD_TABLE = "submission_period"
        private const val KEYSPACE = "ocds"
        private const val CPID_COLUMN = "cp_id"
        private const val STAGE_COLUMN = "stage"
        private const val START_DATE_COLUMN = "start_date"
        private const val END_DATE_COLUMN = "end_date"

        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val STAGE = Stage.AC
        private val START_DATE = JsonDateDeserializer.deserialize(JsonDateSerializer.serialize(LocalDateTime.now()))
        private val END_DATE = START_DATE.plusDays(2)
    }

    @Autowired
    private lateinit var container: CassandraTestContainer

    private lateinit var session: Session
    private lateinit var periodDao: PeriodDao

    @BeforeEach
    fun init() {
        val poolingOptions = PoolingOptions()
            .setMaxConnectionsPerHost(HostDistance.LOCAL, 1)
        val cluster = Cluster.builder()
            .addContactPoints(container.contractPoint)
            .withPort(container.port)
            .withoutJMXReporting()
            .withPoolingOptions(poolingOptions)
            .withAuthProvider(PlainTextAuthProvider(container.username, container.password))
            .build()

        session = spy(cluster.connect())

        createKeyspace()
        createTable()

        periodDao = PeriodDao(session)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()
    }

    @Test
    fun trySave() {
        val expectedPeriod = stubPeriod()
       periodDao.trySave(expectedPeriod)

        val actual = periodDao.getByCpIdAndStage(cpId = expectedPeriod.cpId, stage = expectedPeriod.stage)

        assertEquals(expectedPeriod, actual)
    }

    private fun createKeyspace() {
        session.execute(
            "CREATE KEYSPACE ${KEYSPACE} " +
                "WITH replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1};"
        )
    }

    private fun dropKeyspace() {
        session.execute("DROP KEYSPACE ${KEYSPACE};")
    }

    private fun createTable() {
        session.execute(
            """
                CREATE TABLE IF NOT EXISTS $KEYSPACE.$PERIOD_TABLE
                    (
                        $CPID_COLUMN text,
                        $STAGE_COLUMN text,
                        $START_DATE_COLUMN timestamp,
                        $END_DATE_COLUMN timestamp,
                        primary key($CPID_COLUMN, $STAGE_COLUMN);
            """
        )
    }

    private fun stubPeriod() = PeriodEntity(
        cpId = CPID.toString(),
        stage = STAGE.toString(),
        startDate = START_DATE.toDate(),
        endDate = END_DATE.toDate()
    )

    private fun insertPeriod(
        periodEntity: PeriodEntity
    ) {
        val record = QueryBuilder.insertInto(KEYSPACE, PERIOD_TABLE)
            .value(CPID_COLUMN, periodEntity.cpId)
            .value(STAGE_COLUMN, periodEntity.stage)
            .value(START_DATE_COLUMN, periodEntity.startDate)
            .value(END_DATE_COLUMN, periodEntity.endDate)

        session.execute(record)
    }
}