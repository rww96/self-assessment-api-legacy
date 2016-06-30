/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi.services

import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.domain.TaxYear
import uk.gov.hmrc.selfassessmentapi.domain.selfemployment.SelfEmployment
import uk.gov.hmrc.selfassessmentapi.repositories.domain.MongoJobStatus._
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{MongoJobHistory, MongoSelfAssessment, MongoSelfEmployment}
import uk.gov.hmrc.selfassessmentapi.repositories.live.SelfEmploymentMongoRepository
import uk.gov.hmrc.selfassessmentapi.repositories.{JobHistoryMongoRepository, SelfAssessmentMongoRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class DeleteExpiredDataServiceSpec extends MongoEmbeddedDatabase with BeforeAndAfterEach with MockitoSugar {

  private val saRepo = new SelfAssessmentMongoRepository
  private val seRepo = new SelfEmploymentMongoRepository
  private val jobRepo = new JobHistoryMongoRepository

  val taxYear = TaxYear("2016-17")
  val saUtr = generateSaUtr()
  val lastModifiedDate = DateTime.now().minusWeeks(1)

  override def beforeEach() {
    dropSelfAssessment()
    dropSelfEmployment()
    dropJob()
  }

  "deleteExpiredData" should {

    "delete only the expired records (older than the lastModifiedDate) and not the latest records which have not expired" in {
      val lastModifiedDate = DateTime.now.minusWeeks(1)
      val saUtr2 = generateSaUtr()
      val saUtr3 = generateSaUtr()

      val sa1 = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val sa2 = MongoSelfAssessment(BSONObjectID.generate, saUtr2, taxYear, DateTime.now().minusMonths(2), DateTime.now().minusMonths(2))
      val latestSa3 = MongoSelfAssessment(BSONObjectID.generate, saUtr3, taxYear, DateTime.now().minusDays(1), DateTime.now().minusDays(1))

      val se1 = MongoSelfEmployment.create(saUtr, taxYear, SelfEmployment.example())
      val se2 = MongoSelfEmployment.create(saUtr2, taxYear, SelfEmployment.example())
      val latestSe3 = MongoSelfEmployment.create(saUtr3, taxYear, SelfEmployment.example())

      insertSelfAssessmentRecords(sa1, sa2, latestSa3)
      insertSelfEmploymentRecords(se1, se2, latestSe3)

      val service = new DeleteExpiredDataService(saRepo, seRepo, jobRepo)

      await(service.deleteExpiredData(lastModifiedDate))

      val saRecords = await(saRepo.findAll())
      val seRecords = await(seRepo.findAll())

      saRecords.size shouldBe 1
      saRecords.head.saUtr == latestSa3.saUtr && saRecords.head.taxYear == latestSa3.taxYear shouldBe true

      seRecords.size shouldBe 1
      seRecords.head.saUtr == latestSe3.saUtr && seRecords.head.taxYear == latestSe3.taxYear shouldBe true
    }

    "mark job as failed if there is an exception when trying to delete records from self assessment" in {
      val sa1 = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val saRepo = mock[SelfAssessmentMongoRepository]
      when(saRepo.findOlderThan(any())).thenReturn(Future.successful(Seq(sa1)))
      when(saRepo.delete(any(), any())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(saRepo).delete(any(), any())
    }

    "mark job as failed if there is an exception when trying to delete records from self employment" in {
      val sa1 = MongoSelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now().minusMonths(1), DateTime.now().minusMonths(1))
      val seRepo = mock[SelfEmploymentMongoRepository]
      await(saRepo.insert(sa1))
      when(seRepo.delete(any(), any())).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(jobRepo.find().head).status shouldBe Failed
      verify(seRepo).delete(any(), any())
    }


    "mark job as failed if there is an exception when trying to mark job as completed" in {
      val jobRepo = mock[JobHistoryMongoRepository]
      when(jobRepo.startJob()).thenReturn(Future(MongoJobHistory(1, InProgress)))
      when(jobRepo.completeJob(1, 0)).thenThrow(new RuntimeException("something wrong"))

      val service = new DeleteExpiredDataService(saRepo, seRepo, jobRepo)

      an[RuntimeException] should be thrownBy await(service.deleteExpiredData(DateTime.now))

      await(saRepo.findAll()).size shouldBe 0
      await(seRepo.findAll()).size shouldBe 0
      verify(jobRepo).abortJob(1)
    }

  }

  private def insertSelfAssessmentRecords(records: MongoSelfAssessment *) = {
    records.foreach { record =>
      await(saRepo.insert(record))
    }
  }

  private def insertSelfEmploymentRecords(records: MongoSelfEmployment *) = {
    records.foreach { record =>
      await(seRepo.insert(record))
    }
  }

  private def dropSelfAssessment() = {
    await(saRepo.drop)
    await(saRepo.ensureIndexes)
  }

  private def dropSelfEmployment() = {
    await(seRepo.drop)
    await(seRepo.ensureIndexes)
  }

  private def dropJob() = {
    await(jobRepo.drop)
    await(jobRepo.ensureIndexes)
  }
}
