package db

import models._
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{Index, IndexType}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.{immutable, Seq}

class DeletionTaskMongoDataStore(val mongoApi: ReactiveMongoApi)(implicit val executionContext: ExecutionContext)
    extends MongoDataStore[DeletionTask] {
  val format: OFormat[DeletionTask]           = models.DeletionTask.deletionTaskFormats
  override def collectionName(tenant: String) = s"$tenant-deletionTasks"

  override def indices = Seq(
    Index(immutable.Seq("orgKey" -> IndexType.Ascending), name = Some("orgKey"), unique = false, sparse = true),
    Index(immutable.Seq("userId" -> IndexType.Ascending), name = Some("userId"), unique = false, sparse = true)
  )

  def insert(tenant: String, deletionTask: DeletionTask): Future[Boolean] =
    insertOne(tenant, deletionTask)

  def updateById(tenant: String, id: String, deletionTask: DeletionTask): Future[Boolean] =
    updateOne(tenant, id, deletionTask)

  def findById(tenant: String, id: String): Future[Option[DeletionTask]] =
    findOneById(tenant, id)

  def findAll(tenant: String, page: Int, pageSize: Int): Future[(Seq[DeletionTask], Long)] =
    findManyByQueryPaginateCount(tenant = tenant, query = Json.obj(), page = page, pageSize = pageSize)

  def findAllByOrgKey(tenant: String, orgKey: String, page: Int, pageSize: Int): Future[(Seq[DeletionTask], Long)] =
    findManyByQueryPaginateCount(
      tenant = tenant,
      query = Json.obj("orgKey" -> orgKey),
      sort = Json.obj("userId" -> 1),
      page = page,
      pageSize = pageSize
    )

  def findAllByUserId(tenant: String, userId: String, page: Int, pageSize: Int): Future[(Seq[DeletionTask], Long)] =
    findManyByQueryPaginateCount(
      tenant = tenant,
      query = Json.obj("userId" -> userId),
      page = page,
      pageSize = pageSize
    )

  def deleteDeletionTaskByTenant(tenant: String): Future[Boolean] =
    storedCollection(tenant).flatMap { col =>
      col.drop(failIfNotFound = false)
    }
}
